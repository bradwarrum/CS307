import static org.junit.Assert.*;

import org.junit.Test;

import java.util.HashSet;
import java.util.Set;
import java.util.concurrent.*;

public class SessionTableTest {

	@Test
	public void testSynchronous() {
		SessionToken.startRNG();
		SessionTable st = new SessionTable();
		//Check null inputs
		assertSame("Null inputs", st.authenticate(1, null), SessionTable.AuthResult.NOT_FOUND);
		assertSame("Authentication prior to update", st.authenticate(1, new SessionToken("1234")), SessionTable.AuthResult.NOT_FOUND);
		SessionToken user1 = st.updateSession(1);
		assertSame("User1 Authentication", st.authenticate(1, user1), SessionTable.AuthResult.OK);
		SessionToken user2 = st.updateSession(2);
		assertSame("User2 Authentication", st.authenticate(2,  user2), SessionTable.AuthResult.OK);
		assertSame("Wrong user Authentication", st.authenticate(1, user2), SessionTable.AuthResult.WRONG_USER);
		SessionToken user1new = st.updateSession(1);
		assertSame("User1 Authenticate with Old Key", st.authenticate(1, user1), SessionTable.AuthResult.NOT_FOUND);
		assertSame("User1 Authenticate With New Key", st.authenticate(1, user1new), SessionTable.AuthResult.OK);
	}
	
	public final int TESTCOUNT = 100000;
	public final int THREADCOUNT = 4;
	
	@Test
	public void testAsynchronous() throws InterruptedException, ExecutionException {
		SessionToken.startRNG();
		SessionTable st = new SessionTable();
		SessionToken[] tokens = new SessionToken[TESTCOUNT];
		Set<Future<SessionTable.AuthResult>> futures = new HashSet<Future<SessionTable.AuthResult>>();
		ExecutorService ex = Executors.newFixedThreadPool(THREADCOUNT);
		for (int i = 0; i < TESTCOUNT; i++) {
			ex.execute(new RunAdd(st, i, tokens));
		}
		Thread.sleep(3000);
		for (int i = TESTCOUNT-1; i>=0; i--) {
			futures.add(ex.submit(new RunCheck(st, i, tokens[i])));
			
		}
		long start = System.currentTimeMillis();
		for (Future<SessionTable.AuthResult> f : futures) {
			assertSame("User authenticate", f.get(), SessionTable.AuthResult.OK);
		}
		long end = System.currentTimeMillis();
		System.out.println("Authentication took " + (end - start) + " milliseconds"); 
	}
	
	public static class RunAdd implements Runnable {

		public SessionTable st;
		public int user;
		public SessionToken[] tokens;
		@Override
		public void run() {
			SessionToken t = st.updateSession(user);
			//System.out.println("Added user " + user);
			tokens[user] = t;
		}
		
		public RunAdd(SessionTable st, int user, SessionToken[] tokens) {
			this.st = st;
			this.user = user;
			this.tokens = tokens;
		}
		
	}
	
	public static class RunCheck implements Callable<SessionTable.AuthResult> {

		public SessionTable st;
		public int user;
		public SessionToken t;
		public RunCheck(SessionTable st, int user, SessionToken t) {
			this.st = st;
			this.t = t;
			this.user = user;
		}

		@Override
		public SessionTable.AuthResult call() throws Exception {
			return st.authenticate(user, t);
		}

		
	}

}
