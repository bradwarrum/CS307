import static org.junit.Assert.*;

import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;

import java.util.HashSet;
import java.util.LinkedList;
import java.util.Queue;
import java.util.Set;
import java.util.concurrent.*;
import java.security.InvalidParameterException;

public class SessionTableTest {
	
	public static final int TESTCOUNT = 10000;
	public static final int THREADCOUNT = 25 ;
	private static ExecutorService ex;
	private SessionTable st;
	private SessionToken[] tokens;
	@BeforeClass
	public static void setupRNG() {
		System.out.println("Setting up RNG");
		SessionToken.startRNG();
		ex = Executors.newFixedThreadPool(THREADCOUNT);
	}
	
	@Before
	public void setUp() {
		System.out.println("Setting up required classes");
		st = new SessionTable(3, 0);
		tokens = new SessionToken[TESTCOUNT];
	}
	@Test(expected=InvalidParameterException.class)
	public void testNullInputs() {
		st.login(-1);
		assertSame("Null inputs", st.authenticate(null), -1);
	}
	
	@Test
	public void testSynchronousTypical() {
		assertSame("Authentication prior to update", st.authenticate(new SessionToken("1234")), -1);
		SessionToken user1 = st.login(1);
		assertSame("User1 Authentication", st.authenticate(user1), 1);
		SessionToken user2 = st.login(2);
		assertSame("User2 Authentication", st.authenticate(user2), 2);
		SessionToken user1new = st.login(1);
		assertSame("Returns same key if not expired", user1, user1new);
	}

	@Test
	public void testAsynchronous() throws InterruptedException, ExecutionException {
		Set<Future<Integer>> futures = new HashSet<Future<Integer>>();
		Set<Future<Object>> done = new HashSet<Future<Object>>();
		for (int i = 0; i < TESTCOUNT; i++) {
			Integer ind = new Integer(i);
			done.add(ex.submit(new Callable<Object> () {
				@Override
				public Object call() {
					tokens[ind] = st.login(ind);
					return new Object();
				}}));
		}
		for (Future<Object> f : done) {
			f.get();
		}
		for (int i = TESTCOUNT-1; i>=0; i--) {
			Integer ind = new Integer(i);
			futures.add(ex.submit(new Callable<Integer>() {
				@Override
				public Integer call() throws Exception {

					return st.authenticate(tokens[ind]);
				}}));
			
		}
		long start = System.currentTimeMillis();
		for (Future<Integer> f : futures) {
			assertNotSame("User authenticate " + f.get(), f.get(), -1);
		}
		long end = System.currentTimeMillis();
		System.out.println("Authentication took " + (end - start) + " milliseconds"); 
	}
	@Test
	public void testSimultaneousLogin() throws InterruptedException, ExecutionException {
		Set<Future<SessionToken>> futures = new HashSet<Future<SessionToken>>();
		Queue<SessionToken> stokens = new LinkedList<SessionToken>();
		for (int i = 0; i < TESTCOUNT; i++) {
			//Try to log the user in TESTCOUNT times at once, all tokens should be the same
			futures.add(ex.submit(new Callable<SessionToken>() {
				@Override
				public SessionToken call() throws Exception {
					return st.login(250);
				}}));
		}
		for (Future<SessionToken> f : futures) {
			stokens.add(f.get());
		}
		SessionToken valid = stokens.poll();
		while (!stokens.isEmpty()) {
			assertSame ("Token equality issue on index " + stokens.size() , stokens.poll(), valid);
		}
		
	}
	
	@Test
	public void testCleaner() throws InterruptedException {
		for (int i = 0; i < 1000000; i++) {
			if (i % 100000 == 0) System.out.println("" + i);
			st.login(i);
		}
		Thread.sleep(4000);
		st.forceStartCleaner();
		Thread.sleep(100);
		st.sessions.clear();
		for (int i = 0; i < 1000000; i++) {
			if (i % 100000 == 0) System.out.println("" + i);
			st.login(i);
		}
		Thread.sleep(4000);
		st.forceStartCleaner();
		Thread.sleep(8000);
	}
}
