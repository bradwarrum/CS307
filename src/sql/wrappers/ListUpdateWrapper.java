package sql.wrappers;

import java.sql.ResultSet;
import java.sql.SQLException;
import sql.SQLType;
import java.util.List;

import core.Permissions;
import routes.ListUpdateRoute.ListUpdateItemJSON;
import sql.SQLExecutable;
import sql.SQLParam;

public class ListUpdateWrapper extends SQLExecutable {
	private int userID, householdID, listID;
	private long timestamp;
	private List<ListUpdateItemJSON> items;
	
	public static enum ListUpdateResult {
		OK,
		INTERNAL_ERROR,
		INSUFFICIENT_PERMISSIONS,
		OUTDATED_INFORMATION
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
		Permissions permissions= getPermissions();
		if (permissions == null) {return ListUpdateResult.INTERNAL_ERROR;}
		if (!permissions.set().contains(Permissions.Flag.CAN_MODIFY_LISTS)) {release(); return ListUpdateResult.INSUFFICIENT_PERMISSIONS;}
		// 2) Read the household record and ensure the timestamp has not been updated
		long DBtimestamp = readAndLockTimestamp();
		if (DBtimestamp < 0) {release(); return ListUpdateResult.INTERNAL_ERROR;}
		if (timestamp != DBtimestamp ) {release(); return ListUpdateResult.OUTDATED_INFORMATION;}
		// 3) Update all the entries in the table
		if (!updateRows()) {return ListUpdateResult.INTERNAL_ERROR;}
		
		// 4) Write the timestamp to the household
		long newstamp = writeTimestamp();
		if (newstamp == -1) {return ListUpdateResult.INTERNAL_ERROR;}
		release();
		return ListUpdateResult.OK;
		
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
	
	private long readAndLockTimestamp() {
		long stamp = -1;
		ResultSet results = null;
		try {
			results = query ("SELECT Timestamp FROM HouseholdShoppingList WHERE ListID=? FOR UPDATE;",
					new SQLParam(listID, SQLType.INT));
			if (results == null) {release(); return -1;}
			if (!results.next()) {release(results); release(); return -1;}
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
		try {
			for (ListUpdateItemJSON item : items) {
				fail = update("UPDATE ShoppingListItem SET Quantity=? WHERE ListId=? AND UPC=?;",
						new SQLParam(item.quantity, SQLType.INT),
						listidp, 
						new SQLParam(item.UPC, SQLType.VARCHAR));
				if (fail < 0) {rollback(); release(); return false;}
				updated += fail;
			}
		} catch (SQLException e) {
			rollback();
			release();
			return false;
		}
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
}
