package sql.wrappers;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;

import core.Permissions;
import sql.SQLExecutable;
import sql.SQLParam;
import sql.SQLType;

public class ListFetchWrapper extends SQLExecutable {
	
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
	
	public static enum ListFetchResults {
		OK,
		NOT_MODIFIED,
		INTERNAL_ERROR,
		INSUFFICIENT_PERMISSIONS
	}
	
	public static class ListFetchItemsJSON {
		@Expose(serialize = true)		
		public final String UPC;
		@Expose(serialize = true)
		public final String description;
		@Expose(serialize = true)		
		public final int quantity;
		@Expose(serialize = true)		
		public final int fractional;
		@Expose(serialize = true)
		public final String unitName;		
		public ListFetchItemsJSON (String unitName, String UPC, String description, int quantity, int fractional) {
			this.unitName = unitName;
			this.UPC = UPC;
			this.description = description;
			this.quantity = quantity;
			this.fractional = fractional;
		}
	}
	
	public ListFetchResults fetch() {
		Permissions permissions = getPermissions();
		if (permissions == null) {return ListFetchResults.INTERNAL_ERROR;}
		if (!permissions.set().contains(Permissions.Flag.CAN_READ_LISTS)) { release(); return ListFetchResults.INSUFFICIENT_PERMISSIONS;}
		int modresult = isModified(timestamp);
		if (modresult == -1) return ListFetchResults.INTERNAL_ERROR;
		else if (modresult == 0) return ListFetchResults.NOT_MODIFIED;
		else {
			if (!selectAll()) return ListFetchResults.INTERNAL_ERROR;
			release();
			return ListFetchResults.OK;
		}
		
	}
	
	private Permissions getPermissions() {
		ResultSet results = null;
		try{
			results = query("SELECT PermissionLevel FROM HouseholdPermissions WHERE UserId=? AND HouseholdId=?;",
					new SQLParam(userID, SQLType.INT),
					new SQLParam(householdID, SQLType.INT));

		} catch (SQLException e) {
			release();
			return null;
		}
		int permissionRaw = 0;
		try {
			if (results == null || !results.next()) { release(); return null;}
			permissionRaw = results.getInt(1);
		} catch (SQLException e) {
			release();
			return null;
		}finally {
			release(results);
		}
		Permissions p = new Permissions(permissionRaw);
		return p;
	}
	
	private int isModified(long since) {
		ResultSet results = null;
		long dbstamp = 0;
		try {
			results = query("SELECT Timestamp, Name FROM HouseholdShoppingList WHERE ListId=?;",
					new SQLParam(listID, SQLType.INT));
			if (results == null) {release(); return -1;}
			if (!results.next()) {release(results); release(); return -1;}
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
			results = query("SELECT InventoryItem.UPC, InventoryItem.Description, InventoryItem.UnitName, ShoppingListItem.Quantity "
					+ "FROM ShoppingListItem INNER JOIN InventoryItem ON (ShoppingListItem.ItemId=InventoryItem.ItemId) "
					+ "WHERE (ShoppingListItem.ListId=?);",
					new SQLParam(listID, SQLType.INT));
			if (results == null) {release(); return false;}
			
			items = new ArrayList<ListFetchItemsJSON>();
			String UPC, description, unitName;
			int quantity, fractional, temp;
			while (results.next()) {
				UPC = results.getString(1);
				description = results.getString(2);
				unitName=  results.getString(3);
				temp = results.getInt(4);
				quantity = (temp / 100);
				fractional = temp - quantity * 100;
				items.add(new ListFetchItemsJSON(unitName, UPC, description, quantity, fractional));
			}
		} catch (SQLException e) {
			release();
			return false;
		}
		return true;
	}
	
	public long getTimestamp() {
		return timestamp;
	}
}
