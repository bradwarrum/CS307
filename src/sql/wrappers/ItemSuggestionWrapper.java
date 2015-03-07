package sql.wrappers;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

import com.google.gson.*;
import com.google.gson.annotations.Expose;

import core.ResponseCode;

import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.*;

import sql.SQLExecutable;
import sql.SQLParam;
import sql.SQLType;

public class ItemSuggestionWrapper extends SQLExecutable{
	private int userID;
	@Expose(serialize = true)
	private String UPC;
	@Expose(serialize = true)
	private int householdId;
	@Expose(serialize = true)
	private String currentDescription = null;
	@Expose(serialize = true)
	private List<InternalSuggestJSON> internalSuggestions = new ArrayList<InternalSuggestJSON>();
	@Expose(serialize = true)
	private List<ExternalSuggestJSON> externalSuggestions = new ArrayList<ExternalSuggestJSON>();

	private static class InternalSuggestJSON {
		@Expose(serialize = true)
		private final int householdID;
		@Expose(serialize = true)
		private final String description;
		public InternalSuggestJSON(int householdID, String description) {
			this.householdID = householdID;
			this.description = description;
		}
	}

	private static class ExternalSuggestJSON {
		@Expose(serialize = true)
		private final String source;
		@Expose (serialize = true)
		private final String description;

		public ExternalSuggestJSON(String source, String description) {
			this.source = source;
			this.description = description;
		}
	}

	public ItemSuggestionWrapper(int householdId, int userID, String UPC){
		this.householdId = householdId;
		this.UPC = UPC;
		this.userID = userID;
	}

	public ResponseCode fetch(){
		ResultSet results= null;
		SQLParam upcsql = new SQLParam(UPC, SQLType.VARCHAR);
		SQLParam usersql = new SQLParam(userID, SQLType.INT);
		try{
			results = query("SELECT PermissionLevel FROM HouseholdPermissions WHERE (UserId=? AND HouseholdId=?);",
					usersql,
					new SQLParam(householdId, SQLType.INT));
			if (results == null) {release(); return  ResponseCode.INTERNAL_ERROR;}
			if (!results.next()) {release(results); release(); return  ResponseCode.HOUSEHOLD_NOT_FOUND;}
			release(results);
			results = query ("SELECT InventoryItem.Description, HouseholdPermissions.HouseholdId FROM HouseholdPermissions "
					+ "INNER JOIN InventoryItem ON (InventoryItem.HouseholdId = HouseholdPermissions.HouseholdId) "
					+ "WHERE (HouseholdPermissions.UserId=? AND InventoryItem.UPC=?);",
					usersql,
					upcsql);
			if (results == null) {release(); return  ResponseCode.INTERNAL_ERROR;}
			int houseTEMP = -1;
			String descriptionTEMP;
			while (results.next()) {
				descriptionTEMP = results.getString(1);
				houseTEMP = results.getInt(2);
				if (houseTEMP == householdId) {
					currentDescription = descriptionTEMP;
				} else {
					internalSuggestions.add(new InternalSuggestJSON(houseTEMP, descriptionTEMP));
				}
			}
			//hit API for default description
			String surl= "http://api.upcdatabase.org/json/72aaf1c920ed0cd53c54c6bc52b4c7ad/"+UPC;

			URL url = new URL(surl);
			HttpURLConnection request = (HttpURLConnection) url.openConnection();
			request.connect();

			JsonParser jp = new JsonParser(); 
			JsonElement root = jp.parse(new InputStreamReader((InputStream) request.getContent())); //convert the input stream to a json element
			JsonObject rootobj = root.getAsJsonObject(); 
			String res=rootobj.get("valid").getAsString();

			if(res.equals("true")){
				externalSuggestions.add(new ExternalSuggestJSON("www.upcdatabase.org", rootobj.get("itemname").getAsString()));
			}			
			request.disconnect();
			//TODO: add more api hits in case there is over 1000 a day or buy more hits
		}catch (SQLException e){
			release(results);
			release();
			return  ResponseCode.INTERNAL_ERROR;
		}catch (MalformedURLException e){
			release(results);
			release();
			return ResponseCode.INTERNAL_ERROR;
		}catch (Exception e){
			release(results);
			release();
			return  ResponseCode.INTERNAL_ERROR;
		}
		release(results);
		release();
		return  ResponseCode.OK;
	}

}
