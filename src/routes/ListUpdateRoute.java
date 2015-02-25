package routes;

import java.io.IOException;

import com.google.gson.annotations.Expose;
import com.sun.net.httpserver.HttpExchange;

import core.Server;

public class ListUpdateRoute extends Route {
	@Override
	public void handle(HttpExchange xchg) throws IOException {
		if (!"post".equalsIgnoreCase(xchg.getRequestMethod())) {respond(xchg, 404); return;}
		int userID = -1;
		int householdID = (int)xchg.getAttribute("householdID");
		int listID = (int)xchg.getAttribute("listID");
		if ((userID = Server.sessionTable().authenticate(getToken(xchg))) < 0) {respond(xchg, 403); return;}
		String request = getRequest(xchg.getRequestBody());
		
	}
	private static class ListUpdateJSON {
		@Expose(deserialize = true)
		public long timestamp;
		@Expose(deserialize = true)
		public 
		
		public boolean valid() {
		}
		
		public void trim() {
		}
	}
}
