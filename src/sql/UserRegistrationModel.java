package sql;

public class UserRegistrationModel extends SQLExecutable {

	private String emailAddress, firstName, lastName, sha256pwd;
	
	public static enum RegistrationResult {
		CREATED,
		EMAIL_TAKEN,
		MALFORMED_REQUEST
	}
	
	public UserRegistrationModel (String emailAddress, String firstName, String lastName, String sha256pwd){
		this.emailAddress = emailAddress;
		this.firstName = firstName;
		this.lastName = lastName;
		this.sha256pwd = sha256pwd;
	}
	
	public RegistrationResult register() {
		if (emailAddress == null || sha256pwd == null || firstName == null || lastName == null) return RegistrationResult.MALFORMED_REQUEST;
		//TODO: Check for valid email address
		return RegistrationResult.EMAIL_TAKEN;
	}
}
