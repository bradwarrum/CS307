package sql.wrappers;

import java.sql.ResultSet;
import java.sql.SQLException;

import sql.SQLType;

import java.util.List;

import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;

import core.Permissions;
import routes.ListUpdateRoute.ListUpdateItemJSON;
import sql.SQLExecutable;
import sql.SQLParam;

public class ListUpdateWrapper extends SQLExecutable {
	private int userID, householdID, listID;
	@Expose(serialize = true)
	@SerializedName("version")
	private long timestamp;
	private List<ListUpdateItemJSON> items;

	public static enum ListUpdateResult {
		OK,
		INTERNAL_ERROR,
		INSUFFICIENT_PERMISSIONS,
		OUTDATED_INFORMATION,
		LIST_NOT_FOUND,
		ITEM_NOT_FOUND
	}

	public ListUpdateWrapper(int userID, int householdID, int listID, long timestamp, List<ListUpdateItemJSON> items) {
		this.userID = userID;
		this.householdID = householdID;
		this.listID = listID;
		this.timestamp = timestamp;
		this.items = items;
	}

	public ListUpdateResult update() {
		// 1) Read permissions and ensure the user ID can modify lists
		int permissionsraw = getPermissions();
		if (permissionsraw == -1) {return ListUpdateResult.INTERNAL_ERROR;}
		else if (permissionsraw == -2) {return ListUpdateResult.LIST_NOT_FOUND;}
		Permissions permissions = new Permissions(permissionsraw);
		if (!permissions.set().contains(Permissions.Flag.CAN_MODIFY_LISTS)) {release(); return ListUpdateResult.INSUFFICIENT_PERMISSIONS;}
		// 2) Read the household record and ensure the timestamp has not been updated
		long DBtimestamp = readAndLockTimestamp();
		if (DBtimestamp == -1) {release(); return ListUpdateResult.INTERNAL_ERROR;}
		else if (DBtimestamp == -2) {release(); return ListUpdateResult.INSUFFICIENT_PERMISSIONS;}
		if (timestamp != DBtimestamp ) {release(); return ListUpdateResult.OUTDATED_INFORMATION;}
		// 3) Update all the entries in the table
		if (!updateRows()) {return ListUpdateResult.ITEM_NOT_FOUND;}

		// 4) Write the timestamp to the household
		long newstamp = writeTimestamp();
		if (newstamp == -1) {return ListUpdateResult.INTERNAL_ERROR;}
		release();
		timestamp = newstamp;
		return ListUpdateResult.OK;

	}

	private int getPermissions() {
		ResultSet results = null;
		try{
			results = query("SELECT PermissionLevel FROM HouseholdPermissions WHERE UserId=? AND HouseholdId=?;",
					new SQLParam(userID, SQLType.INT),
					new SQLParam(householdID, SQLType.INT));

		} catch (SQLException e) {
			release();
			return -1;
		}
		int permissionRaw = 0;
		try {
			if (results == null) { release(); return -1;}
			if	(!results.next()) { release(results); return -2;}
			permissionRaw = results.getInt(1);
		} catch (SQLException e) {
			release();
			return -1;
		}finally {
			release(results);
		}
		return permissionRaw;
	}

	private long readAndLockTimestamp() {
		long stamp = -1;
		ResultSet results = null;
		try {
			results = query ("SELECT Timestamp FROM HouseholdShoppingList WHERE ListID=? AND HouseholdId=? FOR UPDATE;",
					new SQLParam(listID, SQLType.INT),
					new SQLParam(householdID, SQLType.INT));
			if (results == null) {release(); return -1;}
			if (!results.next()) {release(results); release(); return -2;}
			stamp = results.getLong(1);
		} catch (SQLException e) {
			release(results);
			rollback();
			release();
			return -1;
		}
		return stamp;
	}

	private boolean updateRows() {
		int updated = 0;
		int fail;
		SQLParam listidp = new SQLParam(listID, SQLType.INT);
		SQLParam houseidp = new SQLParam(householdID, SQLType.INT);

		ResultSet results= null;
		try {
			for (ListUpdateItemJSON item : items) {
				results = query("SELECT ItemId FROM InventoryItem WHERE (UPC=? AND HouseholdId=?);",
						new SQLParam(item.UPC, SQLType.VARCHAR),
						houseidp);
				if (results == null || !results.next()) {release(results); rollback(); release(); return false;}
				int itemID = results.getInt(1);
				SQLParam itemidp = new SQLParam(itemID, SQLType.INT);
				SQLParam quantityp = new SQLParam(item.quantity * 100 + item.fractional, SQLType.INT);
				if (item.quantity == 0 && item.fractional == 0) {
					fail = update("DELETE FROM ShoppingListItem WHERE (ListId=? AND ItemId=?);",
							listidp,
							itemidp);
				} else {
					fail = update("INSERT INTO ShoppingListItem (ListId, ItemId, Quantity) VALUES (?, ?, ?)"
							+ " ON DUPLICATE KEY UPDATE Quantity=?;",
							listidp,
							itemidp,
							quantityp,
							quantityp);
				}
				if (fail <= 0) {release(results); rollback(); release(); return false;}
				updated += 1;
				release(results);
			}
		} catch (SQLException e) {
			release(results);
			rollback();
			release();
			return false;
		}
		release(results);
		if (updated != items.size()) {rollback(); release(); return false;}
		return true;
	}

	private long writeTimestamp() {
		long stamp = System.currentTimeMillis();
		int affected = -1;
		try {
			affected = update("UPDATE HouseholdShoppingList SET Timestamp=? WHERE ListId=?;",
					new SQLParam(stamp, SQLType.LONG),
					new SQLParam(listID, SQLType.INT));
		} catch (SQLException e) {
			rollback();
			release();
			return -1;
		}
		if (affected <= 0) {rollback(); release(); return -1;}
		return stamp;
	}
	
	public long getTimestamp() {
		return timestamp;
	}
}
