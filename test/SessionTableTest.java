import static org.junit.Assert.*;

import org.junit.Test;


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

}
