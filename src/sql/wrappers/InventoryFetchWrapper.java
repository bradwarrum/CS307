package sql.wrappers;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;

import sql.SQLParam;
import sql.SQLType;
import core.Permissions;
import core.ResponseCode;

public class InventoryFetchWrapper extends BaseWrapper {

	int userID, householdID;
	@Expose(serialize = true)
	@SerializedName("version")
	long etag;
	@Expose(serialize = true)
	List<InventoryFetchResponseItemJSON> items;
	public InventoryFetchWrapper (int userID, int householdID, long etag) {
		this.userID = userID;
		this.householdID = householdID;
		this.etag = etag;
	}
	
	private static class InventoryFetchResponseItemJSON {
		@Expose(serialize = true)
		private String UPC;
		@Expose(serialize = true)
		private boolean isInternalUPC;
		@Expose(serialize = true)
		private String description;
		@Expose(serialize = true)
		private float packageSize;
		@Expose(serialize = true)
		private String packageUnits;
		@Expose(serialize = true)
		private String packageName;
		@Expose(serialize = true)
		private int quantity;
		@Expose(serialize = true)
		private int fractional;
		
		public InventoryFetchResponseItemJSON (String UPC, boolean isInternalUPC, String description, float packageSize, String packageUnits, String packageName, int quantity, int fractional) {
			this.UPC = UPC;
			this.isInternalUPC = isInternalUPC;
			this.description = description;
			this.packageSize = packageSize;
			this.packageUnits = packageUnits;
			this.packageName = packageName;
			this.quantity = quantity;
			this.fractional = fractional;
		}
	}
	
	public ResponseCode fetch() {
		int permLevel = getPermissions(userID, householdID);
		if (permLevel == -1) return ResponseCode.INTERNAL_ERROR;
		else if (permLevel == -2) return ResponseCode.HOUSEHOLD_NOT_FOUND;
		
		Permissions permissions = new Permissions(permLevel);
		if (!permissions.has(Permissions.Flag.CAN_READ_INVENTORY)) return ResponseCode.INSUFFICIENT_PERMISSIONS;
		
		//Fetch the version number of the household
		SQLParam householdParam = new SQLParam(householdID, SQLType.INT);
		ResultSet results = null;
		try {
			results = query("SELECT Version FROM Household WHERE HouseholdId=?;",
					householdParam);
			if (results == null || !results.next()) {release(results); release(); return ResponseCode.INTERNAL_ERROR;}
			long dbversion = results.getLong(1);
			if (dbversion == this.etag) 
			{
				release(results); release(); return ResponseCode.NOT_MODIFIED;
			} else 
				this.etag = dbversion;
		} catch (SQLException e) {
			release(results);
			release();
			return ResponseCode.INTERNAL_ERROR;
		}
		items = new ArrayList<InventoryFetchResponseItemJSON> ();
		try {
			results = query("SELECT UPC, Description, PackageQuantity, PackageUnits, PackageName, InventoryQuantity FROM InventoryItem "
					+ "WHERE HouseholdId=? AND Hidden=?;",
					householdParam,
					SQLParam.SQLFALSE);
			if (results == null) { release(results); release(); return ResponseCode.INTERNAL_ERROR;}
			while (results.next()) {
				int temp = results.getInt("InventoryQuantity");
				int quantity = temp / 100;
				int fractional = temp - quantity * 100;
				String UPC = results.getString(1);
				items.add(new InventoryFetchResponseItemJSON(UPC, UPC.length() == 5, results.getString(2), results.getFloat(3), results.getString(4),
						results.getString(5), quantity, fractional));
			}
			release(results);
			release();
			return ResponseCode.OK;
		} catch (SQLException e) {
			release(results);
			release();
			return ResponseCode.INTERNAL_ERROR;
		}

	}
	
	public long getVersion() {
		return etag;
	}
}
