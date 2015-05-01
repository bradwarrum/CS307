package sql.wrappers;

import java.sql.ResultSet;
import java.sql.SQLException;

import sql.SQLParam;
import sql.SQLType;

import com.google.gson.annotations.Expose;

import core.Permissions;
import core.ResponseCode;

public class InviteCreateWrapper extends BaseWrapper {
	private int userId, householdId;
	public InviteCreateWrapper(int userId, int householdID){
		this.userId= userId;
		this.householdId= householdID;
	}
	public ResponseCode create(){
		// 2) Create the invite
		int affected = 0;
		try {
			affected = update("INSERT INTO HouseholdInvitations (UserId,HouseholdId) VALUES (?, ?);", 
					new SQLParam(userId, SQLType.INT),
					new SQLParam(householdId, SQLType.INT));
			
		} catch (SQLException e) {
			rollback();
			release();
			return ResponseCode.INTERNAL_ERROR;
		}
		if (affected == 0) {rollback(); release(); return ResponseCode.INTERNAL_ERROR;}
		release();
		return ResponseCode.CREATED;

	}
}
