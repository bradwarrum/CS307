package sql.wrappers;

import java.sql.SQLException;

import core.Permissions;
import core.Barcode;
import core.ResponseCode;
import sql.SQLParam;
import sql.SQLType;


public class UPCLinkWrapper extends BaseWrapper {

	private int userID, householdID,unitName,packageName;
	private Barcode barcode;
	private String description;
	private float size;
	
	public UPCLinkWrapper(int userID, int householdID, Barcode barcode, String description, int unitName, float size, int packageName) {
		this.userID = userID;
		this.householdID = householdID;
		this.barcode = barcode;
		this.description = description;
		this.unitName = unitName;
		this.size = size;
		this.packageName = packageName;
	}
	
	public ResponseCode link() {
		int permissionraw = getPermissions(userID, householdID);
		if (permissionraw == -1) return ResponseCode.INTERNAL_ERROR;
		else if (permissionraw == -2) return ResponseCode.HOUSEHOLD_NOT_FOUND;
		Permissions permissions = new Permissions(permissionraw);
		if (!permissions.has(Permissions.Flag.CAN_MODIFY_INVENTORY)) {release(); return ResponseCode.INSUFFICIENT_PERMISSIONS;}
		
		try {
			update("INSERT INTO InventoryItem (UPC, HouseholdId, Description, PackageQuantity, PackageUnits, PackageName, InventoryQuantity, Hidden) VALUES (?, ?, ?, ?, ?, ?, ?, ?)"
							+ "ON DUPLICATE KEY UPDATE Description=VALUES(Description), PackageQuantity=VALUES(PackageQuantity), PackageName=VALUES(PackageName), Hidden=VALUES(Hidden);",
					new SQLParam(barcode.toString(), SQLType.VARCHAR),
					new SQLParam(householdID, SQLType.INT),
					new SQLParam(description, SQLType.VARCHAR),
					new SQLParam(size, SQLType.FLOAT),
					new SQLParam(unitName, SQLType.INT),
					new SQLParam(packageName, SQLType.INT),
					new SQLParam(0, SQLType.INT),
					SQLParam.SQLFALSE);
		} catch (SQLException e) {
			rollback();
			return ResponseCode.INTERNAL_ERROR;
		} finally {
			release();
		}
		return ResponseCode.OK;
	}
}
