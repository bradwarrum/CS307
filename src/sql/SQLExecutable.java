package sql;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.LinkedList;
import java.util.List;
import java.util.Queue;
abstract public class SQLExecutable  {
	
	private static SQLDatasource ds = null;
	private Connection c = null;
	private Queue<Statement> stmts = new LinkedList<Statement>();
	public final static void setSharedDatasource(SQLDatasource d) {
		ds = d;
	}
	
	protected final int update(String SQLStatement, SQLParam... parameters) throws SQLException {
		verifyConnection();
		int val = 0;
		PreparedStatement ps = null;
		try {
			ps = c.prepareStatement(SQLStatement);
			fillParameters(ps, parameters);
			val =  ps.executeUpdate();
		} catch (SQLException sqle) {
			throw sqle;
		}finally {
			try {
				if (ps != null ) ps.close();
			} catch (SQLException sqle) {
				System.out.println("Possible leak: Could not close PreparedStatement");
			} 
		}
		return val;
	}
	
	protected final int update(String SQLStatement, List<SQLParam> parameters) throws SQLException {
		return update(SQLStatement, (SQLParam[]) parameters.toArray());
	}
	
	protected final int update(String SQLStatement) throws SQLException {
		verifyConnection();
		Statement s = null;
		int val = 0;
		try {
			s = c.createStatement();
			val =  s.executeUpdate(SQLStatement);
		} catch (SQLException sqle) {
			throw sqle;
		} finally {
			try {
				if (s != null) s.close();
			} catch (SQLException sqle) {
				System.out.println("Possible leak: Could not close Statement");
			}
		}
		return val;
	}
	
	protected final ResultSet query(String SQLStatement, SQLParam...parameters) throws SQLException {
		verifyConnection();
		PreparedStatement ps = c.prepareStatement(SQLStatement);
		stmts.add(ps);
		fillParameters(ps, parameters);
		return ps.executeQuery();
	}
	
	protected final ResultSet query(String SQLStatement, List<SQLParam> parameters) throws SQLException {
		return query(SQLStatement, (SQLParam[]) parameters.toArray());
	
	}
	
	protected final ResultSet query (String SQLStatement) throws SQLException {
		verifyConnection();
		Statement s = c.createStatement();
		stmts.add(s);
		return s.executeQuery(SQLStatement);
	}
	
	private final void fillParameters(PreparedStatement ps, SQLParam[] parameters) throws SQLException {
		int i = 1;
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
	
	private final void verifyConnection() throws SQLException{
		if (ds == null) throw new SQLException("The DataSource object is not set up.", "HY000");
		if (c == null) {
				c = ds.getConnection();
				c.setAutoCommit(false);
				c.setTransactionIsolation(Connection.TRANSACTION_REPEATABLE_READ);
		}
	}
	/**
	 * Closes all open statements and frees the connection.  This MUST be called to commit a transaction.
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
				c.commit();
				c.close();
			} catch (SQLException e) {
				System.out.println("Error closing connection");
			} finally {
				c = null;
			}
		}
	}
	/**
	 * Releases a ResultSet resource.<p>
	 * Has no effect if the ResultSet is null or already closed.
	 * @param rs The ResultSet to close.
	 */
	public final void release(ResultSet rs) {
		if (rs == null) return;
		try {
			rs.close();
		}catch (SQLException sqle) {
				System.out.println("Possible leak: Could not close ResultSet");
		}
	}
	
	/**
	 * If changes have been made within a transaction, this method rolls back all changes made.
	 */
	public final void rollback() {
		if (c != null) {
			try {
				c.rollback();
			} catch (SQLException e) {
				System.out.println("WARNING: Could not rollback SQL query. Database may be in an unstable state.");
			} 
		}
	}
}
