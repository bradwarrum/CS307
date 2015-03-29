package sql.wrappers;

import java.sql.SQLException;

import core.Permissions;
import core.Barcode;
import core.ResponseCode;
import sql.SQLParam;
import sql.SQLType;


public class UPCLinkWrapper extends BaseWrapper {

	private int userID, householdID;
	private Barcode barcode;
	private String description;
	private String unitName;
	private float size;
	private String packageName;
	
	public UPCLinkWrapper(int userID, int householdID, Barcode barcode, String description, String unitName, float size, String packageName) {
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
							+ "ON DUPLICATE KEY UPDATE Description=VALUES(Description), PackageQuantity=VALUES(PackageQuantity), PackageName=VALUES(PackageName);",
					new SQLParam(barcode.toString(), SQLType.VARCHAR),
					new SQLParam(householdID, SQLType.INT),
					new SQLParam(description, SQLType.VARCHAR),
					new SQLParam(size, SQLType.FLOAT),
					new SQLParam(unitName, SQLType.VARCHAR),
					new SQLParam(packageName, SQLType.VARCHAR),
					new SQLParam(0, SQLType.INT),
					new SQLParam(0, SQLType.BYTE));
		} catch (SQLException e) {
			rollback();
			return ResponseCode.INTERNAL_ERROR;
		} finally {
			release();
		}
		return ResponseCode.OK;
	}
}
