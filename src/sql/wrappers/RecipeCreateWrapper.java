package sql.wrappers;

import com.google.gson.annotations.Expose;

import core.ResponseCode;

public class RecipeCreateWrapper extends BaseWrapper {
	
	private int userID, householdID;
	private String recipeName, recipeDescription;
	@Expose(serialize = true)
	private long version;

	public RecipeCreateWrapper(int userID, int householdID, String recipeName, String recipeDescription) {
		this.userID = userID;
		this.householdID = householdID;
		this.recipeName = recipeName;
		this.recipeDescription = recipeDescription;
	}
	
	public ResponseCode create() {
		
		//Follow these steps
		// 1) Fetch permissions and check that user has recipe_modify permissions (look at other wrappers, there's a function for it)
		int permLevel = getPermissions(userID, householdID);
		if (permLevel == -2) return ResponseCode.HOUSEHOLD_NOT_FOUND;
		else if (permLevel == -1) return ResponseCode.INTERNAL_ERROR;
		Permissions permissions = new Permissions(permLevel);

		if (!permissions.has(Permissions.Flag.CAN_MODIFY_RECIPES)) {release(); return ResponseCode.INSUFFICIENT_PERMISSIONS;}
		// 2) Create the recipe and insert the version number into the table ( use System.currentTimeMillis() for the version)
		int affected = 0;
		version = System.currentTimeMillis();
		try {
			affected = update("INSERT INTO HouseholdRecipe  (HouseholdId, Name, Description, Timestamp) VALUES (?, ?, ?, ?);", 
					new SQLParam(householdID, SQLType.INT),
					new SQLParam(recipeName, SQLType.VARCHAR),
					new SQLParam(recipeDescription,SQLType.VARCHAR),
					new SQLParam(version, SQLType.LONG));
			
		} catch (SQLException e) {
			rollback();
			release();
			return ResponseCode.INTERNAL_ERROR;
		}
		if (affected == 0) {rollback(); release(); return ResponseCode.INTERNAL_ERROR;}
		ResultSet results = null;
		// 3) Set the version variable above to the same version you inserted into the table.  It will automatically sent back to the client.
		try {
			results = query("SELECT LAST_INSERT_ID() AS lastID;");
			if (results == null || !results.next()) {rollback(); release(); return ResponseCode.INTERNAL_ERROR;}
			listID = results.getInt(1);
		}catch (SQLException e) {
			rollback(); release();
			return ResponseCode.INTERNAL_ERROR;
		} 
		release();
		return ResponseCode.CREATED;
	}
}
