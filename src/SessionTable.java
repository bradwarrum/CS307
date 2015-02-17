import java.util.HashMap;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.locks.ReentrantLock;
public class SessionTable {
	public static enum AuthResult {
		OK,
		TOKEN_EXPIRED,
		WRONG_USER,
		NOT_FOUND
	}
	//Sessions are keyed by session token since the majority of concurrency will occur
	//When accessing the table by token
	private ConcurrentHashMap<SessionToken,SessionRecord> sessions = new ConcurrentHashMap<SessionToken,SessionRecord>();
	//Synchronized slow key lookup will suffice for users trying to obtain a new key (much less frequent)
	private final ReentrantLock lock = new ReentrantLock(true);
	private HashMap<Integer,SessionToken> userkeys = new HashMap<Integer,SessionToken>();
	
	public SessionToken updateSession(int user) {
		//Refreshing a token should
		//1) Look up the SessionToken for the specified user
		//2) Remove the record from the sessions object
		//3) Add a new record with refreshed expiration date to the sessions object
		//4) Update the userkeys object with the new token
		SessionToken tok = null;
		Integer u = (Integer)user;
		lock.lock();
		try {
			tok = userkeys.remove(u);
		} finally {
			lock.unlock();
		}
		
		if (tok != null) {
			sessions.remove(tok);
		}
		
		tok = SessionToken.generate();
		SessionRecord rec = new SessionRecord(user, 20); //Expires in 20 minutes
		sessions.put(tok, rec);
		lock.lock();
		try {
			userkeys.put(u, tok);
		} finally {
			lock.unlock();
		}
		return tok;
	}
	/**
	 * Queries the session table and attempts to authenticate the user with the specified token.
	 * @param user The user that is attempting to authenticate
	 * @param token The token value that is attempting to authenticate the user
	 * @return AuthResult.OK if the user and token match and the token is not expired.<p>
	 * AuthResult.TOKEN_EXPIRED if the user and token match, but the token is expired.<p>
	 * AuthResult.WRONG_USER if the token returns a result, but the userID's do not match.<p>
	 * AuthResult.NOT_FOUND if the token does not correspond to a valid session record.
	 */
	public AuthResult authenticate(int user, SessionToken token) {
		//Query the sessions table for a record
		SessionRecord rec = null;
		if (token == null) return AuthResult.NOT_FOUND;
		rec = sessions.get(token);
		if (rec == null) return AuthResult.NOT_FOUND;
		if (rec.getUserID() != user) return AuthResult.WRONG_USER;
		long unixNow = System.currentTimeMillis() / 1000L;
		if (unixNow > rec.getExpiration()) return AuthResult.TOKEN_EXPIRED;
		return AuthResult.OK;
	}
}
