package sql.wrappers;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

import com.google.gson.annotations.Expose;

import core.Permissions;
import core.ResponseCode;
import sql.SQLParam;
import sql.SQLType;

public class HouseholdFetchWrapper extends BaseWrapper {
	
	private int userID;
	@Expose(serialize = true)	
	private int householdID;
	@Expose(serialize = true)	
	private String householdName;
	@Expose(serialize = true)	
	private String householdDescription;
	@Expose(serialize = true)
	private int headOfHousehold;
	@Expose(serialize = true)
	private List<MembersJSON> members;
	@Expose(serialize = true)
	private List<ListsJSON> lists;
	
	
	private static class MembersJSON {
		@Expose(serialize = true)
		private final int userID;
		@Expose(serialize = true)		
		private final String firstName;
		@Expose(serialize = true)		
		private final String lastName;
		@Expose(serialize = true)		
		private final String emailAddress;
		
		public MembersJSON(int userID, String firstName, String lastName, String emailAddress) {
			this.userID = userID;
			this.firstName = firstName;
			this.lastName = lastName;
			this.emailAddress = emailAddress;
		}
	}
	
	private static class ListsJSON {
		@Expose(serialize = true)
		private final int listID;
		@Expose(serialize = true)
		private final String listName;
		
		public ListsJSON(int listID, String listName) {
			this.listID = listID;
			this.listName = listName;
		}
	}
	public HouseholdFetchWrapper(int userID, int householdID) {
		this.userID = userID;
		this.householdID = householdID;
	}
	
	
	public ResponseCode fetch() {
		ResultSet results = null;
		Permissions permissions = null;
		SQLParam hidp = new SQLParam(householdID, SQLType.INT);
		try {
			results = query("SELECT User.UserId, User.Email, User.FirstName, User.LastName, HouseholdPermissions.PermissionLevel FROM User "
					+ "INNER JOIN HouseholdPermissions ON (HouseholdPermissions.UserId=User.UserId) "
					+ "WHERE (HouseholdId=?);",
					hidp);
			if (results == null) {release(); return ResponseCode.INTERNAL_ERROR;}
			members = new ArrayList<MembersJSON>();
			int userID_temp, permraw_TEMP;
			String firstNameTEMP, lastNameTEMP, emailAddressTEMP;
			while (results.next()) {
				userID_temp = results.getInt(1);
				emailAddressTEMP = results.getString(2);
				firstNameTEMP = results.getString(3);
				lastNameTEMP = results.getString(4);
				permraw_TEMP = results.getInt(5);
				if (userID_temp == userID) {
					permissions = new Permissions(permraw_TEMP);
				}
				members.add(new MembersJSON(userID_temp, firstNameTEMP, lastNameTEMP, emailAddressTEMP));
			}
			release(results);
		} catch (SQLException e ){
			release(results);
			release();
			return ResponseCode.INTERNAL_ERROR;
		}
		if (permissions == null) {release(); return ResponseCode.HOUSEHOLD_NOT_FOUND;}
		
		try {
			results = query("SELECT Name, Description, HeadOfHousehold FROM Household WHERE (HouseholdId=?);",
					hidp);
			if (results == null) {release(); return ResponseCode.INTERNAL_ERROR;}
			if (!results.next()) {release(results); release(); return ResponseCode.HOUSEHOLD_NOT_FOUND;}
			householdName = results.getString(1);
			householdDescription = results.getString(2);
			headOfHousehold = results.getInt(3);
		}catch (SQLException e ){
			release(results);
			release();
			return ResponseCode.INTERNAL_ERROR;
		}
		lists = new ArrayList<ListsJSON> ();
		if (permissions.has(Permissions.Flag.CAN_READ_LISTS)) {
			try {
				results = query("SELECT ListId, Name FROM HouseholdShoppingList WHERE (HouseholdId=?);",
						hidp);
				if (results == null) {release(); return ResponseCode.INTERNAL_ERROR;}
				int listidTEMP;
				String nameTEMP;
				while (results.next()) {
					listidTEMP = results.getInt(1);
					nameTEMP = results.getString(2);
					lists.add(new ListsJSON(listidTEMP, nameTEMP));
				}
			} catch (SQLException e ){
				release(results);
				release();
				return ResponseCode.INTERNAL_ERROR;
			}
		}
		return ResponseCode.OK;
	}
	
	
}
