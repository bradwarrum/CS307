package sql.wrappers;

import java.security.MessageDigest;
import java.security.SecureRandom;
import java.sql.SQLException;

import org.apache.commons.codec.DecoderException;
import org.apache.commons.codec.binary.Hex;

import core.ResponseCode;
import sql.SQLExecutable;
import sql.SQLParam;
import sql.SQLType;

public class UserRegistrationWrapper extends SQLExecutable {
	private String emailAddress, firstName, lastName, sha256pwd;
	
	
	public UserRegistrationWrapper (String emailAddress, String firstName, String lastName, String sha256pwd){
		this.emailAddress = emailAddress;
		this.firstName = firstName;
		this.lastName = lastName;
		this.sha256pwd = sha256pwd;
	}
	
	public ResponseCode register() {
		if (emailAddress == null || sha256pwd == null || firstName == null || lastName == null) return ResponseCode.INVALID_PAYLOAD;
		//Ensure password might actually be a SHA-256 hash
		byte [] bytePWD;
		try {
			bytePWD = Hex.decodeHex(sha256pwd.toCharArray());
			if (bytePWD.length != 32) return ResponseCode.INVALID_PAYLOAD;
		} catch (DecoderException e) {return ResponseCode.INVALID_PAYLOAD;}
		
		//TODO: Check for valid email address
		
		//TODO: Check this SQL statement
		byte [] byteSALT = new byte[16];
		byte [] saltedpwd;
		MessageDigest digest;
		try {
			SecureRandom.getInstance("SHA1PRNG").nextBytes(byteSALT);
			digest = MessageDigest.getInstance("SHA-256");
			saltedpwd = new byte[32+16];
			System.arraycopy(bytePWD, 0, saltedpwd, 0, bytePWD.length);
			System.arraycopy(byteSALT, 0, saltedpwd, bytePWD.length, byteSALT.length);
		} catch (Exception e) {return ResponseCode.INTERNAL_ERROR;}
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
			if (count != 1) return ResponseCode.EMAIL_TAKEN;
			else return ResponseCode.CREATED;
		} catch (SQLException sqle) {
			String state = sqle.getSQLState();
			//Duplicate key in table
			if (state.equalsIgnoreCase("23000"))
				return ResponseCode.EMAIL_TAKEN;
			else 
				return ResponseCode.INTERNAL_ERROR;
		}
		finally{
			release();
		}
	}
}
