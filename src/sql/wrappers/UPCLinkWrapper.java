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

	private int userID, householdID,unitName;
	private String packageName;
	private Barcode barcode;
	private String description;
	private float size;
	@Expose(serialize = true)
	private String UPC;
	@Expose(serialize = true)
	private long version;
	public UPCLinkWrapper(int userID, int householdID, Barcode barcode, String description, int unitName, float size, String packageName, long version) {
		this.userID = userID;
		this.householdID = householdID;
		this.barcode = barcode;
		this.description = description;
		this.unitName = unitName;
		this.size = size;
		this.packageName = packageName;
		this.version = version;
	}
	public UPCLinkWrapper(int userID, int householdID, String description, int unitName, float size, String packageName, long version) {
		this(userID, householdID, null, description, unitName, size, packageName, version);
	}

	public ResponseCode link() {
		int permissionraw = getPermissions(userID, householdID);
		if (permissionraw == -1) return ResponseCode.INTERNAL_ERROR;
		else if (permissionraw == -2) return ResponseCode.HOUSEHOLD_NOT_FOUND;
		Permissions permissions = new Permissions(permissionraw);
		if (!permissions.has(Permissions.Flag.CAN_MODIFY_INVENTORY)) {release(); return ResponseCode.INSUFFICIENT_PERMISSIONS;}
		
		long serverVersion = readAndLockVersion();
		if (serverVersion == -2) {rollback(); release(); return ResponseCode.HOUSEHOLD_NOT_FOUND;}
		if (serverVersion == -1) {rollback(); release(); return ResponseCode.INTERNAL_ERROR;}
		
		
		if (serverVersion != version) {rollback(); release(); return ResponseCode.OUTDATED_TIMESTAMP;}

		SQLParam householdParam = new SQLParam(householdID, SQLType.INT);
		if (barcode == null) {
			//Fetch the next available UPC from the household table
			ResultSet results = null;
			int affected = -1;
			try {
				// First check to see if there are any items with the exact same name in the household
				results = query("SELECT UPC FROM InventoryItem WHERE (HouseholdId=? AND UPPER(Description)=UPPER(?));",
						householdParam,
						new SQLParam(description, SQLType.VARCHAR));
				if (results == null) {rollback(); release(results); release(); return ResponseCode.INTERNAL_ERROR;}
				if (results.next()) {rollback(); release(results); release(); return ResponseCode.ITEM_DUPLICATE_FOUND;}
				release(results);
			
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
		} else {
			UPC = barcode.toString();
		}


		try {
			update("INSERT INTO InventoryItem (UPC, HouseholdId, Description, PackageQuantity, PackageUnits, PackageName, InventoryQuantity, Hidden) VALUES (?, ?, ?, ?, ?, ?, ?, ?)"
					+ "ON DUPLICATE KEY UPDATE Description=VALUES(Description), PackageQuantity=VALUES(PackageQuantity), PackageName=VALUES(PackageName), PackageUnits=VALUES(PackageUnits), Hidden=VALUES(Hidden);",
					new SQLParam(barcode.toString(), SQLType.VARCHAR),
					householdParam,
					new SQLParam(description, SQLType.VARCHAR),
					new SQLParam(size, SQLType.FLOAT),
					new SQLParam(unitName, SQLType.INT),
					new SQLParam(packageName, SQLType.VARCHAR),
					new SQLParam(0, SQLType.INT),
					SQLParam.SQLFALSE);
		} catch (SQLException e) {
			rollback();
			release();
			return ResponseCode.INTERNAL_ERROR;
		}
		
		version = writeVersion();
		if (version <0) return ResponseCode.INTERNAL_ERROR;
		
		release();
		return ResponseCode.OK;
	}
	
	/**
	 * Reads the version from a household, and performs an exclusive lock on the household version.
	 * @return Returns -1 if there was an issue executing the query. <p>
	 * Returns -2 if there was no information returned (the information does not exist in the database)<p>
	 * Otherwise returns the version for the household
	 */
	private long readAndLockVersion() {
		long stamp = -1;
		ResultSet results = null;
		try {
			results = query ("SELECT Version FROM Household WHERE HouseholdId=? FOR UPDATE;",
					new SQLParam(householdID, SQLType.INT));
			if (results == null) {release(); return -1;}
			if (!results.next()) {release(results); release(); return -2;}
			stamp = results.getLong(1);
		} catch (SQLException e) {
			rollback();
			release();
			return -1;
		} finally {
			release(results);
		}
		return stamp;
	}
	
	private long writeVersion() {
		long stamp = System.currentTimeMillis();
		int affected = -1;
		try {
			affected = update("UPDATE Household SET Version=? WHERE HouseholdId=?;",
					new SQLParam(stamp, SQLType.LONG),
					new SQLParam(householdID, SQLType.INT));
		} catch (SQLException e) {
			rollback();
			release();
			return -1;
		}
		if (affected <= 0) {rollback(); release(); return -1;}
		return stamp;
	}
}
