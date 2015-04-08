package sql.wrappers;

import java.util.List;

import routes.RecipeUpdateRoute.RecipeUpdateIngredJSON;

import com.google.gson.annotations.Expose;

import core.ResponseCode;

public class RecipeFetchWrapper extends BaseWrapper {
	
	private int userID, householdID, recipeID;
	
	@Expose(serialize = true)
	private long version;
	@Expose(serialize = true)
	public List<RecipeUpdateIngredJSON> ingredients;
	@Expose(serialize = true)
	public List<String> instructions;
	@Expose(serialize = true)
	public String recipeName;
	@Expose(serialize = true)
	public String recipeDescription;

	public RecipeFetchWrapper(int userID, int householdID, int recipeID, long etag) {
		this.userID = userID;
		this.householdID = householdID;
		this.recipeID = recipeID;
		this.version = etag;
	}
	
	
	public ResponseCode fetch() {
		
		//Follow these steps
		// 1) Fetch permissions and check that user has recipe_read permissions (look at other wrappers, there's a function for it)
		// 2) Check the database and see if the recipe version matches the version number above. If it matches return NOT_MODIFIED.
		// 3) If the version is different, fill in all the private variables above that have the serialize attribute above them. Make sure you set the version variable to the database version.
		// 4) Close the transaction. It will auto-commit, although you probably won't be updating anything in the database.  All the variables above will be automatically returned to the client.
		// Look at other wrappers if you need help with the response codes, they're fairly self explanatory but certain database errors mean different responses
		return ResponseCode.OK;
	}
	
	public long getVersion() {
		return version;
	}
}
