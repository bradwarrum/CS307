package sql.wrappers;

import java.sql.ResultSet;
import java.sql.SQLException;

import sql.SQLExecutable;
import sql.SQLParam;
import sql.SQLType;

/**
 * Provides functions common to most wrappers for convenience
 * @author Brad
 *
 */
public abstract class BaseWrapper extends SQLExecutable {

	/**
	 * Gets the permission level for a specfic userID and householdID
	 * @param permUserID The userID for which permissions are being checked
	 * @param permHouseholdID The householdID the user belongs to providing the permission level
	 * @return Returns -1 if there was an error executing the query.
	 * Returns -2 if the user is not registered with the householdID provided.
	 * Returns the raw integer permission level of the user in all other circumstances.
	 * Note that all valid permission levels are guaranteed to be positive.
	 */
	protected int getPermissions (int permUserID, int permHouseholdID) {
		ResultSet results = null;
		try{
			results = query("SELECT PermissionLevel FROM HouseholdPermissions WHERE UserId=? AND HouseholdId=?;",
					new SQLParam(permUserID, SQLType.INT),
					new SQLParam(permHouseholdID, SQLType.INT));

		} catch (SQLException e) {
			release();
			return -1;
		}
		int permissionRaw = 0;
		try {
			if (results == null) { release(); return -1;}
			if	(!results.next()) { release(results); release(); return -2;}
			permissionRaw = results.getInt(1);
		} catch (SQLException e) {
			release();
			return -1;
		}finally {
			release(results);
		}
		return permissionRaw;
	}
}
