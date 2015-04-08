package sql.wrappers;

import core.ResponseCode;


public class RecipeDeleteWrapper extends BaseWrapper {
	
	private int userID, householdID, recipeID;

	public RecipeDeleteWrapper(int userID, int householdID, int recipeID) {
		this.userID = userID;
		this.householdID = householdID;
		this.recipeID = recipeID;
	}
	
	public ResponseCode delete() {
		
		//Follow these steps
		// 1) Fetch permissions and check that user has recipe_modify permissions (look at other wrappers, there's a function for it)
		// 2) Remove all recipe ingredient entries and recipe instruction rows in the database
		// 3) Remove the recipe entry in the database
		// 4) Close the transaction (it will commit when you close)
		// Look at other wrappers if you need help with the response codes, they're fairly self explanatory but certain database errors mean different responses
		return ResponseCode.OK;
	}
}
