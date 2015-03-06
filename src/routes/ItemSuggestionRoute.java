package routes;

import java.io.IOException;

import sql.wrappers.ItemSuggestionWrapper;
import sql.wrappers.ItemSuggestionWrapper.ItemSuggestionResult;

import com.sun.net.httpserver.HttpExchange;

import core.Server;

public class ItemSuggestionRoute extends Route {
	@Override
	public void handle(HttpExchange xchg) throws IOException {
		if (!"get".equalsIgnoreCase(xchg.getRequestMethod())){
			respond(xchg, 404); 
			return;
		}
		int userID = -1;
		if ((userID = Server.sessionTable().authenticate(getToken(xchg))) < 0){
			respond(xchg, 403); 
			return;
		}
		int householdID = (int) xchg.getAttribute("householdID");
		if(householdID<0){
			respond(xchg,400);
			return;			
		}
		String UPC=(String) xchg.getAttribute("UPC");
		ItemSuggestionWrapper idw= new ItemSuggestionWrapper(householdID, userID, UPC);
		ItemSuggestionResult results = idw.fetch();
		if (results == ItemSuggestionResult.INTERNAL_ERROR) {
			respond(xchg,500);
			return;
		} else if (results == ItemSuggestionResult.HOUSEHOLD_NOT_FOUND) {
			error(xchg, 404, "Household " + householdID + " does not exist for that user.");
			return;
		}
		respond(xchg,200,gson.toJson(idw,ItemSuggestionWrapper.class));
	}
}

