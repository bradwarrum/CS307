package routes;

import java.io.IOException;

import sql.wrappers.ListDeleteWrapper;

import com.sun.net.httpserver.HttpExchange;

import core.ResponseCode;
import core.Server;

public class ListDeleteRoute extends Route {
	@Override
	public void handle(HttpExchange xchg) throws IOException {
		if (!"post".equalsIgnoreCase(xchg.getRequestMethod())) {respond(xchg, 404); return;}
		int householdID = (int)xchg.getAttribute("householdID");
		int listID = (int)xchg.getAttribute("listID");
		
		int userID = Server.sessionTable().authenticate(getToken(xchg));
		if (userID == -2) {error(xchg, ResponseCode.TOKEN_EXPIRED); return;}
		else if (userID == -1) {error(xchg, ResponseCode.INVALID_TOKEN); return;}
		
		ListDeleteWrapper ldw = new ListDeleteWrapper(userID, listID, householdID);
		ResponseCode result = ldw.delete();
		if (!result.success())
			error(xchg, result);
		else
			respond(xchg, result.getHttpCode());
		
	}
}
