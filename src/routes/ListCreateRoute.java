package routes;

import java.io.IOException;

import sql.wrappers.ListCreateWrapper;

import com.google.gson.JsonSyntaxException;
import com.google.gson.annotations.Expose;
import com.sun.net.httpserver.HttpExchange;

import core.ResponseCode;
import core.Server;

public class ListCreateRoute extends Route {
	
	@Override
	public void handle(HttpExchange xchg) throws IOException {
		if (!"post".equalsIgnoreCase(xchg.getRequestMethod())) {respond(xchg, 404); return;}
		int householdID = (int)xchg.getAttribute("householdID");
		int userID = Server.sessionTable().authenticate(getToken(xchg));
		if (userID == -2) {error(xchg, ResponseCode.TOKEN_EXPIRED); return;}
		else if (userID == -1) {error(xchg, ResponseCode.INVALID_TOKEN); return;}
		String request = getRequest(xchg.getRequestBody());
		ListCreateJSON lcj = null;
		try {
			lcj = gson.fromJson(request, ListCreateJSON.class);
		} catch (JsonSyntaxException e) {
			error(xchg, ResponseCode.INVALID_PAYLOAD);
			return;
		}
		if (lcj == null || !lcj.valid() || householdID < 0) {error(xchg, ResponseCode.INVALID_PAYLOAD); return;}
		lcj.trim();
		ListCreateWrapper lcw = new ListCreateWrapper(userID, householdID, lcj.listName);
		ResponseCode result = lcw.create();
		if (!result.success()) 
			error(xchg, result);
		else
			respond(xchg, result.getHttpCode(), gson.toJson(lcw, ListCreateWrapper.class));
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
