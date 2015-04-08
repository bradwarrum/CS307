package core;


public enum ResponseCode {
	OK(200),
	CREATED(201),
	NOT_MODIFIED(304),
	INTERNAL_ERROR(500, "Generic Internal Server Error"),
	TOKEN_EXPIRED(403,"The user token is expired, renew the token."),
	INVALID_TOKEN(403, "A user cannot be found for that token."),
	EMAIL_TAKEN(403, "Cannot register a user for that email address, the email address is already taken"),
	INVALID_PASSWORD(403, "The user's password is incorrect."),
	INVALID_PAYLOAD(400, "JSON request payload invalid.  Check field lengths and field names."),
	USER_NOT_FOUND(400, "No user was found with that email address."),
	HOUSEHOLD_NOT_FOUND(400, "User is not associated with that household."),
	LIST_NOT_FOUND(400, "One or more shopping lists in the request cannot be found for that household."),
	ITEM_NOT_FOUND(400, "One or more UPC strings are not associated with the household, ensure that all UPCs are linked to the household."),
	RECIPE_NOT_FOUND(400, "One or more of the recipies cannot be found for that household."),
	UPC_FORMAT_NOT_SUPPORTED(400, "Support does not exist for barcodes of that format."),
	UPC_CHECKSUM_INVALID(400, "UPC-A or EAN-13 barcode has an invalid checksum, try again."),
	INSUFFICIENT_PERMISSIONS(403, "The user has insufficient household permissions to perform this action."),
	OUTDATED_TIMESTAMP(409, "The server version of this resource is different than the client's version.  Update the resource on the client and retry.");
	
	private final int errorcode;
	private final int httpcode;
	private final String description;
	private ResponseCode(int httpcode, String description) {
		this.errorcode = this.ordinal();
		this.httpcode = httpcode;
		this.description = description;
	}
	
	private ResponseCode(int httpcode) {
		this.errorcode = -1;
		this.httpcode = httpcode;
		this.description = null;
	}
	
	public int getErrorCode() {
		return errorcode;
	}
	
	public int getHttpCode() {
		return httpcode;
	}
	
	public String getDescription() {
		return description;
	}
	
	public boolean success() {
		return (errorcode == -1);
	}
	
	@Override
	public String toString() {
		if (!success())
			return "{\n  \"errorCode\" : " + errorcode + ",\n  \"errorName\" : \"" + this.name() + "\",\n  \"httpStatus\" : " + httpcode + ",\n  \"errorDescription\" : \"" + description + "\"\n}";
		else
			return "";
	}
	
}
