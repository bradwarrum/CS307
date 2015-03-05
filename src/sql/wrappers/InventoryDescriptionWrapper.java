package sql.wrappers;

import java.sql.ResultSet;
import java.sql.SQLException;

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
import java.nio.charset.Charset;

import json.JSONObject;

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
				//first one has 1000 hits per day
				
				String surl= "http://api.upcdatabase.org/json/72aaf1c920ed0cd53c54c6bc52b4c7ad/"+UPC;
				JsonObject rootobj = readJsonFromUrl(surl); 
			    String res=rootobj.get("valid").getAsString();
								
				if(!res.equals("false")){
					description=rootobj.get("itemname");
				}else{
					description= "";
				}
				//TODO: add more api hits in case there is over 1000 a day
			}else{
				description=results.getString(1);			
			}
		}catch (SQLException e){
			release(results);
			release();
			return false;
		}
		return true;
	}
	
}
