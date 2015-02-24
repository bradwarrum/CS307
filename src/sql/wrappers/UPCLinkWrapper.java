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
	private String unitName;
	
	public static enum UPCLinkResult {
		OK,
		INTERNAL_ERROR,
		HOUSEHOLD_NOT_FOUND,
		INSUFFICIENT_PERMISSIONS
	}
	public UPCLinkWrapper(int userID, int householdID, Barcode barcode, String description, String unitName) {
		this.userID = userID;
		this.householdID = householdID;
		this.barcode = barcode;
		this.description = description;
		this.unitName = unitName;
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
		
		try {
			update("INSERT INTO InventoryItem (UPC, HouseholdId, Description, UnitQuantity, UnitName, Hidden) VALUES (?, ?, ?, ?, ?, ?)"
							+ "ON DUPLICATE KEY UPDATE Description=VALUES(Description), UnitName=VALUES(UnitName);",
					new SQLParam(barcode.toString(), SQLType.VARCHAR),
					new SQLParam(householdID, SQLType.INT),
					new SQLParam(description, SQLType.VARCHAR),
					new SQLParam(0f,SQLType.FLOAT),
					new SQLParam(unitName, SQLType.VARCHAR),
					new SQLParam(0, SQLType.BYTE));
		} catch (SQLException e) {
			rollback();
			return UPCLinkResult.INTERNAL_ERROR;
		} finally {
			release();
		}
		return UPCLinkResult.OK;
	}
}
