package routes;

import java.io.IOException;

import sql.wrappers.RecipeFetchWrapper;

import com.sun.net.httpserver.HttpExchange;

import core.ResponseCode;
import core.Server;

public class HouseholdInviteCheckRoute extends Route{
	@Override
	public void handle(HttpExchange xchg) throws IOException {
		if (!"get".equalsIgnoreCase(xchg.getRequestMethod())) {respond(xchg, 404); return;}
		int householdID = (int)xchg.getAttribute("householdID");
		int recipeID = (int)xchg.getAttribute("userID");
		InviteCheckWrapper icw = new InviteCheckWrapper(userID, householdID);
		ResponseCode result = icw.fetch();
		if (!result.success()) 
			error(xchg, result);
		else {
			if (result == ResponseCode.OK)
				respond(xchg, 200, gson.toJson(rfw, RecipeFetchWrapper.class));
			else
				respond(xchg, 304);
				
		}
	}
}
