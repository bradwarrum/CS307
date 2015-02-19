package core;
import java.io.FileNotFoundException;
import java.io.PrintStream;
import java.net.InetSocketAddress;

import sql.Datasource;
import sql.SQLExecutable;

import com.sun.net.httpserver.*;

// Notice, do not import com.mysql.jdbc.*
// or you will have problems!

public class ConnectionTest {
    public static void main(String[] args) throws Exception {
    	setErrorStream();
    	Datasource.setLoggingPref();
        Datasource d = new Datasource();
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