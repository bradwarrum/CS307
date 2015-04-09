package sql.wrappers;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;

import core.Permissions;
import core.ResponseCode;
import sql.SQLParam;
import sql.SQLType;
import sql.wrappers.InventoryFetchWrapper.PackagingJSON;

public class ListFetchWrapper extends BaseWrapper {
	
	private int userID, householdID, listID;
	@Expose(serialize = true)
	@SerializedName("version")
	private long timestamp;
	@Expose(serialize = true)
	private String name;
	@Expose(serialize = true)
	private List<ListFetchItemsJSON> items = null;

	//private ListFetchItemsJSON[] items;
	
	public ListFetchWrapper(int userID, int householdID, int listID, long timestamp) {
		this.userID = userID;
		this.householdID = householdID;
		this.listID = listID;
		this.timestamp = timestamp;
	}
	
	
	public static class ListFetchItemsJSON {
		@Expose(serialize = true)		
		public final String UPC;
		@Expose(serialize = true)
		public final boolean isInternalUPC;
		@Expose(serialize = true)
		public final String description;
		@Expose(serialize = true)		
		public final int quantity;
		@Expose(serialize = true)
		public final PackagingJSON packaging;

		public ListFetchItemsJSON (String UPC, String description, int quantity, boolean isInternalUPC, PackagingJSON packaging) {
			this.packaging = packaging;
			this.UPC = UPC;
			this.description = description;
			this.quantity = quantity;
			this.isInternalUPC = isInternalUPC;
		}
	}
	
	public ResponseCode fetch() {
		int permissionsraw = getPermissions(userID, householdID);
		if (permissionsraw == -1) return ResponseCode.INTERNAL_ERROR;
		else if (permissionsraw == -2) return ResponseCode.HOUSEHOLD_NOT_FOUND;
		Permissions permissions = new Permissions(permissionsraw);
		if (!permissions.has(Permissions.Flag.CAN_READ_LISTS)) { release(); return ResponseCode.INSUFFICIENT_PERMISSIONS;}
		
		int modresult = isModified(timestamp);
		if (modresult == -1) return ResponseCode.INTERNAL_ERROR;
		else if (modresult == -2) return ResponseCode.LIST_NOT_FOUND;
		else if (modresult == 0) return ResponseCode.NOT_MODIFIED;
		else {
			if (!selectAll()) return ResponseCode.INTERNAL_ERROR;
			release();
			return ResponseCode.OK;
		}
		
	}
	
	private int isModified(long since) {
		ResultSet results = null;
		long dbstamp = 0;
		try {
			results = query("SELECT Timestamp, Name FROM HouseholdShoppingList WHERE ListId=? AND HouseholdId=?;",
					new SQLParam(listID, SQLType.INT),
					new SQLParam(householdID, SQLType.INT));
			if (results == null) {release(); return -1;}
			if (!results.next()) {release(results); release(); return -2;}
			dbstamp = results.getLong(1);
			name = results.getString(2);
		} catch (SQLException e) {
			release(results);
			release();
			return -1;
		}
		if (dbstamp != since) {timestamp = dbstamp; return 1;}
		return 0;
		
	}
	
	private boolean selectAll() {
		ResultSet results = null;
		try {
			results = query("SELECT I.UPC, I.Description, I.PackageQuantity, I.PackageUnits, M.UnitName, M.UnitAbbreviation, I.PackageName, S.Quantity "
					+ "FROM ShoppingListItem S INNER JOIN InventoryItem I ON (S.ItemId=I.ItemId) "
					+ "INNER JOIN MeasurementUnit M ON (M.UnitId=I.PackageUnits) "
					+ "WHERE (S.ListId=?);",
					new SQLParam(listID, SQLType.INT));
			if (results == null) {release(); return false;}
			
			items = new ArrayList<ListFetchItemsJSON>();
			String UPC, description, packageName, unitName, unitAbbrev;
			float unitQuantity;
			int quantity, unitID;
			while (results.next()) {
				UPC = results.getString(1);
				description = results.getString(2);
				unitQuantity = results.getInt(3);
				unitID = results.getInt(4);
				unitName = results.getString(5);
				unitAbbrev = results.getString(6);
				packageName = results.getString(7);
				quantity = results.getInt(8);
				PackagingJSON p = new PackagingJSON(unitQuantity, unitID, unitName, unitAbbrev, packageName);
				items.add(new ListFetchItemsJSON(UPC, description, quantity, UPC.length() == 5, p));
			}
		} catch (SQLException e) {
			release();
			return false;
		} finally {
			release(results);
		}
		return true;
	}
	
	public long getTimestamp() {
		return timestamp;
	}
}
