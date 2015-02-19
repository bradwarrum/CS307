package core;
import java.security.*;
import org.apache.commons.codec.binary.*;
public class SessionToken {
	private String token;
	private static SecureRandom rand;
	private static boolean running = false;
	public static void startRNG() {
		if (running) return;
		running = true;
		try {
			rand = SecureRandom.getInstance("SHA1PRNG");
		} catch(Exception e) {
			rand = new SecureRandom();
		}
		rand.nextBytes(new byte[16]);
	}
	public static SessionToken generate() throws NullPointerException {
		if (running == false || rand == null) throw new NullPointerException();
		byte[] bytes = new byte[16];
		rand.nextBytes(bytes);
		return new SessionToken(bytes);
	}
	
	public static SessionToken fromString(String base64EncodedString) {
		return new SessionToken(base64EncodedString);
	}
	
	private SessionToken(byte[] rawbytes) {
		token = Base64.encodeBase64URLSafeString(rawbytes);
	}
	
	public SessionToken(String base64) {
		token = base64;
	}
	
	public String getToken() {
		return token;
	}
	
	@Override
	public boolean equals(Object o) {
		return (o != null) && o.getClass().equals(this) && ((SessionToken) o).token.equals(this.token);
	}
	
	@Override
	public int hashCode() {
		return token.hashCode();
	}
	@Override
	public String toString() {
		return token;
	}
}
