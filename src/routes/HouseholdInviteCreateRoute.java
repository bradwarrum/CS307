package routes;

import java.io.IOException;

import sql.wrappers.RecipeCreateWrapper;

import com.google.gson.JsonSyntaxException;
import com.google.gson.annotations.Expose;
import com.sun.net.httpserver.HttpExchange;

import core.ResponseCode;
import core.Server;

public class HouseholdInviteCreateRoute extends Route {
	@Override
	public void handle(HttpExchange xchg)throws IOException{
		if(!"post".equalsIgnoreCase(xchg.getRequestMethod())){
			respond(xchg,404);
			return;
		}
		int householdID= (int)xchg.getAttribute("householdID");
		int userID = Server.sessionTable().authenticate(getToken(xchg));
		if (userID == -2) {error(xchg, ResponseCode.TOKEN_EXPIRED); return;}
		else if (userID == -1) {error(xchg, ResponseCode.INVALID_TOKEN); return;}
		InviteCreateWrapper icw= new InviteCreateWrapper(userID,householdID);
		ResponseCode result= icw.create();
		if(!result.success())
			error(xchg,result);
		else
			respond(xchg,result.getHttpCode(),gson.toJson(icw,InviteCreateWrapper.class));
	}
}
