package sql.wrappers;

import java.sql.ResultSet;
import java.sql.SQLException;

import sql.SQLParam;
import sql.SQLType;

import com.google.gson.annotations.Expose;

import core.Permissions;
import core.ResponseCode;

public class ListCreateWrapper extends BaseWrapper {
	private String listName;
	@Expose(serialize = true)
	private int listID = -1;
	@Expose(serialize = true)
	private long version = -1;
	private int userID, householdID;
	
	public ListCreateWrapper(int userID, int householdID, String listName) {
		this.listName = listName;
		this.userID = userID;
		this.householdID = householdID;
	}
	
	
	public ResponseCode create() {
		int permLevel = getPermissions(userID, householdID);
		if (permLevel == -2) return ResponseCode.HOUSEHOLD_NOT_FOUND;
		else if (permLevel == -1) return ResponseCode.INTERNAL_ERROR;
		Permissions permissions = new Permissions(permLevel);

		if (!permissions.set().contains(Permissions.Flag.CAN_MODIFY_LISTS)) {release(); return ResponseCode.INSUFFICIENT_PERMISSIONS;}
		
		int affected = 0;
		version = System.currentTimeMillis();
		try {
			affected = update("INSERT INTO HouseholdShoppingList (HouseholdId, Name, Timestamp) VALUES (?, ?, ?);", 
					new SQLParam(householdID, SQLType.INT),
					new SQLParam(listName, SQLType.VARCHAR),
					new SQLParam(version, SQLType.LONG));
			
		} catch (SQLException e) {
			rollback();
			release();
			return ResponseCode.INTERNAL_ERROR;
		}
		if (affected == 0) {rollback(); release(); return ResponseCode.INTERNAL_ERROR;}
		ResultSet results = null;
		try {
			results = query("SELECT LAST_INSERT_ID() AS lastID;");
			if (results == null || !results.next()) {rollback(); release(); return ResponseCode.INTERNAL_ERROR;}
			listID = results.getInt(1);
		}catch (SQLException e) {
			rollback(); release();
			return ResponseCode.INTERNAL_ERROR;
		} 
		release();
		return ResponseCode.CREATED;
		
		
		
		
	}
}
