package sql.wrappers;

import java.util.List;

import com.google.gson.annotations.Expose;

import core.ResponseCode;
import routes.RecipeUpdateRoute.*;

public class RecipeUpdateWrapper extends BaseWrapper {
	private int userID, householdID, recipeID;
	@Expose(serialize = true)
	private long version;
	private String recipeDescription, recipeName;
	private List<RecipeUpdateIngredJSON> ingredients;
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
		else if (DBtimestamp == -2) {release(); return ResponseCode.INSUFFICIENT_PERMISSIONS;}
		if (timestamp != DBtimestamp ) {release(); return ResponseCode.OUTDATED_TIMESTAMP;}
		// 3) If the version matches, update the values in the database
		if (!updateRows()) {return ResponseCode.ITEM_NOT_FOUND;}

		// 4) Update the version in the recipe table. (Use System.currentTimeMillis() for the version value)
		long newstamp = writeTimestamp();
		if (newstamp == -1) {return ResponseCode.INTERNAL_ERROR;}
		release();
		// 5) Set the version variable above to the same version you inserted into the table.  It will automatically sent back to the client.
		timestamp = newstamp;
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
	private boolean updateRows() {
		int updated = 0;
		int fail;
		SQLParam recipeidp = new SQLParam(recipeID, SQLType.INT);
		SQLParam houseidp = new SQLParam(householdID, SQLType.INT);

		ResultSet results= null;
		try {
			//this will consume inventory according to the recipe
			for (ListUpdateItemJSON item : items) {
				results = query("SELECT ItemId FROM InventoryItem WHERE (UPC=? AND HouseholdId=? AND Hidden=?);",
						new SQLParam(item.UPC, SQLType.VARCHAR),
						houseidp,
						SQLParam.SQLFALSE);
				if (results == null || !results.next()) {release(results); rollback(); release(); return false;}
				int itemID = results.getInt(1);
				SQLParam itemidp = new SQLParam(itemID, SQLType.INT);
				SQLParam quantityp = new SQLParam(item.quantity, SQLType.INT);
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
			affected = update("UPDATE HouseholdRecipe SET Timestamp=? WHERE RecipeId=?;",
					new SQLParam(stamp, SQLType.LONG),
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
