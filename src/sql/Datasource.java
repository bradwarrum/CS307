package sql;
import java.beans.PropertyVetoException;
import java.sql.Connection;
import java.sql.SQLException;
import java.util.Properties;

import com.mchange.v2.c3p0.*;
public class Datasource {
	private ComboPooledDataSource cpds;
	public Datasource() {
		cpds = new ComboPooledDataSource();
	}
	public static void setLoggingPref() {
		Properties p = new Properties(System.getProperties());
		p.put("com.mchange.v2.log.MLog", "com.mchange.v2.log.FallbackMLog");
		p.put("com.mchange.v2.log.FallbackMLog.DEFAULT_CUTOFF_LEVEL", "WARNING");
		System.setProperties(p);
	}
	public boolean connect() {
		try {
			cpds.setDriverClass("com.mysql.jdbc.Driver");
			cpds.setUser("root");
			cpds.setPassword("cs307team");
			cpds.setInitialPoolSize(5);
			cpds.setMaxPoolSize(50);
			cpds.setMinPoolSize(5);
			cpds.setAcquireIncrement(5);
			cpds.setJdbcUrl("jdbc:mysql://localhost/testdb");
			
			cpds.setUnreturnedConnectionTimeout(60); // Any connection not returned within 60 seconds is assumed to be orphaned
			cpds.setMaxIdleTime(3600); //Connections idling for more than 1 hour are returned to the pool
			cpds.setMaxIdleTimeExcessConnections(300); //Connections in excess of minimum pool size are shrunk back to min size quickly
			boolean validity;
			try (Connection c = cpds.getConnection()) {
				validity = c.isValid(10);
			} catch (SQLException e) {
				return false;
			}
			return validity;
		} catch (PropertyVetoException pve) {
			return false;
		}
	}
	
	public Connection getConnection() throws SQLException {
		return cpds.getConnection();
	}
}
