package routes;

import java.io.IOException;

import sql.wrappers.ListUpdateWrapper;

import com.google.gson.JsonSyntaxException;
import com.sun.net.httpserver.HttpExchange;

import core.ResponseCode;
import core.Server;
import core.json.VersionedUpdateListJSON;

public class ListUpdateRoute extends Route {
	@Override
	public void handle(HttpExchange xchg) throws IOException {
		if (!"post".equalsIgnoreCase(xchg.getRequestMethod())) {respond(xchg, 404); return;}
		
		int userID = Server.sessionTable().authenticate(getToken(xchg));
		if (userID == -2) {error(xchg, ResponseCode.TOKEN_EXPIRED); return;}
		else if (userID == -1) {error(xchg, ResponseCode.INVALID_TOKEN); return;}
		
		int householdID = (int)xchg.getAttribute("householdID");
		int listID = (int)xchg.getAttribute("listID");
		String request = getRequest(xchg.getRequestBody());
		VersionedUpdateListJSON luj = null;
		try {
			luj = gson.fromJson(request, VersionedUpdateListJSON.class);
		} catch (JsonSyntaxException e) {
			error(xchg, ResponseCode.INVALID_PAYLOAD); return;
		}
		if (luj == null || !luj.valid()) { error(xchg, ResponseCode.INVALID_PAYLOAD); return;}
		ListUpdateWrapper luw = new ListUpdateWrapper(userID, householdID, listID, luj);
		ResponseCode result = luw.update();
		if (!result.success()) 
			error(xchg, result);
		else {
			xchg.getResponseHeaders().set("ETag",
					"\"" + luw.getTimestamp() + "\"");
			respond(xchg, 200, gson.toJson(luw, ListUpdateWrapper.class));
		}
	}
}
