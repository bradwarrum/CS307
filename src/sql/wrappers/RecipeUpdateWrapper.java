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
		// 2) Check that the version number matches the database version (look at ListUpdateWrapper. You will want to SELECT ... FOR UPDATE to lock the version number in the database)
		// 3) If the version matches, update the values in the database
		// 4) Update the version in the recipe table. (Use System.currentTimeMillis() for the version value)
		// 5) Set the version variable above to the same version you inserted into the table.  It will automatically sent back to the client.
		// Look at other wrappers if you need help with the response codes, they're fairly self explanatory but certain database errors mean different responses
		return ResponseCode.OK;
	}
	
	public long getVersion() {
		return version;
	}
}
