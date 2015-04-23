package json;

import java.util.List;

public class JSONModels {
	@SuppressWarnings("unused")
	public static class RegisterReqJSON {
		private final String emailAddress;
		private final String password;
		private final String firstName;
		private final String lastName;
		public RegisterReqJSON (String emailAddress, String password, String firstName, String lastName) {
			this.emailAddress = emailAddress;
			this.password = password;
			this.firstName = firstName;
			this.lastName = lastName;
		}
	}
	@SuppressWarnings("unused")
	public static class LoginReqJSON {
		private final String emailAddress;
		private final String password;
		public LoginReqJSON (String emailAddress, String password) {
			this.emailAddress = emailAddress;
			this.password = password;
		}
	}
	
	public static class LoginResJSON {
		public String token;
	}
	@SuppressWarnings("unused")
	public static class HouseholdCreateReqJSON {
		private final String householdName;
		private final String householdDescription;
		public HouseholdCreateReqJSON(String name, String description ) {
			householdName = name;
			householdDescription = description;
		}
	}
	public static class HouseholdCreateResJSON {
		public int householdID;
		public long version;
	}
	
	@SuppressWarnings("unused")
	public static class LinkReqJSON {
		private final String description;
		private final String packageName;
		private final int packageUnits;
		private final float packageSize;
		private final long version;
		public LinkReqJSON(String description, String packageName, int packageUnits, float packageSize, long version) {
			this.description = description;
			this.packageName = packageName;
			this.packageUnits = packageUnits;
			this.packageSize = packageSize;
			this.version = version;
		}
	}
	
	public static class LinkResJSON {
		public String UPC;
		public long version;
	}
	@SuppressWarnings("unused")
	public static class ListCreateReqJSON {
		private final String listName;
		public ListCreateReqJSON (String name) {
			listName = name;
		}
	}
	public static class ListCreateResJSON {
		public int listID;
		public long version;
	}
	@SuppressWarnings("unused")
	public static class ListUpdateReqJSON {
		private long version;
		private List<ListUpdateItem> items;
		
		public ListUpdateReqJSON(long timestamp, List<ListUpdateItem> items) {
			this.version = timestamp;
			this.items = items;
		}
	}
	public static class ListUpdateItem {
		public String UPC;
		public int quantity;
		public int fractional = 0;
		
		public ListUpdateItem(String UPC, int quantity, int fractional) {
			this.UPC = UPC;
			this.quantity = quantity;
			this.fractional = fractional;
		}
	}
	public static class ListUpdateResJSON {
		public long timestamp;
	}
	
	public static class InventoryUpdateReqJSON {
		public long version;
		public List<InventoryUpdateItem> items;
		
		public InventoryUpdateReqJSON(long version, List<InventoryUpdateItem> items) {
			this.version = version;
			this.items = items;
		}
		
	}
	public static class InventoryUpdateItem {
		public String UPC;
		public int quantity;
		public int fractional = 0;
		
		public InventoryUpdateItem(String UPC, int quantity, int fractional) {
			this.UPC = UPC;
			this.quantity = quantity;
			this.fractional = fractional;
		}
		
	}
	
	public static class RecipeCreateReqJSON {
		public String recipeName;
		public String recipeDescription;
		
		public RecipeCreateReqJSON (String recipeName, String recipeDescription) {
			this.recipeName = recipeName;
			this.recipeDescription = recipeDescription;
		}
	}
	
	public static class RecipeCreateResJSON {
		public int recipeID;
		public long version;
	}
	
	public static class RecipeUpdateReqJSON {
		public long version;
		public String recipeName;
		public String recipeDescription;
		public List<String> instructions;
		public List<RecipeUpdateIngredJSON> ingredients;
		
		public RecipeUpdateReqJSON(long version, String recipeName, String recipeDescription, List<String> instructions, List<RecipeUpdateIngredJSON> ingredients) {
			this.version = version;
			this.recipeName = recipeName;
			this.recipeDescription = recipeDescription;
			this.instructions = instructions;
			this.ingredients = ingredients;
		}
	}
	
	public static class RecipeUpdateIngredJSON {
		public String UPC;
		public int quantity;
		public int fractional;
		
		public RecipeUpdateIngredJSON(String UPC, int quantity, int fractional) {
			this.UPC = UPC;
			this.quantity = quantity;
			this.fractional = fractional;
		}
	}
}
