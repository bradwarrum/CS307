package sql.wrappers;

import java.sql.ResultSet;
import java.sql.SQLException;

import com.google.gson.*;
import com.google.gson.JsonSyntaxException;
import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;
import com.sun.net.httpserver.HttpExchange;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.Reader;
import java.net.URL;
import java.net.*;
import java.nio.charset.Charset;

import sql.SQLExecutable;
import sql.SQLParam;
import sql.SQLType;
import core.Permissions;

import core.Server;
public class InventoryDescriptionWrapper extends SQLExecutable{

	@Expose(serialize = true)
	private String UPC;
	@Expose(serialize = true)
	private int householdId;
	@Expose(serialize = true)
	private String description;
	
	public InventoryDescriptionWrapper(int householdId, String UPC){
		this.householdId = householdId;
		this.UPC = UPC;
	}
	public boolean fetch(){
		ResultSet results= null;
		SQLParam hidp = new SQLParam(householdId, SQLType.INT);
		SQLParam upcsql = new SQLParam(UPC, SQLType.VARCHAR);
		try{
			results= query("SELECT Description FROM InventoryItem WHERE (UPC=? AND HouseholdId=?);",upcsql,hidp);
			if(results==null){
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
					description=rootobj.get("itemname").getAsString();
				}else{
					description= "";
				}			
				//TODO: add more api hits in case there is over 1000 a day or buy more hits
			}else{
				description=results.getString(1);			
			}
		}catch (SQLException e){
			release(results);
			release();
			return false;
		}catch (MalformedURLException e){
			release(results);
			release();
			return false;
		}catch (Exception e){
			release(results);
			release();
			return false;
		}
		return true;
	}
	
}
