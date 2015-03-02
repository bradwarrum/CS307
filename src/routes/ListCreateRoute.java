package routes;

import java.io.IOException;

import sql.wrappers.ListCreateWrapper;
import sql.wrappers.ListCreateWrapper.ListCreateResult;

import com.google.gson.JsonSyntaxException;
import com.google.gson.annotations.Expose;
import com.sun.net.httpserver.HttpExchange;

import core.Server;

public class ListCreateRoute extends Route {
	
	@Override
	public void handle(HttpExchange xchg) throws IOException {
		if (!"post".equalsIgnoreCase(xchg.getRequestMethod())) {respond(xchg, 404); return;}
		int userID = -1;
		int householdID = (int)xchg.getAttribute("householdID");
		if ((userID = Server.sessionTable().authenticate(getToken(xchg))) < 0) {respond(xchg, 403); return;}
		String request = getRequest(xchg.getRequestBody());
		ListCreateJSON lcj = null;
		try {
			lcj = gson.fromJson(request, ListCreateJSON.class);
		} catch (JsonSyntaxException e) {
			respond(xchg, 400);
			return;
		}
		if (lcj == null || !lcj.valid() || householdID < 0) {respond(xchg, 400); return;}
		lcj.trim();
		ListCreateWrapper lcw = new ListCreateWrapper(userID, householdID, lcj.listName);
		ListCreateResult result = lcw.create();
		if (result == ListCreateResult.INTERNAL_ERROR) {respond(xchg, 500);}
		else if (result == ListCreateResult.HOUSEHOLD_NOT_FOUND) {error(xchg, 404, "Household was not found for that user.");}
		else if (result == ListCreateResult.INSUFFICIENT_PERMISSIONS) {error(xchg, 403, "Insufficient permission level.");}
		else if (result == ListCreateResult.CREATED) {respond(xchg, 201, gson.toJson(lcw, ListCreateWrapper.class));}
	}
	
	private static class ListCreateJSON {
		@Expose(deserialize = true)
		public String listName;
		
		public boolean valid() {
			return (listName != null && listName.length() <= 40);
		}
		
		public void trim() {
			listName = listName.trim();
		}
	}
}
