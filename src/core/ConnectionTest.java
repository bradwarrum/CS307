package core;
import java.io.FileNotFoundException;
import java.io.PrintStream;

import sql.SQLDatasource;
import sql.SQLExecutable;

// Notice, do not import com.mysql.jdbc.*
// or you will have problems!

public class ConnectionTest {
    public static void main(String[] args) throws Exception {
    	setErrorStream();
    	SQLDatasource.setLoggingPref();
        SQLDatasource d = new SQLDatasource();
    	SQLExecutable.setSharedDatasource(d);
        if(d.connect()) System.out.println("Connection valid.");

        
    }
    private static void setErrorStream() {
    	try {
    		PrintStream ps = new PrintStream("errlog.log");
    		System.setErr(ps);
    	} catch (FileNotFoundException e) {
    		System.err.println(e.getMessage());
    	}
    }
}