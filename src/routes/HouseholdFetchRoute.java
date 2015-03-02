package routes;

import java.io.IOException;

import sql.wrappers.HouseholdFetchWrapper;

import com.sun.net.httpserver.HttpExchange;

import core.Server;

public class HouseholdFetchRoute extends Route {
	@Override
	public void handle(HttpExchange xchg) throws IOException {
		if (!"get".equalsIgnoreCase(xchg.getRequestMethod())) {respond(xchg, 404); return;}
		int userID = -1;
		int householdID = (int) xchg.getAttribute("householdID");
		if ((userID = Server.sessionTable().authenticate(getToken(xchg))) < 0) {respond(xchg, 403); return;}
		if (householdID < 0) {respond(xchg,400); return;}
		HouseholdFetchWrapper hfw = new HouseholdFetchWrapper(userID, householdID);
		if (!hfw.fetch()) {respond(xchg, 500); return;}
		respond(xchg, 200, gson.toJson(hfw, HouseholdFetchWrapper.class));
	}
}
