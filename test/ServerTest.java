

import static org.junit.Assert.*;

import java.io.BufferedReader;
import java.io.DataOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;

import org.junit.BeforeClass;
import org.junit.Test;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;

import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.ProtocolException;
import java.net.URL;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

import json.JSONModels.HouseholdCreateResJSON;
import json.JSONModels.*;
import core.MeasurementUnits;


public class ServerTest {
	
	public static class DatabaseSetup extends sql.SQLExecutable {
		public boolean setup() {
			Path sqlfile = Paths.get("scripts/Sprint2Initialization.sql");
			System.out.println("Running setup script from " + sqlfile.toAbsolutePath().toString());
			List<String> contents;
			try {
				contents = Files.readAllLines(sqlfile);
			} catch (IOException e) {
				return false;
			}
			try {
			update("DROP DATABASE testdb;");
			update("CREATE DATABASE testdb;");
			update("USE testdb;");
			} catch (SQLException e ) {
				System.out.println("Database setup failed");
				e.printStackTrace(System.out);
				System.exit(1);
			}
			String query = "";
			for (String s : contents) {
				query += s + "\n";
				if (s.endsWith(");")) {
					try {
						update(query);
					}catch (SQLException e) {
						System.out.println("Database setup failed on query " + query);
						e.printStackTrace(System.out);
						System.exit(1);
					}
					query = "";
				}
			}
			release();
			System.out.println("Database setup done");
			return true;
		}
	}

	
	public static void dispatchLocalServer() {
		Thread t= new Thread(new Runnable () {

			@Override
			public void run() {
				try {
					core.Server.main(new String[0]);
				} catch (Exception e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
			}
			
		});
		t.start();
	}
	
	public static boolean setupDatabase() throws SQLException {
		DatabaseSetup setup = new DatabaseSetup();
		return setup.setup();
	}
	
	public static String delimiter = "==========================================================================";
	public static String host;
	public static String protocol = "http";
	public static int port = 8000;
	public static String prefix = "";
	
	public static String token = null;
	public static int householdID;
	public static int listID;
	public static int recipeID;
	public static long inventoryVersion, listVersion, recipeVersion;
	
	public static int rcode;
	public static String response;
	
	public static Gson gson = new GsonBuilder().setPrettyPrinting().create();
	@BeforeClass
	public static void setupClass() throws IOException, InterruptedException, SQLException {
		BufferedReader in = new BufferedReader(new InputStreamReader(System.in));
		System.out.println("Use local server? [Y/N] ");
		host = "127.0.0.1";
		if (in.readLine().equalsIgnoreCase("Y")) {
			dispatchLocalServer();
			Thread.sleep(5000);
			if (!setupDatabase()) throw new IOException("Cannot instantiate database");
		} else {
			System.out.println("Enter host address (without http://): ");
			host = in.readLine();
			port = 80;
			prefix = "/api";
		}
	}
	@Test
	public void test() throws MalformedURLException, IOException {
		System.out.println("Starting server test");
		//Registration and Login
		register("email1@gmail.com", "5e884898da28047151d0e56f8dc6292773603d0d6aabbdd62a11ef721d1542d8", "John", "Doe");
		assertEquals("Registration 1 pass", 201, rcode);
		register("email1@gmail.com", "5e884898da28047151d0e56f8dc6292773603d0d6aabbdd62a11ef721d1542d8", "John", "Doe");
		assertNotEquals("Registration 1 retry", 201, rcode);
		register("email2@gmail.com", "d9298a10d1b0735837dc4bd85dac641b0f3cef27a47e5d53a54f2f3f5b2fcffa", "Jane", "Robinson");
		assertEquals("Registration 2 pass", 201, rcode);
		login("email1@gmail.com", "5e884898da28047151d0e56f8dc6292773603d0d6aabbdd62a11ef721d1542d9");
		assertEquals("Login failure", 403, rcode);
		login("email1@gmail.com", "5e884898da28047151d0e56f8dc6292773603d0d6aabbdd62a11ef721d1542d8");
		assertEquals("Login pass", 200, rcode);
		token = gson.fromJson(response, LoginResJSON.class).token;
		getSelfInfo();
		assertEquals("Get self pass", 200, rcode);
		
		//Household Creation
		createHousehold("Stash", "Private Inventory");
		assertEquals("Household creation pass", 201, rcode);
		HouseholdCreateResJSON hcr = gson.fromJson(response, HouseholdCreateResJSON.class);
		householdID = hcr.householdID;
		inventoryVersion = hcr.version;
		
		//Linking
		link("029000071858", "Planters Cocktail Peanuts", "tins", MeasurementUnits.OZ, 12.0f);
		LinkResJSON lrj = gson.fromJson(response,  LinkResJSON.class);
		inventoryVersion = lrj.version;
		assertEquals("Link 1 pass", 200, rcode);
		
		link( "04963406", "Coca Cola", "cans", MeasurementUnits.OZ, 12.0f);
		lrj = gson.fromJson(response,  LinkResJSON.class);
		inventoryVersion = lrj.version;
		assertEquals("Link 2 pass", 200, rcode);
		
		link("040000231325", "Starburst FaveRed Jellybeans", "bags", MeasurementUnits.OZ, 14.0f);
		assertEquals("Link 3 pass", 200, rcode);
		lrj = gson.fromJson(response,  LinkResJSON.class);
		inventoryVersion = lrj.version;
		
		link(null, "Apple", "each", MeasurementUnits.UNITS, 1.0f);
		assertEquals("Generate pass", 200, rcode);
		lrj = gson.fromJson(response,  LinkResJSON.class);
		inventoryVersion = lrj.version;
		
		//Recipe creation
		createRecipe("Spaghetti", "Grandma's Special Spaghetti Recipe");
		assertEquals("Create recipe pass", 201, rcode);
		RecipeCreateResJSON rcr = gson.fromJson(response, RecipeCreateResJSON.class);
		recipeID = rcr.recipeID;
		recipeVersion = rcr.version;
		
		//Update recipe
		List<String> rinstructions = new ArrayList<String>();
		List<RecipeUpdateIngredJSON> ringredients = new ArrayList<RecipeUpdateIngredJSON>();
		rinstructions.add("First put the thing in a pot");
		rinstructions.add("Then put the thing on the stove");
		rinstructions.add("Stir and then drop the spaghetti");
		rinstructions.add("Get on the floor and walk the dinosaur");
		ringredients.add(new RecipeUpdateIngredJSON("029000071858", 2, 50));
		ringredients.add(new RecipeUpdateIngredJSON("00001", 1, 99));
		updateRecipe("New Spaghetti", "This Spaghetti Is Actually Grandpas", rinstructions, ringredients);
		assertEquals("Update recipe pass", 200, rcode);
		
		//Fetch the recipe
		fetchRecipe();
		assertEquals("Fetch recipe pass", 200, rcode);
		
		//List operations
		createList("Weekly Shopping");
		assertEquals("Create List pass", 201, rcode);
		ListCreateResJSON lcr = gson.fromJson(response, ListCreateResJSON.class);
		listID = lcr.listID;
		listVersion = lcr.version;
		List<ListUpdateItem> items = new ArrayList<ListUpdateItem>();
		items.add(new ListUpdateItem("029000071858", 3, 0));
		items.add(new ListUpdateItem( "04963406", 12, 50));
		items.add(new ListUpdateItem("040000231325", 0, 50));
		items.add(new ListUpdateItem("00001", 0, 99)); // 1 is the first value for the generated UPC
		updateList(items);
		assertEquals("Update list pass", 200, rcode);
		listVersion = gson.fromJson(response, ListUpdateResJSON.class).timestamp;
		getList();
		assertEquals("Get list pass", 200, rcode);
		
		//General fetching
		getHousehold();
		assertEquals("Get household pass", 200, rcode);
		getInventory();
		assertEquals("Inventory fetch pass", 200, rcode);
		
		//Inventory updates
		List<InventoryUpdateItem> invItems = new ArrayList<InventoryUpdateItem>();
		invItems.add(new InventoryUpdateItem("029000071858", 1, 50));
		invItems.add(new InventoryUpdateItem("04963406", 17, 0));
		invItems.add(new InventoryUpdateItem("040000231325", 1, 25));
		updateInventory(invItems);
		assertEquals("Inventory update pass", 200, rcode);
		getInventory();
		assertEquals("Inventory fetch pass", 200, rcode);
		
		//Deletions
		deleteItem("04963406");
		assertEquals("Deletion pass", 200, rcode);
		deleteItem("00001");
		assertEquals("Deletion of generated UPC", 200, rcode);
		getInventory();
		getList();
		fetchRecipe();
		link("04963406", "Coca cola", "cans", MeasurementUnits.ML, 355.0f);
		getInventory();
		removeList();
		assertEquals("List removal pass", 200, rcode);
		getHousehold();
		assertEquals("Get household pass", 200, rcode);
		deleteRecipe();
		assertEquals("Recipe removal pass", 200, rcode);
		getHousehold();
		
		
		//Suggestions
		createHousehold("Apartment", "John and Julia's Inventory");
		assertEquals("Household creation pass", 201, rcode);
		hcr = gson.fromJson(response, HouseholdCreateResJSON.class);
		householdID = hcr.householdID;
		inventoryVersion = hcr.version;
		link( "04963406", "Coke", "cans", MeasurementUnits.ML, 355.0f);
		assertEquals("Link 3 pass", 200, rcode);
		getSuggestions("04963406");
		assertEquals("Suggestion pass", 200, rcode);

		
		
	}
	
	
	public static class Transaction {
		private HttpURLConnection connection;
		public Transaction(String protocol, String host, int port, String file) throws MalformedURLException, IOException {
			connection = (HttpURLConnection) new URL(protocol, host, port, prefix + file).openConnection();
		}
		
		public String getRequestURL() {
			return connection.getURL().toString();
		}
		public void setGetMethod() throws ProtocolException {
			connection.setRequestMethod("GET");
			connection.setDoInput(true);
		}
		public void setPostMethod() throws ProtocolException {
			connection.setRequestMethod("POST");
			connection.setRequestProperty("Content-Type", "application/json");
			connection.setDoInput(true);
			connection.setDoOutput(true);
		}
		public void send(String request) throws IOException {
			DataOutputStream wr = new DataOutputStream(connection.getOutputStream());
			wr.writeBytes(request);
			wr.flush(); wr.close();
		}
		public int getResponseCode() throws IOException {
			return connection.getResponseCode();
		}
		
		public String getResponse() throws IOException {
			
			InputStream is;
			if (connection.getResponseCode() == 200 || connection.getResponseCode() == 201 || connection.getResponseCode() == 304) 
				is = connection.getInputStream();
			else
				is = connection.getErrorStream();
			BufferedReader rd = new BufferedReader(new InputStreamReader(is));
			String line;
			StringBuffer response = new StringBuffer();
			while ((line = rd.readLine()) != null) {
				response.append(line);
				response.append("\n");
			}
			rd.close();
			return response.toString();
		}
		public void close() {
			if (connection != null) {
				connection.disconnect();
			}
		}
	}

	public void register(String emailAddress, String password, String first, String last) throws MalformedURLException, IOException {
		Transaction request = new Transaction(protocol, host, port, "/users/register");
		request.setPostMethod();
		String reqstr = gson.toJson(new RegisterReqJSON(emailAddress, password, first, last));
		System.out.println(delimiter + "\nRequest: REGISTER");
		System.out.println(request.getRequestURL());
		System.out.println(reqstr);
		request.send(reqstr);
		System.out.println("Response:");
		rcode = request.getResponseCode();
		System.out.println("HTTP " + rcode);
		try {
			response = request.getResponse();
			System.out.println(response);
		}catch (IOException e) {
			System.out.println(e);
		}
		request.close();	
	}

	
	public void login(String emailAddress, String password) throws IOException {
		Transaction request = new Transaction(protocol, host, port, "/users/login");
		request.setPostMethod();
		String reqstr = gson.toJson(new LoginReqJSON(emailAddress, password));
		System.out.println(delimiter + "\nRequest: LOGIN ");
		System.out.println(request.getRequestURL());
		System.out.println(reqstr);
		request.send(reqstr);
		System.out.println("Response:");
		rcode = request.getResponseCode();
		System.out.println("HTTP " + rcode);
		try {
			response = request.getResponse();
			System.out.println(response);
		}catch (IOException e) {}
		request.close();
	}

	public void createHousehold(String name, String description) throws IOException {
		Transaction request = new Transaction(protocol, host, port, "/households/create?token=" + token);
		request.setPostMethod();
		String reqstr = gson.toJson(new HouseholdCreateReqJSON(name, description));
		System.out.println(delimiter + "\nRequest: CREATE HOUSEHOLD");
		System.out.println(request.getRequestURL());
		System.out.println(reqstr);
		request.send(reqstr);
		System.out.println("Response:");
		rcode = request.getResponseCode();
		System.out.println("HTTP " + rcode);
		try {
			response = request.getResponse();
			System.out.println(response);
		}catch (IOException e) {}
		request.close();
	}
	
	public void link(String UPC, String description, String packageName, MeasurementUnits packageUnits, float packageSize)throws IOException {
		Transaction request;
		if (UPC != null) {
			request = new Transaction(protocol, host, port, "/households/" + householdID + "/items/" + UPC + "/link?token=" + token);
		} else {
			request = new Transaction(protocol, host, port, "/households/" + householdID + "/items/generate?token=" + token);
		}
		request.setPostMethod();
		String reqstr = gson.toJson(new LinkReqJSON(description, packageName, packageUnits.getID(), packageSize, inventoryVersion));
		if (UPC == null) {
			System.out.println(delimiter + "\nRequest: GENERATE UPC");
		} else {
			System.out.println(delimiter + "\nRequest: LINK UPC");
		}
		System.out.println(request.getRequestURL());
		System.out.println(reqstr);
		request.send(reqstr);
		System.out.println("Response:");
		rcode = request.getResponseCode();
		System.out.println("HTTP " + rcode);
		try {
			response = request.getResponse();
			System.out.println(response);
		}catch (IOException e) {}
		request.close();
	}
	
	public void createList(String listName) throws IOException {
		Transaction request = new Transaction(protocol, host, port, "/households/" + householdID + "/lists/create?token=" + token);
		request.setPostMethod();
		String reqstr = gson.toJson(new ListCreateReqJSON(listName));
		System.out.println(delimiter + "\nRequest: CREATE LIST");
		System.out.println(request.getRequestURL());
		System.out.println(reqstr);
		request.send(reqstr);
		System.out.println("Response:");
		rcode = request.getResponseCode();
		System.out.println("HTTP " + rcode);
		try {
			response = request.getResponse();
			System.out.println(response);
		}catch (IOException e) {}
		request.close();
	}
	
	public void updateList(List<ListUpdateItem> items) throws IOException {
		Transaction request = new Transaction(protocol, host, port, "/households/" + householdID + "/lists/" + listID + "/update?token=" + token);
		request.setPostMethod();
		String reqstr = gson.toJson(new ListUpdateReqJSON(listVersion, items));
		System.out.println(delimiter + "\nRequest: UPDATE LIST");
		System.out.println(request.getRequestURL());
		System.out.println(reqstr);
		request.send(reqstr);
		System.out.println("Response:");
		rcode = request.getResponseCode();
		System.out.println("HTTP " + rcode);
		try {
			response = request.getResponse();
			System.out.println(response);
		}catch (IOException e) {}
		request.close();
	}
	
	public void getList() throws IOException{
		Transaction request = new Transaction(protocol, host, port, "/households/" + householdID + "/lists/" + listID + "?token=" + token);
		request.setGetMethod();
		System.out.println(delimiter + "\nRequest: GET LIST");
		System.out.println(request.getRequestURL());
		System.out.println("Response:");
		rcode = request.getResponseCode();
		System.out.println("HTTP " + rcode);
		try {
			response = request.getResponse();
			System.out.println(response);
		}catch (IOException e) {}
		request.close();
	}
	
	public void getSelfInfo() throws IOException{
		Transaction request = new Transaction(protocol, host, port, "/users/me?token=" + token);
		request.setGetMethod();
		System.out.println(delimiter + "\nRequest: GET USER INFORMATION");
		System.out.println(request.getRequestURL());
		System.out.println("Response:");
		rcode = request.getResponseCode();
		System.out.println("HTTP " + rcode);
		try {
			response = request.getResponse();
			System.out.println(response);
		}catch (IOException e) {}
		request.close();
	}
	public void getHousehold() throws IOException{
		Transaction request = new Transaction(protocol, host, port, "/households/" + householdID +"?token=" + token);
		request.setGetMethod();
		System.out.println(delimiter + "\nRequest: GET HOUSEHOLD");
		System.out.println(request.getRequestURL());
		System.out.println("Response:");
		rcode = request.getResponseCode();
		System.out.println("HTTP " + rcode);
		try {
			response = request.getResponse();
			System.out.println(response);
		}catch (IOException e) {}
		request.close();
	}
	public void removeList() throws IOException {
		Transaction request = new Transaction(protocol, host, port, "/households/" + householdID + "/lists/" + listID + "/remove?token=" + token);
		request.setPostMethod();
		System.out.println(delimiter + "\nRequest: REMOVE LIST");
		System.out.println(request.getRequestURL());
		System.out.println("Response:");
		rcode = request.getResponseCode();
		System.out.println("HTTP " + rcode);
		try {
			response = request.getResponse();
			System.out.println(response);
		}catch (IOException e) {}
		request.close();
	}	
	public void getSuggestions(String UPC) throws IOException{
		Transaction request = new Transaction(protocol, host, port, "/households/" + householdID +"/items/" + UPC + "/suggestions?token=" + token);
		request.setGetMethod();
		System.out.println(delimiter + "\nRequest: GET ITEM SUGGESTIONS");
		System.out.println(request.getRequestURL());
		System.out.println("Response:");
		rcode = request.getResponseCode();
		System.out.println("HTTP " + rcode);
		try {
			response = request.getResponse();
			System.out.println(response);
		}catch (IOException e) {}
		request.close();
	}
	
	public void getInventory() throws MalformedURLException, IOException {
		Transaction request = new Transaction(protocol, host, port, "/households/" + householdID + "/items?token=" + token);
		request.setGetMethod();
		System.out.println(delimiter + "\nRequest: GET HOUSEHOLD INVENTORY");
		System.out.println(request.getRequestURL());
		System.out.println("Response:");
		rcode = request.getResponseCode();
		System.out.println("HTTP " + rcode);
		try {
			response = request.getResponse();
			System.out.println(response);
		}catch (IOException e) {}
		request.close();
	}
	
	public void updateInventory(List<InventoryUpdateItem> items) throws MalformedURLException, IOException {
		Transaction request = new Transaction(protocol, host, port, "/households/" + householdID + "/items/update?token=" + token);
		request.setPostMethod();
		String reqstr = gson.toJson(new InventoryUpdateReqJSON(inventoryVersion, items));
		System.out.println(delimiter + "\nRequest: UPDATE INVENTORY");
		System.out.println(request.getRequestURL());
		System.out.println(reqstr);
		request.send(reqstr);
		System.out.println("Response:");
		rcode = request.getResponseCode();
		System.out.println("HTTP " + rcode);
		try {
			response = request.getResponse();
			System.out.println(response);
		}catch (IOException e) {}
		request.close();
	}
	
	public void deleteItem(String UPC) throws MalformedURLException, IOException {
		Transaction request = new Transaction(protocol, host, port, "/households/" + householdID + "/items/" + UPC + "/unlink?token=" + token);
		request.setPostMethod();
		System.out.println(delimiter + "\nRequest: REMOVE INVENTORY ITEM");
		System.out.println(request.getRequestURL());
		System.out.println("Response:");
		rcode = request.getResponseCode();
		System.out.println("HTTP " + rcode);
		try {
			response = request.getResponse();
			System.out.println(response);
		}catch (IOException e) {}
		request.close();
	}
	
	public void createRecipe(String name, String description) throws MalformedURLException, IOException {
		Transaction request = new Transaction(protocol, host, port, "/households/" + householdID + "/recipes/create?token=" + token);
		request.setPostMethod();
		String reqstr = gson.toJson(new RecipeCreateReqJSON(name, description));
		System.out.println(delimiter + "\nRequest: CREATE RECIPE");
		System.out.println(request.getRequestURL());
		System.out.println(reqstr);
		request.send(reqstr);
		System.out.println("Response:");
		rcode = request.getResponseCode();
		System.out.println("HTTP " + rcode);
		try {
			response = request.getResponse();
			System.out.println(response);
		}catch (IOException e) {}
		request.close();
	}
	
	public void updateRecipe(String name, String description, List<String> instructions, List<RecipeUpdateIngredJSON> ingredients) throws MalformedURLException, IOException {
		Transaction request = new Transaction(protocol, host, port, "/households/" + householdID + "/recipes/" + recipeID + "/update?token=" + token);
		request.setPostMethod();
		String reqstr = gson.toJson(new RecipeUpdateReqJSON(recipeVersion, name, description, instructions, ingredients));
		System.out.println(delimiter + "\nRequest: UPDATE RECIPE");
		System.out.println(request.getRequestURL());
		System.out.println(reqstr);
		request.send(reqstr);
		System.out.println("Response:");
		rcode = request.getResponseCode();
		System.out.println("HTTP " + rcode);
		try {
			response = request.getResponse();
			System.out.println(response);
		}catch (IOException e) {}
		request.close();
	}
	
	public void fetchRecipe() throws MalformedURLException, IOException {
		Transaction request = new Transaction(protocol, host, port, "/households/" + householdID + "/recipes/" + recipeID + "?token=" + token);
		request.setGetMethod();
		System.out.println(delimiter + "\nRequest: GET RECIPE");
		System.out.println(request.getRequestURL());
		System.out.println("Response:");
		rcode = request.getResponseCode();
		System.out.println("HTTP " + rcode);
		try {
			response = request.getResponse();
			System.out.println(response);
		}catch (IOException e) {}
		request.close();
	}
	
	public void deleteRecipe() throws IOException {
		Transaction request = new Transaction(protocol, host, port, "/households/" + householdID + "/recipes/" + recipeID + "/remove?token=" + token);
		request.setPostMethod();
		System.out.println(delimiter + "\nRequest: REMOVE RECIPE");
		System.out.println(request.getRequestURL());
		System.out.println("Response:");
		rcode = request.getResponseCode();
		System.out.println("HTTP " + rcode);
		try {
			response = request.getResponse();
			System.out.println(response);
		}catch (IOException e) {}
		request.close();
	}

}
