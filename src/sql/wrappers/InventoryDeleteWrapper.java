package sql.wrappers;

import java.sql.ResultSet;
import java.sql.SQLException;

import com.google.gson.annotations.Expose;

import sql.SQLParam;
import sql.SQLType;
import core.Permissions;
import core.ResponseCode;

public class InventoryDeleteWrapper extends BaseWrapper {

	int userID, householdID;
	String UPC;
	@Expose(serialize = true)
	long version;

	public InventoryDeleteWrapper(int userID, int householdID, String UPC) {
		this.userID = userID;
		this.householdID = householdID;
		this.UPC = UPC;
	}

	public ResponseCode delete() {
		int permRaw = getPermissions(userID, householdID);
		if (permRaw == -1) return ResponseCode.INTERNAL_ERROR;
		else if (permRaw == -2) return ResponseCode.HOUSEHOLD_NOT_FOUND;

		Permissions permissions = new Permissions(permRaw);
		//Deleting an inventory item will affect the inventory, lists that contain the item, and recipes that contain it as well
		if (!permissions.has(Permissions.Flag.CAN_MODIFY_INVENTORY, Permissions.Flag.CAN_MODIFY_LISTS, Permissions.Flag.CAN_MODIFY_RECIPES)) {
			return ResponseCode.INSUFFICIENT_PERMISSIONS;
		}

		// 1)Select the inventory item to ensure that it actually exists
		ResultSet results = null;
		SQLParam householdParam = new SQLParam(householdID, SQLType.INT);
		SQLParam upcparam = new SQLParam(UPC, SQLType.VARCHAR);
		int itemID = -1;
		try {
			results = query("SELECT ItemId FROM InventoryItem WHERE HouseholdId=? AND UPC=? AND Hidden=?;",
					householdParam,
					upcparam,
					SQLParam.SQLFALSE);
			if (results == null) {rollback(); release(); return ResponseCode.INTERNAL_ERROR;}
			else if (!results.next()) {rollback(); release(results); release(); return ResponseCode.ITEM_NOT_FOUND;}
			itemID = results.getInt(1);
		} catch (SQLException e) {
			rollback();
			release();
			return ResponseCode.INTERNAL_ERROR;
		} finally {
			release(results);
		}

		int affected = -1;

		version = System.currentTimeMillis();
		SQLParam verParam = new SQLParam(version, SQLType.LONG);
		SQLParam itemIDParam = new SQLParam(itemID, SQLType.INT);


		try {
			// 2) Update each shopping list that has been modified as a result of the deletion with a new version number
			affected = update("UPDATE HouseholdShoppingList SET Timestamp=? WHERE ListId IN (SELECT ListId FROM ShoppingListItem WHERE ItemId=?);",
					verParam,
					itemIDParam);
			if (affected < 0) {rollback(); release(results); release(); return ResponseCode.INTERNAL_ERROR;}

			// 3) Update each recipe that has been modified as a result of the deletion with a new version number
			affected = update("UPDATE HouseholdRecipe SET Timestamp=? WHERE RecipeId IN (SELECT RecipeId FROM RecipeItem WHERE ItemId=?);",
					verParam,
					itemIDParam);
			if (affected < 0) {rollback(); release(results); release(); return ResponseCode.INTERNAL_ERROR;}

			// 4) Delete the shopping list items referencing the hidden inventory item
			affected = update("DELETE FROM ShoppingListItem WHERE ItemId=?;",
					itemIDParam);

			
			// 5) Delete the recipe items referencing the hidden inventory item
			affected = update("DELETE FROM RecipeItem Where ItemId=?;",
					itemIDParam);
			if (affected < 0) {rollback(); release(results); release(); return ResponseCode.INTERNAL_ERROR;}
			
			// 6) Hide the inventory item
			affected = update("UPDATE InventoryItem SET Hidden=?, InventoryQuantity=0 WHERE ItemId=?;",
					SQLParam.SQLTRUE,

					itemIDParam);
			if (affected <= 0) {rollback(); release(results); release(); return ResponseCode.INTERNAL_ERROR;}

			// 7) Update the version for the inventory and return it
			affected = update("UPDATE Household SET Version=? WHERE HouseholdId=?;",
					verParam,
					householdParam);
			if (affected <= 0) {rollback(); release(results); release(); return ResponseCode.INTERNAL_ERROR;}

		} catch (SQLException e) {
			rollback(); release(results); release(); return ResponseCode.INTERNAL_ERROR;
		}



		release(results);
		release();
		return ResponseCode.OK;
	}

	public long getVersion() {
		return version;
	}


}
