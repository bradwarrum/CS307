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
import core.json.FetchItemJSON;
import core.json.PackagingJSON;

public class InventoryFetchWrapper extends BaseWrapper {

	int userID, householdID;
	@Expose(serialize = true)
	@SerializedName("version")
	long etag;
	@Expose(serialize = true)
	List<FetchItemJSON> items;
	public InventoryFetchWrapper (int userID, int householdID, long etag) {
		this.userID = userID;
		this.householdID = householdID;
		this.etag = etag;
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
		items = new ArrayList<FetchItemJSON> ();
		try {
			results = query("SELECT I.UPC, I.Description, I.PackageQuantity, I.PackageUnits, M.UnitName, M.UnitAbbreviation, I.PackageName, I.InventoryQuantity FROM InventoryItem I "
					+ "INNER JOIN MeasurementUnit M ON M.UnitId=I.PackageUnits "
					+ "WHERE I.HouseholdId=? AND I.Hidden=?;",
					householdParam,
					SQLParam.SQLFALSE);
			if (results == null) { release(results); release(); return ResponseCode.INTERNAL_ERROR;}
			while (results.next()) {
				String UPC = results.getString(1);
				String description = results.getString(2);
				float pkgQuantity = results.getFloat(3);
				int pkgUnitID = results.getInt(4);
				String pkgUnitName = results.getString(5);
				String pkgUnitAbbrev = results.getString(6);
				String pkgName = results.getString(7);
				int temp = results.getInt(8);
				int quantity = temp / 100;
				int fractional = temp - quantity * 100;
				PackagingJSON packaging = new PackagingJSON(pkgQuantity, pkgUnitID, pkgUnitName, pkgUnitAbbrev, pkgName);
				items.add(new FetchItemJSON(UPC, description, quantity, fractional, UPC.length() == 5,  packaging));
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
