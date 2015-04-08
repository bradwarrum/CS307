package routes;

import java.io.IOException;

import sql.wrappers.RecipeCreateWrapper;

import com.google.gson.JsonSyntaxException;
import com.google.gson.annotations.Expose;
import com.sun.net.httpserver.HttpExchange;

import core.ResponseCode;
import core.Server;

public class RecipeCreateRoute extends Route {
	@Override
	public void handle(HttpExchange xchg) throws IOException {
		if (!"post".equalsIgnoreCase(xchg.getRequestMethod())) {respond(xchg, 404); return;}
		int householdID = (int)xchg.getAttribute("householdID");
		int userID = Server.sessionTable().authenticate(getToken(xchg));
		if (userID == -2) {error(xchg, ResponseCode.TOKEN_EXPIRED); return;}
		else if (userID == -1) {error(xchg, ResponseCode.INVALID_TOKEN); return;}
		String request = getRequest(xchg.getRequestBody());
		RecipeCreateJSON rcj = null;
		try {
			rcj = gson.fromJson(request, RecipeCreateJSON.class);
		} catch (JsonSyntaxException e) {
			error(xchg, ResponseCode.INVALID_PAYLOAD);
			return;
		}
		if (rcj == null || !rcj.valid() || householdID < 0) {error(xchg, ResponseCode.INVALID_PAYLOAD); return;}
		rcj.trim();
		RecipeCreateWrapper rcw = new RecipeCreateWrapper(userID, householdID, rcj.recipeName, rcj.recipeDescription);
		ResponseCode result = rcw.create();
		if (!result.success()) 
			error(xchg, result);
		else
			respond(xchg, result.getHttpCode(), gson.toJson(rcw, RecipeCreateWrapper.class));
	}
	
	public static class RecipeCreateJSON {
		@Expose(deserialize = true)
		public String recipeName;
		@Expose(deserialize = true)
		public String recipeDescription;
		
		public boolean valid () {
			//Feel free to edit these character limits
			if (recipeName == null || recipeName.length() > 40) return false;
			if (recipeDescription == null || recipeName.length() > 128) return false;
			return true;
		}
		
		public void trim() {
			recipeName = recipeName.trim();
			recipeDescription = recipeDescription.trim();
		}
	}
}
