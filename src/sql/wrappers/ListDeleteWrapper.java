package sql.wrappers;

import java.sql.ResultSet;
import java.sql.SQLException;

import core.Permissions;
import sql.SQLExecutable;
import sql.SQLParam;
import sql.SQLType;

public class ListDeleteWrapper extends SQLExecutable {
	private int userID, listID, householdID;
	
	public ListDeleteWrapper(int userID, int listID, int householdID) {
		this.userID = userID;
		this.listID = listID;
		this.householdID = householdID;
	}
	
	public static enum ListDeleteResult {
		OK,
		INTERNAL_ERROR,
		INSUFFICIENT_PERMISSIONS,
		LIST_NOT_FOUND
	}
	
	public ListDeleteResult delete() {
		//Check permissions
		int permraw = getPermissions();
		if (permraw == -1) {return ListDeleteResult.INTERNAL_ERROR;}
		else if (permraw == -2) {return ListDeleteResult.LIST_NOT_FOUND;}
		Permissions perm = new Permissions(permraw);
		if (!perm.set().contains(Permissions.Flag.CAN_MODIFY_LISTS)) {return ListDeleteResult.INSUFFICIENT_PERMISSIONS;}
		//Remove the shopping list and check that the list belongs to the household
		ListDeleteResult res = deleteList();
		if (res != ListDeleteResult.OK) return res;
		//Remove the individual items belonging to the shopping list
		if (!deleteItems()) return ListDeleteResult.INTERNAL_ERROR;
		
		release();
		return ListDeleteResult.OK;
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
	
	private boolean deleteItems() {
		int affected = -1;
		try {
			affected = update("DELETE FROM ShoppingListItem WHERE ListId=?;",
					new SQLParam(listID, SQLType.INT));
		} catch (SQLException e) {
			rollback();
			release();
			return false;
		}
		if (affected < 0) {rollback(); release(); return false;}
		return true;
	}
	
	private ListDeleteResult deleteList() {
		int affected = -1;
		try {
			affected = update("DELETE FROM HouseholdShoppingList WHERE HouseholdId=? AND ListId=?;",
					new SQLParam(householdID, SQLType.INT),
					new SQLParam(listID, SQLType.INT));
		} catch (SQLException e) {
			rollback();
			release();
			return ListDeleteResult.INTERNAL_ERROR;
		}
		if (affected < 0) {rollback(); release(); return ListDeleteResult.INTERNAL_ERROR;}
		else if (affected == 0) {rollback(); release(); return ListDeleteResult.LIST_NOT_FOUND;}
		return ListDeleteResult.OK;
	}
}
