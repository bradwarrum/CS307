package routes;

import java.io.IOException;

import sql.wrappers.InventoryUpdateWrapper;

import com.google.gson.JsonSyntaxException;
import com.sun.net.httpserver.HttpExchange;

import core.ResponseCode;
import core.Server;
import core.json.VersionedUpdateListJSON;

public class InventoryUpdateRoute extends Route {
	@Override
	public void handle(HttpExchange xchg) throws IOException {
		if (!"post".equalsIgnoreCase(xchg.getRequestMethod())) {respond(xchg, 404); return;}
		
		int userID = Server.sessionTable().authenticate(getToken(xchg));
		if (userID == -2) {error(xchg, ResponseCode.TOKEN_EXPIRED); return;}
		else if (userID == -1) {error(xchg, ResponseCode.INVALID_TOKEN); return;}
		
		int householdID = (int)xchg.getAttribute("householdID");
		String request = getRequest(xchg.getRequestBody());
		VersionedUpdateListJSON luj = null;
		try {
			luj = gson.fromJson(request, VersionedUpdateListJSON.class);
		} catch (JsonSyntaxException e) {
			error(xchg, ResponseCode.INVALID_PAYLOAD); return;
		}
		if (luj == null || !luj.valid()) { error(xchg, ResponseCode.INVALID_PAYLOAD); return;}
		
		InventoryUpdateWrapper iuw = new InventoryUpdateWrapper(userID, householdID, luj);
		ResponseCode result = iuw.update();
		if (!result.success()) 
			error(xchg, result);
		else {
			xchg.getResponseHeaders().set("ETag",
					"\"" + iuw.getVersion() + "\"");
			respond(xchg, 200, gson.toJson(iuw, InventoryUpdateWrapper.class));
		}
	}
}
