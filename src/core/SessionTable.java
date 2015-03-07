package core;
import java.security.InvalidParameterException;
import java.util.HashMap;
import java.util.Map.Entry;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.locks.ReentrantLock;
public class SessionTable {
	//Sessions are keyed by session token since the majority of concurrency will occur
	//When accessing the table by token
	public final ConcurrentHashMap<SessionToken,SessionRecord> sessions = new ConcurrentHashMap<SessionToken,SessionRecord>();
	//Synchronized slow key lookup will suffice for users trying to obtain a new key (much less frequent)
	private final ReentrantLock lock = new ReentrantLock(true);
	private final HashMap<Integer,SessionToken> userkeys = new HashMap<Integer,SessionToken>();
	private final int EXPIRATION_SEC;

	private ScheduledExecutorService cleaner = Executors.newScheduledThreadPool(1);
	public SessionTable(int expirationTimeSec, int cleanerPeriodMinutes) {
		EXPIRATION_SEC = expirationTimeSec;
		if (cleanerPeriodMinutes > 0)
			cleaner.scheduleAtFixedRate(cleaner_runnable, cleanerPeriodMinutes, cleanerPeriodMinutes, TimeUnit.MINUTES);
	}
	
	public void forceStartCleaner() {
		if (cleaner_runnable.RUNNING) return;
		cleaner.execute(cleaner_runnable);
	}
	public SessionToken login(int user) throws InvalidParameterException {
		SessionToken tok = null;
		if (user == -1) throw new InvalidParameterException();
		Integer u = (Integer)user;
		lock.lock();
		try {
			tok = userkeys.get(u);
		} finally {lock.unlock();}

		//If token is not found for a user, insert a token for that user
		if (tok == null) {
			tok = SessionToken.generate();
			//PutIfAbsent to prevent queued threads from overwriting the first value
			SessionToken prevToken = null;
			lock.lock();
			try {
				prevToken = userkeys.putIfAbsent(u, tok);
				if (prevToken == null) sessions.put(tok, new SessionRecord(u, EXPIRATION_SEC));
				else tok = prevToken;
			// Only place the Record if our token got placed in the userkeys table. Otherwise, pass back the one that was already there.
			// It is assumed that the other thread put the new session record in the sessions table for their generated token.
			} finally {lock.unlock();}
			return tok;
		} else {
			// If the token was found for a user
			SessionRecord rec = sessions.get(tok);

			//If the record is null, it probably expired and was removed, so generate a new token
			//replace the userkeys table entry with the new token and put a new session record in the table
			if (rec == null) {
				SessionToken newtok = SessionToken.generate();
				boolean replaced = false;
				lock.lock();
				try {
					replaced = userkeys.replace(u,  tok, newtok);
					if (!replaced) {
						newtok = userkeys.get(u);
					} else {
						sessions.put(newtok, new SessionRecord(u, EXPIRATION_SEC));
					}
				} finally {lock.unlock();}
				return newtok;
			} else if (rec.getUserID() != u) {
				// If the record is valid but somehow belongs to another user, we should remove the other user's reference
				// To that token if that reference still exists, and replace the record associated with the token
				System.out.println("WARNING: Overlapping record values in session table, removing oldest value");
				lock.lock();
				try {
					userkeys.replace(rec.getUserID(), tok, null);
					sessions.put(tok, new SessionRecord(u, EXPIRATION_SEC));
				} finally {lock.unlock();}
				return tok;
			} else {
				// Record was retrieved successfully and user ID matches.  Check for expiration time
				if (rec.isExpired()) {
					//If the record is expired we need to generate a new one and pass it back
					SessionToken newtok = SessionToken.generate();
					boolean replaced = false;
					lock.lock();
					try {
						replaced = userkeys.replace(u,  tok, newtok);
						if (!replaced) {
							newtok = userkeys.get(u);
						} else {
							 sessions.put(newtok, new SessionRecord(u, EXPIRATION_SEC));
						}
					} finally {lock.unlock();}
					return newtok;
				} else {
					//Otherwise, just return the token.
					return tok;
				}
			}
		}
	}

	public int authenticate(SessionToken token) {
		//Query the sessions table for a record
		SessionRecord rec = null;
		if (token == null) return -1;
		rec = sessions.get(token);
		if (rec == null) return -1;
		if (rec.isExpired()) {
			sessions.remove(token, rec);
			return -2;
		}
		return rec.getUserID();
	}
	
	
	//CLEANING STUFF
	private static class Cleaner implements Runnable {
		public volatile boolean RUNNING = false;
		@Override
		public void run() {
		}
	}
	private final Cleaner cleaner_runnable = new Cleaner () {

		@Override
		public void run() {
			if (RUNNING) return;
			RUNNING = true;
			//System.out.println("Starting SessionTable cleaner");
			int cleanseCount = 0;
			try {

				for (Entry<SessionToken, SessionRecord> m : sessions.entrySet()) {
					SessionToken k = m.getKey();
					if (k == null) continue;
					SessionRecord r = m.getValue();
					if (r != null && r.isExpired()) {
						SessionRecord rec = sessions.get(k);
						if (rec != null && rec.equals(r)) {
							lock.lock();
							try{
								if (k == userkeys.get(r.getUserID())) {
									//System.out.println("Removing expired token for user " + r.getUserID());
									userkeys.remove(r.getUserID());
									sessions.remove(k);
								}
							} finally{lock.unlock();}
							cleanseCount++;
						}
					}
				}
			} catch (Exception e) {
				e.printStackTrace();
			}
			finally {
				RUNNING = false;
				System.out.println(cleanseCount + " records removed from SessionTable");
			}
			
		}
		
	};

}
