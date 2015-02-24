package sql.wrappers;

import java.sql.ResultSet;
import java.sql.SQLException;

import com.google.gson.annotations.Expose;

import sql.SQLExecutable;
import sql.SQLParam;
import sql.SQLType;
import core.Permissions;

public class HouseholdCreationWrapper extends SQLExecutable {
	@Expose(serialize = true)
	private int householdID = -1;
	
	private String householdName, householdDescription;
	private int userID;
	
	public HouseholdCreationWrapper (String name, String description, int userID) {
		this.userID = userID;
		householdName =name;
		householdDescription = description;
	}
	
	public static enum HouseholdCreationResult {
		CREATED,
		INTERNAL_ERROR
	}
	
	public HouseholdCreationResult create() {
		int affected = 0;
		try {
			affected = update("INSERT INTO Household (Name, Description, HeadOfHousehold) VALUES (?, ?, ?);",
					new SQLParam(householdName, SQLType.VARCHAR),
					new SQLParam(householdDescription, SQLType.VARCHAR),
					new SQLParam(userID, SQLType.INT));
		} catch (SQLException e) {
			rollback();
			release();
			return HouseholdCreationResult.INTERNAL_ERROR;
		}
		if (affected == 0) {rollback(); release(); return HouseholdCreationResult.INTERNAL_ERROR;}
		
		
		
		ResultSet results = null;
		try {
			results = query("SELECT LAST_INSERT_ID() AS lastID;");
		} catch (SQLException e) {
			rollback();
			release();
			return HouseholdCreationResult.INTERNAL_ERROR;
		}
		if (results == null) {rollback(); release(); return HouseholdCreationResult.INTERNAL_ERROR;}
		try {
			results.next();
			householdID = results.getInt("lastID");
		} catch (SQLException e) {
			rollback();
			return HouseholdCreationResult.INTERNAL_ERROR;
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
			return HouseholdCreationResult.INTERNAL_ERROR;
		}
		if (affected == 0) {rollback(); release(); return HouseholdCreationResult.INTERNAL_ERROR;}
		release();

		return HouseholdCreationResult.CREATED;
		
	}
	
	

}
