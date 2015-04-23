package sql.wrappers;

import java.sql.ResultSet;
import java.sql.SQLException;

import com.google.gson.annotations.Expose;


import sql.SQLParam;
import sql.SQLType;
import core.Permissions;
import core.ResponseCode;

public class HouseholdCreationWrapper extends BaseWrapper {
	@Expose(serialize = true)
	private int householdID = -1;
	@Expose(serialize = true)
	private long version = -1;
	
	private String householdName, householdDescription;
	private int userID;
	
	public HouseholdCreationWrapper (String name, String description, int userID) {
		this.userID = userID;
		householdName =name;
		householdDescription = description;
	}
	
	public ResponseCode create() {
		int affected = 0;
		version = System.currentTimeMillis();
		try {
			affected = update("INSERT INTO Household (Name, Description, HeadOfHousehold, Version, AvailableProduceID) VALUES (?, ?, ?, ?, ?);",
					new SQLParam(householdName, SQLType.VARCHAR),
					new SQLParam(householdDescription, SQLType.VARCHAR),
					new SQLParam(userID, SQLType.INT),
					new SQLParam(version, SQLType.LONG),
					new SQLParam(1, SQLType.INT));
		} catch (SQLException e) {
			rollback();
			release();
			return ResponseCode.INTERNAL_ERROR;
		}
		if (affected == 0) {rollback(); release(); return ResponseCode.INTERNAL_ERROR;}
		
		
		
		ResultSet results = null;
		try {
			results = query("SELECT LAST_INSERT_ID() AS lastID;");
		} catch (SQLException e) {
			rollback();
			release();
			return ResponseCode.INTERNAL_ERROR;
		}
		if (results == null) {rollback(); release(); return ResponseCode.INTERNAL_ERROR;}
		try {
			results.next();
			householdID = results.getInt("lastID");
		} catch (SQLException e) {
			rollback();
			return ResponseCode.INTERNAL_ERROR;
		} finally {
			release(results);
		}		
		
		
		
		Permissions perms = Permissions.all();

		try {
			affected = update("INSERT INTO HouseholdPermissions (UserId, HouseholdId, PermissionLevel) VALUES (?, ?, ?);", 
					new SQLParam(userID, SQLType.INT),
					new SQLParam(householdID, SQLType.INT),
					new SQLParam(perms.asInt(), SQLType.INT));
		} catch (SQLException e) {
			rollback();
			return ResponseCode.INTERNAL_ERROR;
		}
		if (affected == 0) {rollback(); release(); return ResponseCode.INTERNAL_ERROR;}
		release();

		return ResponseCode.CREATED;
		
	}
	
	

}
