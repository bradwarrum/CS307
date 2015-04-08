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
		// 2) Create the recipe and insert the version number into the table ( use System.currentTimeMillis() for the version)
		// 3) Set the version variable above to the same version you inserted into the table.  It will automatically sent back to the client.
		return ResponseCode.CREATED;
	}
}
