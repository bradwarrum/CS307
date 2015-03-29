package sql.wrappers;

import java.sql.SQLException;

import core.Permissions;
import core.ResponseCode;
import sql.SQLParam;
import sql.SQLType;

public class ListDeleteWrapper extends BaseWrapper {
	private int userID, listID, householdID;
	
	public ListDeleteWrapper(int userID, int listID, int householdID) {
		this.userID = userID;
		this.listID = listID;
		this.householdID = householdID;
	}
	
	
	public ResponseCode delete() {
		//Check permissions
		int permraw = getPermissions(userID, householdID);
		if (permraw == -1) {return ResponseCode.INTERNAL_ERROR;}
		else if (permraw == -2) {return ResponseCode.HOUSEHOLD_NOT_FOUND;}
		Permissions perm = new Permissions(permraw);
		if (!perm.has(Permissions.Flag.CAN_MODIFY_LISTS)) {return ResponseCode.INSUFFICIENT_PERMISSIONS;}
		//Remove the shopping list and check that the list belongs to the household
		ResponseCode res = deleteList();
		if (res != ResponseCode.OK) return res;
		//Remove the individual items belonging to the shopping list
		if (!deleteItems()) return ResponseCode.INTERNAL_ERROR;
		
		release();
		return ResponseCode.OK;
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
	
	private ResponseCode deleteList() {
		int affected = -1;
		try {
			affected = update("DELETE FROM HouseholdShoppingList WHERE HouseholdId=? AND ListId=?;",
					new SQLParam(householdID, SQLType.INT),
					new SQLParam(listID, SQLType.INT));
		} catch (SQLException e) {
			rollback();
			release();
			return ResponseCode.INTERNAL_ERROR;
		}
		if (affected < 0) {rollback(); release(); return ResponseCode.INTERNAL_ERROR;}
		else if (affected == 0) {rollback(); release(); return ResponseCode.LIST_NOT_FOUND;}
		return ResponseCode.OK;
	}
}
