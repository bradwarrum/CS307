package routes;

import java.io.IOException;

import sql.wrappers.HouseholdFetchWrapper;
import sql.wrappers.HouseholdFetchWrapper.HouseholdFetchResult;

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
		HouseholdFetchResult results = hfw.fetch();
		if (results == HouseholdFetchResult.INTERNAL_ERROR) {respond(xchg, 500); return;}
		else if (results == HouseholdFetchResult.HOUSEHOLD_NOT_FOUND) {error(xchg, 404, "Could not find a household with that ID."); return;}
		respond(xchg, 200, gson.toJson(hfw, HouseholdFetchWrapper.class));
	}
}
