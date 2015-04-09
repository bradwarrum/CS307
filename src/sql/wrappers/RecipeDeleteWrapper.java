package sql.wrappers;

import java.sql.SQLException;

import sql.SQLParam;
import sql.SQLType;
import core.Permissions;
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
		int permraw = getPermissions(userID, householdID);
		if (permraw == -1) {return ResponseCode.INTERNAL_ERROR;}
		else if (permraw == -2) {return ResponseCode.HOUSEHOLD_NOT_FOUND;}
		Permissions perm = new Permissions(permraw);
		if (!perm.has(Permissions.Flag.CAN_MODIFY_RECIPES)) {return ResponseCode.INSUFFICIENT_PERMISSIONS;}
		// 2) Remove all recipe ingredient entries and recipe instruction rows in the database
		ResponseCode res=deleteRecipeIngredients();
		if(res != ResponseCode.OK)return res;
		res=deleteRecipeInstructions();
		if(res != ResponseCode.OK)return res;
		// 3) Remove the recipe entry in the database
		res=deleteRecipe();
		if(res != ResponseCode.OK)return res;
		// 4) Close the transaction (it will commit when you close)
		release();		
		return ResponseCode.OK;
	}
	private ResponseCode deleteRecipeIngredients(){
		int affected=-1;
		try{
			affected=update("DELETE FROM RecipeItem WHERE RecipeId=?;",
					new SQLParam(recipeID,SQLType.INT));
		} catch (SQLException e) {
			rollback();
			release();
			return ResponseCode.INTERNAL_ERROR;
		}
		if (affected < 0) {rollback(); release(); return ResponseCode.INTERNAL_ERROR;}
		return ResponseCode.OK;
	}
	private ResponseCode deleteRecipeInstructions(){
		int affected=-1;
		try{
			affected=update("DELETE FROM RecipeInstruction WHERE RecipeId=?;",
					new SQLParam(recipeID,SQLType.INT));
		} catch (SQLException e) {
			rollback();
			release();
			return ResponseCode.INTERNAL_ERROR;
		}
		if (affected < 0) {rollback(); release(); return ResponseCode.INTERNAL_ERROR;}
		return ResponseCode.OK;
	}
	private ResponseCode deleteRecipe(){
		int affected = -1;
		try {
			affected = update("DELETE FROM HouseholdRecipe WHERE HouseholdId=? AND RecipeId=?;",
					new SQLParam(householdID, SQLType.INT),
					new SQLParam(recipeID, SQLType.INT));
		} catch (SQLException e) {
			rollback();
			release();
			return ResponseCode.INTERNAL_ERROR;
		}
		if (affected < 0) {rollback(); release(); return ResponseCode.INTERNAL_ERROR;}
		else if (affected == 0) {rollback(); release(); return ResponseCode.RECIPE_NOT_FOUND;}
		return ResponseCode.OK;
	}
}
