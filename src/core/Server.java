package core;

import java.io.FileNotFoundException;
import java.io.PrintStream;
import java.net.InetSocketAddress;

import routes.*;
import sql.SQLDatasource;
import sql.SQLExecutable;

import com.sun.net.httpserver.HttpServer;

public class Server {

	private static SessionTable sessions = new SessionTable(10 * 60, 20);
	public static SessionTable sessionTable() {
		return sessions;
	}
    public static void main(String[] args) throws Exception {
    	setErrorStream();
    	SessionToken.startRNG();
    	SQLDatasource.setLoggingPref();
        SQLDatasource d = new SQLDatasource();
    	SQLExecutable.setSharedDatasource(d);
        if(d.connect()) System.out.println("Connection valid.");

        HttpServer server = HttpServer.create(new InetSocketAddress(8000), 0);
        //Setup routing
        server.createContext("/users/login", new UserLoginRoute());
        server.createContext("/users/register", new UserRegistrationRoute());
        server.createContext("/households/create", new HouseholdCreateRoute());
        System.out.println("Server running.");
        server.start();
        
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
