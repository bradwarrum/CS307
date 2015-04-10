package sql.wrappers;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.List;

import com.google.gson.annotations.Expose;

import core.Permissions;
import core.ResponseCode;
import core.json.UpdateItemJSON;
import routes.RecipeUpdateRoute.*;
import sql.SQLParam;
import sql.SQLType;

public class RecipeUpdateWrapper extends BaseWrapper {
	private int userID, householdID, recipeID;
	@Expose(serialize = true)
	private long version;
	private String recipeDescription, recipeName;
	private List<UpdateItemJSON> ingredients;
	private List<String> instructions;
	public RecipeUpdateWrapper(int userID, int householdID, int recipeID, RecipeUpdateJSON ruj) {
		this.userID = userID;
		this.householdID = householdID;
		this.recipeID = recipeID;
		this.version = ruj.version;
		this.ingredients = ruj.ingredients;
		this.instructions = ruj.instructions;
		this.recipeName = ruj.recipeName;
		this.recipeDescription = ruj.recipeDescription;
	}
	
	public ResponseCode update() {
		
		//Follow these steps
		// 1) Fetch permissions and check that user has recipe_modify permissions (look at other wrappers, there's a function for it)
		int permissionsraw = getPermissions(userID, householdID);
		if (permissionsraw == -1) {return ResponseCode.INTERNAL_ERROR;}
		else if (permissionsraw == -2) {return ResponseCode.HOUSEHOLD_NOT_FOUND;}
		Permissions permissions = new Permissions(permissionsraw);
		if (!permissions.has(Permissions.Flag.CAN_MODIFY_RECIPES)) {release(); return ResponseCode.INSUFFICIENT_PERMISSIONS;}
		
		// 2) Check that the version number matches the database version (look at ListUpdateWrapper. You will want to SELECT ... FOR UPDATE to lock the version number in the database)
		long DBtimestamp = readAndLockTimestamp();
		if (DBtimestamp == -1) {release(); return ResponseCode.INTERNAL_ERROR;}
		else if (DBtimestamp == -2) {release(); return ResponseCode.RECIPE_NOT_FOUND;}
		if (version != DBtimestamp ) {release(); return ResponseCode.OUTDATED_TIMESTAMP;}
		// 3) If the version matches, update the values in the database
		ResponseCode res = updateRows();
		if (res != ResponseCode.OK) {return res;}

		// 4) Update the version in the recipe table. (Use System.currentTimeMillis() for the version value)
		long newstamp = writeTimestamp();
		if (newstamp == -1) {return ResponseCode.INTERNAL_ERROR;}
		release();
		// 5) Set the version variable above to the same version you inserted into the table.  It will automatically sent back to the client.
		version = newstamp;
		return ResponseCode.OK;
	}
	private long readAndLockTimestamp() {
		long stamp = -1;
		ResultSet results = null;
		try {
			results = query ("SELECT Timestamp FROM HouseholdRecipe  WHERE RecipeId =? AND HouseholdId=? FOR UPDATE;",
					new SQLParam(recipeID, SQLType.INT),
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
	private ResponseCode updateRows() {
		SQLParam recipeidp = new SQLParam(recipeID, SQLType.INT);
		SQLParam houseidp = new SQLParam(householdID, SQLType.INT);
		

		int affected = -1;
		ResultSet results = null;
		try {
			//Delete all relevant items first
			affected = update("DELETE FROM RecipeItem WHERE RecipeId=?;",
					recipeidp);
			if (affected < 0) {rollback(); release(); return ResponseCode.INTERNAL_ERROR;}
			affected = update("DELETE FROM RecipeInstruction WHERE RecipeId=?;",
					recipeidp);
			if (affected < 0) {rollback(); release(); return ResponseCode.INTERNAL_ERROR;}
			//Insert instructions
			for(int i = 0; i < instructions.size(); i++) {
				affected = update("INSERT INTO RecipeInstruction (RecipeId, SortOrder, Instruction)"
						+ "VALUES (?, ?, ?);",
						recipeidp,
						new SQLParam(i, SQLType.INT),
						new SQLParam(instructions.get(i), SQLType.VARCHAR));
				if (affected !=1 ) {rollback(); release(); return ResponseCode.INTERNAL_ERROR;}
			}
			//Insert Ingredients

			for (int i = 0; i < ingredients.size(); i++) {
				UpdateItemJSON ingred = ingredients.get(i);
				results = query("SELECT ItemId FROM InventoryItem WHERE (UPC=? AND HouseholdId=? AND Hidden=?);",
						new SQLParam(ingred.UPC, SQLType.VARCHAR),
						houseidp,
						SQLParam.SQLFALSE);
				if (results == null || !results.next()) {rollback(); release(results); release(); return ResponseCode.ITEM_NOT_FOUND;}
				int itemID = results.getInt(1);
				release(results);
				
				affected = update("INSERT INTO RecipeItem (RecipeId, ItemId, Quantity) "
						+ "VALUES (?, ?, ?);"
						+ "",
						recipeidp,
						new SQLParam(itemID, SQLType.INT),
						new SQLParam(ingred.quantity * 100 + ingred.fractional, SQLType.INT));
				if (affected != 1) {rollback(); release(); return ResponseCode.INTERNAL_ERROR;}
			}
		} catch (SQLException e) {
			rollback();
			release(results);
			release();
			return ResponseCode.INTERNAL_ERROR;
		}
		return ResponseCode.OK;

	}
	private long writeTimestamp() {
		long stamp = System.currentTimeMillis();
		int affected = -1;
		try {
			affected = update("UPDATE HouseholdRecipe SET Timestamp=?, Name=?, Description=? WHERE RecipeId=?;",
					new SQLParam(stamp, SQLType.LONG),
					new SQLParam(recipeName, SQLType.VARCHAR),
					new SQLParam(recipeDescription, SQLType.VARCHAR),
					new SQLParam(recipeID, SQLType.INT));
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
