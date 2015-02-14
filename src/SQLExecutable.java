import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.LinkedList;
import java.util.List;
import java.util.Queue;
abstract public class SQLExecutable  {
	
	private static Datasource ds = null;
	private Connection c = null;
	private Queue<Statement> stmts = new LinkedList<Statement>();
	public final static void setSharedDatasource(Datasource d) {
		ds = d;
	}
	
	protected final int update(String SQLStatement, SQLParam... parameters) throws Exception {
		if (!verifyConnection())
			throw new Exception("Shared data source is invalid, or connection cannot be established on data source");
		int val = 0;
		PreparedStatement ps = null;
		try {
			ps = c.prepareStatement(SQLStatement);
			fillParameters(ps, parameters);
			val =  ps.executeUpdate();
		} catch (SQLException sqle) {
			throw new Exception ("Could not prepare statement.", sqle);
		}
		try {
			if (ps != null ) ps.close();
		} catch (SQLException sqle) {
			System.out.println("Possible leak: Could not close PreparedStatement");
		} 
		return val;
	}
	
	protected final int update(String SQLStatement, List<SQLParam> parameters) throws Exception {
		return update(SQLStatement, (SQLParam[]) parameters.toArray());
	}
	
	protected final int update(String SQLStatement) throws Exception {
		if (!verifyConnection())
			throw new Exception("Shared data source is invalid, or connection cannot be established on data source");
		Statement s = null;
		int val = 0;
		try {
			s = c.createStatement();
			val =  s.executeUpdate(SQLStatement);
		} catch (SQLException sqle) {
			throw new Exception ("Could not prepare statement.", sqle);
		}
		try {
			if (s != null) s.close();
		} catch (SQLException sqle) {
			System.out.println("Possible leak: Could not close Statement");
		}
		return val;
	}
	
	protected final ResultSet query(String SQLStatement, SQLParam...parameters) throws Exception {
		if (!verifyConnection())
			throw new Exception("Shared data source is invalid, or connection cannot be established on data source");
		try {
			PreparedStatement ps = c.prepareStatement(SQLStatement);
			stmts.add(ps);
			fillParameters(ps, parameters);
			return ps.executeQuery();
		} catch (SQLException sqle) {
			throw new Exception ("Could not prepare statement.", sqle);
		}
	}
	
	protected final ResultSet query(String SQLStatement, List<SQLParam> parameters) throws Exception {
		return query(SQLStatement, (SQLParam[]) parameters.toArray());
	
	}
	
	protected final ResultSet query (String SQLStatement) throws Exception {
		if (!verifyConnection())
			throw new Exception("Shared data source is invalid, or connection cannot be established on data source");
		try {
			Statement s = c.createStatement();
			stmts.add(s);
			return s.executeQuery(SQLStatement);
		} catch (SQLException sqle) {
			throw new Exception ("Could not prepare statement.", sqle);
		}
	}
	
	private final void fillParameters(PreparedStatement ps, SQLParam[] parameters) throws SQLException {
		int i = 0;
		for (SQLParam p : parameters) {
			SQLType t = p.getType();
			Object o = p.getValue();
			if (o == null) 
				ps.setNull(i, t.getValue());
			else if (t == SQLType.AUTO) 
				ps.setObject(i, o);
			else 
				ps.setObject(i, o, t.getValue());
			i++;
		}
	}
	
	private final boolean verifyConnection() {
		if (ds == null) return false;
		if (c == null) {
			try {
				c = ds.getConnection();
			} catch (SQLException s) {
				return false;
			}
		}
		return true;
	}
	/**
	 * Closes all open statements and frees the connection.
	 * <p>
	 * This method should be called as often as possible to ensure the connection is returned to the connection pool when no longer needed.
	 */
	public final void release() {
		//Release statements first
		while (!stmts.isEmpty()) {
			Statement s = stmts.poll();
			if (s != null) {
				try {
					s.close();
				} catch (SQLException e) {
					System.out.println("Possible leak: Could not close Statement");
				}
			}
		}
		if (c != null) {
			try {
				c.close();
			} catch (SQLException e) {
				System.out.println("Error closing connection");
			} finally {
				c = null;
			}
		}
	}
}
