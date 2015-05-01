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

public class InviteCheckWrapper extends BaseWrapper {
	private int userID, householdID;
	@Expose(serialize=true)
	public List<int> ids;
	public InviteCheckWrapper(int userID, int householdID){
		this.userID= userID;
		this.householdID= householdID;
	}
	
	public ResponseCode check(){
		ResultSet results= null;
		try{
			results= query("SELECT HouseholdId FROM HouseholdInvitations WHERE UserId=?;"
					,new SQLParam(userID,SQLType.INT));
			if (results == null) {release(); return false;}
			ids= new List<int>();
			while(results.next()){
				ids.add(results.getInt(1));
			}
			release(results);
			query("DELETE FROM HouseholdInvitations where UserId=?;",
					new SQLParam(userID,SQLType.INT));
		}catch(SQLException e){
			release(results);
			release();
			return false;
			
		}
	
	}
}
