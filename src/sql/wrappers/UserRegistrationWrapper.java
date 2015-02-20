package sql.wrappers;

import java.security.MessageDigest;
import java.security.SecureRandom;
import java.sql.ResultSet;
import java.sql.SQLException;

import org.apache.commons.codec.DecoderException;
import org.apache.commons.codec.binary.Hex;

import sql.SQLExecutable;
import sql.SQLParam;
import sql.SQLType;

public class UserRegistrationWrapper extends SQLExecutable {
	private String emailAddress, firstName, lastName, sha256pwd;
	
	public static enum RegistrationResult {
		CREATED,
		EMAIL_TAKEN,
		MALFORMED_INPUT,
		INTERNAL_ERROR
	}
	
	public UserRegistrationWrapper (String emailAddress, String firstName, String lastName, String sha256pwd){
		this.emailAddress = emailAddress;
		this.firstName = firstName;
		this.lastName = lastName;
		this.sha256pwd = sha256pwd;
	}
	
	public RegistrationResult register() {
		if (emailAddress == null || sha256pwd == null || firstName == null || lastName == null) return RegistrationResult.MALFORMED_INPUT;
		//Ensure password might actually be a SHA-256 hash
		byte [] bytePWD;
		try {
			bytePWD = Hex.decodeHex(sha256pwd.toCharArray());
			if (bytePWD.length != 32) return RegistrationResult.MALFORMED_INPUT;
		} catch (DecoderException e) {return RegistrationResult.MALFORMED_INPUT;}
		
		//TODO: Check for valid email address
		
		//TODO: Check this SQL statement
		//Fail fast if email address already exists
		ResultSet results = null;
		try {
			results = query("SELECT COUNT(Email) AS count FROM User WHERE (Email = ?);", new SQLParam(emailAddress, SQLType.VARCHAR)); 
		} catch (Exception s) {
			release();
			return RegistrationResult.INTERNAL_ERROR;
		}
		try {
		if (results != null && results.next()) {
			if (results.getInt("count") != 0) { release(); return RegistrationResult.EMAIL_TAKEN;}
		}
		} catch (SQLException e) {return RegistrationResult.INTERNAL_ERROR;}
		finally {release();}
		
		//Now we can take the time to generate salt and hash
		
		byte [] byteSALT = new byte[16];
		byte [] saltedpwd;
		MessageDigest digest;
		try {
			SecureRandom.getInstance("SHA1PRNG").nextBytes(byteSALT);
			digest = MessageDigest.getInstance("SHA-256");
			saltedpwd = new byte[32+16];
			System.arraycopy(bytePWD, 0, saltedpwd, 0, bytePWD.length);
			System.arraycopy(byteSALT, 0, saltedpwd, bytePWD.length, byteSALT.length);
		} catch (Exception e) {return RegistrationResult.INTERNAL_ERROR;}
		byte[] hashedpwd = digest.digest(saltedpwd);
		String hash = Hex.encodeHexString(hashedpwd);
		String salt = Hex.encodeHexString(byteSALT);
		//Now try to insert the new user into the database
		try {
			int count = update("INSERT INTO User (Email, Password, PasswordSalt, FirstName, LastName) VALUES (?, ?, ?, ?, ?);",
					new SQLParam(emailAddress, SQLType.VARCHAR),
					new SQLParam(hash, SQLType.CHAR),
					new SQLParam(salt, SQLType.CHAR),
					new SQLParam(firstName, SQLType.VARCHAR),
					new SQLParam(lastName, SQLType.VARCHAR));
			if (count != 1) return RegistrationResult.EMAIL_TAKEN;
			else return RegistrationResult.CREATED;
		} catch (Exception e) {return RegistrationResult.EMAIL_TAKEN;}
		finally{
			release();
		}
	}
}
