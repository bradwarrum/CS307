package sql.wrappers;

import java.sql.ResultSet;
import java.sql.SQLException;

import com.google.gson.annotations.Expose;

import sql.SQLExecutable;
import sql.SQLParam;
import sql.SQLType;
import core.Permissions;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.Reader;
import java.net.URL;
import java.nio.charset.Charset;

import org.json.JSONException;
import org.json.JSONObject;

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
	
	private static String readAll(Reader rd) throws IOException {
	    StringBuilder sb = new StringBuilder();
	    int cp;
	    while ((cp = rd.read()) != -1) {
	      sb.append((char) cp);
	    }
	    return sb.toString();
	  }

	public static JSONObject readJson(String upc) throws IOException, JSONException {
		String url= "http://api.upcdatabase.org/json/72aaf1c920ed0cd53c54c6bc52b4c7ad/"+upc
	    InputStream is = new URL(url).openStream();
	    try {
	      BufferedReader rd = new BufferedReader(new InputStreamReader(is, Charset.forName("UTF-8")));
	      String jsonText = readAll(rd);
	      JSONObject json = new JSONObject(jsonText);
	      return json;
	    } finally {
	      is.close();
	    }
	 }
	public boolean fetch(){
		ResultSet results= null;
		SQLParam hidp= new SQLParam(householdID, SQLType.INT);
		SQLParam upc= new SQLParam(UPC,SQLTYPE.VARCHAR(13);
		try{
			reults= query("SELECT Description FROM InventoryItem WHERE (UPC=? AND HouseholdId=?);"
					,upc,hidp);
			if(results==null){
				//hit API for default description
				//first one has 1000 hits per day
				JSONObject des = readJson(UPC);
				if(des.get("valid")!=false){
					description=des.get("itemname")
				}else{
					description= "";
				}
				//TODO: add more api hits in case there is over 1000 a day
			}else{
				description=results.first().getString(1);			
			}
		}catch (SQLException e){
			release(results);
			release();
			return false;
		}
		return true;
	}
	
}
