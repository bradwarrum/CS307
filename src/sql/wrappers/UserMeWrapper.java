package sql.wrappers;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

import com.google.gson.annotations.Expose;

import sql.SQLExecutable;
import sql.SQLParam;
import sql.SQLType;

public class UserMeWrapper extends SQLExecutable {
	@Expose(serialize = true)
	private int userID;
	@Expose(serialize = true)
	private String firstName;
	@Expose(serialize = true)
	private String lastName;
	@Expose(serialize = true)
	private String emailAddress;
	@Expose(serialize = true)
	private List<HouseholdListJSON> households;
	
	private static class HouseholdListJSON {
		@Expose(serialize = true)
		private final int householdID;
		@Expose(serialize = true)
		private final String householdName;
		@Expose(serialize = true)
		private final String householdDescription;
		
		public HouseholdListJSON(int householdID, String householdName, String householdDescription) {
			this.householdID = householdID;
			this.householdDescription = householdDescription;
			this.householdName = householdName;
		}
	}
	
	public UserMeWrapper(int userID) {
		this.userID = userID;
	}
	
	public boolean fetch() {
		ResultSet results = null;
		households = new ArrayList<HouseholdListJSON> ();
		try {
			results = query("SELECT Household.HouseholdId, Household.Name, Household.Description FROM HouseholdPermissions "
					+ "INNER JOIN Household ON (Household.HouseholdId=HouseholdPermissions.HouseholdId) "
					+ "WHERE (HouseholdPermissions.UserId=?);",
					new SQLParam(userID, SQLType.INT));
			if (results == null) {release(); return false;}
			String nameTEMP;
			String descTEMP;
			int hidTEMP;
			while (results.next()) {
				hidTEMP = results.getInt(1);
				nameTEMP = results.getString(2);				
				descTEMP = results.getString(3);
				households.add(new HouseholdListJSON(hidTEMP, nameTEMP, descTEMP));
			}
		} catch (SQLException e) {
			release();
			return false;
		} finally {
			release(results);
		}
		try {
			results = query("SELECT Email, FirstName, LastName FROM User WHERE (UserId=?);",
					new SQLParam(userID, SQLType.INT));
			if (results == null) {release(); return false;}
			if (!results.next()) {release(results); release(); return false;}
			emailAddress = results.getString(1);
			firstName = results.getString(2);
			lastName = results.getString(3);
		} catch (SQLException e) {
			return false;
		} finally {
			release(results);
			release();
		}
		return true;
	}
	
}
