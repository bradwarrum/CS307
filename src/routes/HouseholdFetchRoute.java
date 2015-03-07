package routes;

import java.io.IOException;

import sql.wrappers.HouseholdFetchWrapper;

import com.sun.net.httpserver.HttpExchange;

import core.ResponseCode;
import core.Server;

public class HouseholdFetchRoute extends Route {
	@Override
	public void handle(HttpExchange xchg) throws IOException {
		if (!"get".equalsIgnoreCase(xchg.getRequestMethod())) {respond(xchg, 404); return;}
		int householdID = (int) xchg.getAttribute("householdID");
		int userID = Server.sessionTable().authenticate(getToken(xchg));
		if (userID == -2) {error(xchg, ResponseCode.TOKEN_EXPIRED); return;}
		else if (userID == -1) {error(xchg, ResponseCode.INVALID_TOKEN); return;}
		HouseholdFetchWrapper hfw = new HouseholdFetchWrapper(userID, householdID);
		ResponseCode results = hfw.fetch();
		if (!results.success())
			error(xchg, results);
		else
			respond(xchg, results.getHttpCode(), gson.toJson(hfw, HouseholdFetchWrapper.class));
	}
}
