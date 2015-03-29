package routes;

import java.io.IOException;

import sql.wrappers.InventoryFetchWrapper;

import com.sun.net.httpserver.HttpExchange;

import core.ResponseCode;
import core.Server;

public class InventoryFetchRoute extends Route {
	@Override
	public void handle(HttpExchange xchg) throws IOException {
		if (!"get".equalsIgnoreCase(xchg.getRequestMethod())) {respond(xchg, 404); return;}
		int householdID = (int)xchg.getAttribute("householdID");
		
		int userID = Server.sessionTable().authenticate(getToken(xchg));
		if (userID == -2) {error(xchg, ResponseCode.TOKEN_EXPIRED); return;}
		else if (userID == -1) {error(xchg, ResponseCode.INVALID_TOKEN); return;}
		
		long etag = 0;
		String etagstr = xchg.getRequestHeaders().getFirst("If-None-Match");
		if (etagstr != null) {
			try {
				etag = Long.parseLong(etagstr.replaceAll("\"", ""));
			}catch (NumberFormatException e) {
				etag = 0;
			}
		}
		InventoryFetchWrapper ifw = new InventoryFetchWrapper(userID, householdID, etag);
		ResponseCode result = ifw.fetch();
		if (!result.success()) 
			error(xchg, result);
		else {
			xchg.getResponseHeaders().set("ETag", "\"" + ifw.getVersion() + "\"");
			if (result == ResponseCode.OK)
				respond(xchg, 200, gson.toJson(ifw, InventoryFetchWrapper.class));
			else
				respond(xchg, 304);
				
		}
	}
}
