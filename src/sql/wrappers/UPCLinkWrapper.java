package sql.wrappers;

import java.sql.ResultSet;
import java.sql.SQLException;

import core.Permissions;
import core.Barcode;
import sql.SQLExecutable;
import sql.SQLParam;
import sql.SQLType;


public class UPCLinkWrapper extends SQLExecutable {

	private int userID, householdID;
	private Barcode barcode;
	private String description;
	
	public static enum UPCLinkResult {
		CREATED,
		INTERNAL_ERROR,
		HOUSEHOLD_NOT_FOUND,
		INSUFFICIENT_PERMISSIONS
	}
	public UPCLinkWrapper(int userID, int householdID, Barcode barcode, String description) {
		this.userID = userID;
		this.householdID = householdID;
		this.barcode = barcode;
		this.description = description;
	}
	
	public UPCLinkResult link() {
		ResultSet results = null;
		try {
			results = query("SELECT PermissionLevel FROM HouseholdPermissions WHERE UserId = ? AND HouseholdId = ?;", 
					new SQLParam(userID, SQLType.INT),
					new SQLParam(householdID, SQLType.INT));
		} catch (SQLException e) {
			release();
			return UPCLinkResult.INTERNAL_ERROR;
		}
		int permissionraw = 0;
		if (results == null) {release(); return UPCLinkResult.INTERNAL_ERROR;}
		try {
			if (!results.next()) {release(results); release(); return UPCLinkResult.HOUSEHOLD_NOT_FOUND;}
			permissionraw = results.getInt("PermissionLevel");
		} catch (SQLException e) {
			release();
			return UPCLinkResult.INTERNAL_ERROR;
		} finally {
			release(results);
		}
		Permissions permissions = new Permissions(permissionraw);
		if (!permissions.set().contains(Permissions.Flag.CAN_MODIFY_INVENTORY)) {release(); return UPCLinkResult.INSUFFICIENT_PERMISSIONS;}
		
		int affected = 0;
		try {
			affected = update("INSERT INTO InventoryItem (UPC, HouseholdId, Description, UnitQuantity, UnitId) VALUES (?, ?, ?, ?, ?);",
					new SQLParam(barcode.toString(), SQLType.VARCHAR),
					new SQLParam(householdID, SQLType.INT),
					new SQLParam(description, SQLType.VARCHAR));
							//TODO: Insert unit stuff
		}
		
		
		
		
		return UPCLinkResult.CREATED;
	}
}
