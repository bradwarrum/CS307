package routes;

import java.io.IOException;

import sql.wrappers.InventoryDeleteWrapper;

import com.sun.net.httpserver.HttpExchange;

import core.ResponseCode;
import core.Server;

public class InventoryDeleteRoute extends Route {

	@Override
	public void handle(HttpExchange xchg) throws IOException {
		if (!"post".equalsIgnoreCase(xchg.getRequestMethod())) {respond(xchg, 404); return;}
		
		int userID = Server.sessionTable().authenticate(getToken(xchg));
		if (userID == -2) {error(xchg, ResponseCode.TOKEN_EXPIRED); return;}
		else if (userID == -1) {error(xchg, ResponseCode.INVALID_TOKEN); return;}
		
		int householdID = (int)xchg.getAttribute("householdID");
		String UPC = (String) xchg.getAttribute("UPC");
		
		InventoryDeleteWrapper idw = new InventoryDeleteWrapper(userID, householdID, UPC);
		ResponseCode result = idw.delete();
		if (!result.success()) 
			error(xchg, result);
		else {
			xchg.getResponseHeaders().set("ETag",
					"\"" + idw.getVersion() + "\"");
			respond(xchg, 200, gson.toJson(idw, InventoryDeleteWrapper.class));
		}
	}
}
