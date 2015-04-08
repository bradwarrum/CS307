package sql.wrappers;

import java.sql.ResultSet;
import java.sql.SQLException;

import com.google.gson.annotations.Expose;

import core.Barcode.Format;
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
	@Expose(serialize = true)
	private String UPC;

	public UPCLinkWrapper(int userID, int householdID, Barcode barcode, String description, String unitName, float size, String packageName) {
		this.userID = userID;
		this.householdID = householdID;
		this.barcode = barcode;
		this.description = description;
		this.unitName = unitName;
		this.size = size;
		this.packageName = packageName;
	}

	public UPCLinkWrapper(int userID, int householdID, String description, String unitName, float size, String packageName) {
		this(userID, householdID, null, description, unitName, size, packageName);
	}

	public ResponseCode link() {
		int permissionraw = getPermissions(userID, householdID);
		if (permissionraw == -1) return ResponseCode.INTERNAL_ERROR;
		else if (permissionraw == -2) return ResponseCode.HOUSEHOLD_NOT_FOUND;
		Permissions permissions = new Permissions(permissionraw);
		if (!permissions.has(Permissions.Flag.CAN_MODIFY_INVENTORY)) {release(); return ResponseCode.INSUFFICIENT_PERMISSIONS;}

		SQLParam householdParam = new SQLParam(householdID, SQLType.INT);
		if (barcode == null) {
			//Fetch the next available UPC from the household table
			ResultSet results = null;
			int affected = -1;
			try {
				affected = update("UPDATE Household SET AvailableProduceID=AvailableProduceID + 1;");
				if (affected <= 0) {rollback(); release(results); release(); return ResponseCode.HOUSEHOLD_NOT_FOUND;}
				results = query("SELECT AvailableProduceID FROM Household WHERE HouseholdId=?;",
						householdParam);
				if (results == null || !results.next()) {rollback(); release(results); release(); return ResponseCode.HOUSEHOLD_NOT_FOUND;}
				int produceUPC = results.getInt(1) - 1;
				UPC = String.format("%05d", produceUPC);
				barcode = new Barcode(UPC);
				if (barcode.getFormat() != Format.PRODUCE_5){rollback(); release(results); release(); return ResponseCode.INTERNAL_ERROR;}
			} catch (SQLException e) {
				rollback();
				release(results);
				release();
				return ResponseCode.INTERNAL_ERROR;
			}
		}


		try {
			update("INSERT INTO InventoryItem (UPC, HouseholdId, Description, PackageQuantity, PackageUnits, PackageName, InventoryQuantity, Hidden) VALUES (?, ?, ?, ?, ?, ?, ?, ?)"
					+ "ON DUPLICATE KEY UPDATE Description=VALUES(Description), PackageQuantity=VALUES(PackageQuantity), PackageName=VALUES(PackageName), Hidden=VALUES(Hidden);",
					new SQLParam(barcode.toString(), SQLType.VARCHAR),
					householdParam,
					new SQLParam(description, SQLType.VARCHAR),
					new SQLParam(size, SQLType.FLOAT),
					new SQLParam(unitName, SQLType.VARCHAR),
					new SQLParam(packageName, SQLType.VARCHAR),
					new SQLParam(0, SQLType.INT),
					SQLParam.SQLFALSE);
		} catch (SQLException e) {
			rollback();
			release();
			return ResponseCode.INTERNAL_ERROR;
		}
		release();
		return ResponseCode.OK;
	}
}
