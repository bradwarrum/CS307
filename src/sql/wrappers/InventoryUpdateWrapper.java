package sql.wrappers;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.List;

import com.google.gson.annotations.Expose;

import core.Permissions;
import core.ResponseCode;
import routes.InventoryUpdateRoute.InventoryUpdateItemJSON;
import sql.SQLParam;
import sql.SQLType;

public class InventoryUpdateWrapper extends BaseWrapper {

	private int userID, householdID;
	@Expose(serialize = true)	
	private long version;
	private List<InventoryUpdateItemJSON> items;

	public InventoryUpdateWrapper(int userID, int householdID, long version, List<InventoryUpdateItemJSON> items) {
		this.userID = userID;
		this.householdID = householdID;
		this.version = version;
		this.items = items;
	}

	public ResponseCode update() {
		//Check that the user can modify inventory
		int permLevel = getPermissions(userID, householdID);
		if (permLevel == -1) return ResponseCode.INTERNAL_ERROR;
		else if (permLevel == -2) return ResponseCode.HOUSEHOLD_NOT_FOUND;
		Permissions permissions = new Permissions(permLevel);
		if (!permissions.has(Permissions.Flag.CAN_MODIFY_INVENTORY)) return ResponseCode.INSUFFICIENT_PERMISSIONS;

		//Check that the version passed by the client matches the version in the database
		long dbversion = readAndLockVersion();
		if (dbversion == -1) return ResponseCode.INTERNAL_ERROR;
		else if (dbversion == -2) return ResponseCode.HOUSEHOLD_NOT_FOUND;
		else if (dbversion != version) {rollback(); release(); return ResponseCode.OUTDATED_TIMESTAMP;}

		//If the version matches, update the inventory
		int updatecnt = updateRows();
		if (updatecnt == -2) return ResponseCode.ITEM_NOT_FOUND;
		else if (updatecnt == -1) return ResponseCode.INTERNAL_ERROR;
		
		
		//Write the new timestamp to the household and close the connection
		if ((version = writeVersion()) < 0) return ResponseCode.INTERNAL_ERROR;
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

	/**
	 * Update rows with the new inventory values.<br>
	 * If the update returns any error, the database is rolled back and the connection is released.
	 * @return Returns -2 if an update call did not change any records. <br>
	 * Returns -1 if there was a general internal server error. <br>
	 * Otherwise returns the number of rows updated.
	 */
	private int updateRows() {
		SQLParam householdParam = new SQLParam(householdID);
		for (InventoryUpdateItemJSON item : items) {
			try {
				int affected = update("UPDATE InventoryItem SET InventoryQuantity=? WHERE HouseholdId=? AND UPC=?;",
						new SQLParam(item.fractional + item.quantity * 100, SQLType.INT),
						householdParam,
						new SQLParam(item.UPC, SQLType.VARCHAR));
				if (affected == 0) {rollback(); release(); return -2;}
				else if (affected < 0) {rollback(); release(); return -1;}
			} catch (SQLException e) {
				rollback();
				release();
				return -1;
			}
		}
		return items.size();
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
	
	public long getVersion() {
		return version;
	}

}
