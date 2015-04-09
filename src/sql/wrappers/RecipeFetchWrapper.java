package sql.wrappers;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

import sql.SQLParam;
import sql.SQLType;

import com.google.gson.annotations.Expose;

import core.Permissions;
import core.ResponseCode;

public class RecipeFetchWrapper extends BaseWrapper {
	
	private int userID, householdID, recipeID;
	
	@Expose(serialize = true)
	private long version;
	@Expose(serialize = true)
	public List<RecipeFetchItemsJSON> ingredients;
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
	
	public static class RecipeFetchItemsJSON {
		@Expose(serialize = true)		
		public final String UPC;
		@Expose(serialize = true)
		public final boolean isInternalUPC;
		@Expose(serialize = true)		
		public final int quantity;
		@Expose(serialize = true)
		public final int fractional;
		
		public RecipeFetchItemsJSON (String UPC, int quantity, int fractional, boolean isInternalUPC) {
			this.UPC = UPC;
			this.quantity = quantity;
			this.fractional = fractional;
			this.isInternalUPC = isInternalUPC;
		}
	}
	public ResponseCode fetch() {
		
		//Follow these steps
		// 1) Fetch permissions and check that user has recipe_read permissions (look at other wrappers, there's a function for it)
		int permissionsraw = getPermissions(userID, householdID);
		if (permissionsraw == -1) return ResponseCode.INTERNAL_ERROR;
		else if (permissionsraw == -2) return ResponseCode.HOUSEHOLD_NOT_FOUND;
		Permissions permissions = new Permissions(permissionsraw);
		if (!permissions.has(Permissions.Flag.CAN_READ_RECIPES)) { release(); return ResponseCode.INSUFFICIENT_PERMISSIONS;}
		
		// 2) Check the database and see if the recipe version matches the version number above. If it matches return NOT_MODIFIED.
		int modresult = isModified(version);
		// 3) If the version is different, fill in all the private variables above that have the serialize attribute above them. Make sure you set the version variable to the database version.
		// 4) Close the transaction. It will auto-commit, although you probably won't be updating anything in the database.  All the variables above will be automatically returned to the client.
		// Look at other wrappers if you need help with the response codes, they're fairly self explanatory but certain database errors mean different responses
		if (modresult == -1) return ResponseCode.INTERNAL_ERROR;
		else if (modresult == -2) return ResponseCode.RECIPE_NOT_FOUND;
		else if (modresult == 0) return ResponseCode.NOT_MODIFIED;
		if (!selectAll()) return ResponseCode.INTERNAL_ERROR;
		release();
		return ResponseCode.OK;	
	}
	private int isModified(long since){
		ResultSet results = null;
		long dbstamp = 0;
		try {
			results = query("SELECT Timestamp, Name, Description FROM HouseholdRecipe  WHERE RecipeId =? AND HouseholdId=?;",
					new SQLParam(recipeID, SQLType.INT),
					new SQLParam(householdID, SQLType.INT));
			if (results == null) {release(); return -1;}
			if (!results.next()) {release(results); release(); return -2;}
			dbstamp = results.getLong(1);
			recipeName = results.getString(2);
			recipeDescription = results.getString(3);
		} catch (SQLException e) {
			release(results);
			release();
			return -1;
		}
		if (dbstamp != since) {version = dbstamp; return 1;}
		//Leak, bro
		release();
		return 0;
		
	}
	private boolean selectAll() {
		ResultSet results = null;
		try {
			results = query("SELECT I.UPC, R.Quantity "
					+ "FROM RecipeItem R  INNER JOIN InventoryItem I ON (R.ItemId=I.ItemId) "
					+ "WHERE (R.RecipeId=?);",
					new SQLParam(recipeID, SQLType.INT));
			if (results == null) {release(); return false;}
			
			ingredients = new ArrayList<RecipeFetchItemsJSON>();
			String UPC;
			int quantity, fractional;
			while (results.next()) {
				UPC = results.getString(1);
				int temp = results.getInt(2);
				quantity = temp / 100;
				fractional = temp - quantity * 100;
				ingredients.add(new RecipeFetchItemsJSON(UPC, quantity, fractional, UPC.length() == 5));
			}
			release(results);
			results = query("SELECT Instruction FROM RecipeInstruction ORDER BY SortOrder WHERE (RecipeId=?);",
					new SQLParam(recipeID, SQLType.INT));
			if (results == null) {release(); return false;}
			
		} catch (SQLException e) {
			release(results);
			release();
			return false;
		}
		return true;
	}
	public long getVersion() {
		return version;
	}
}
