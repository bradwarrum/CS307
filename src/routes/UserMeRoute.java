package routes;

import java.io.IOException;

import sql.wrappers.UserMeWrapper;

import com.sun.net.httpserver.HttpExchange;

import core.ResponseCode;
import core.Server;

public class UserMeRoute extends Route {
	@Override
	public void handle(HttpExchange xchg) throws IOException {
		if (!"get".equalsIgnoreCase(xchg.getRequestMethod())) {respond(xchg, 404); return;}
		if (!xchg.getRequestURI().getPath().equals("/users/me")) {respond(xchg, 404); return;}
		
		int userID = Server.sessionTable().authenticate(getToken(xchg));
		if (userID == -2) {error(xchg, ResponseCode.TOKEN_EXPIRED); return;}
		else if (userID == -1) {error(xchg, ResponseCode.INVALID_TOKEN); return;}
		
		UserMeWrapper umw = new UserMeWrapper(userID);
		if (!umw.fetch()) {error(xchg, ResponseCode.INTERNAL_ERROR); return;}
		respond(xchg, 200, gson.toJson(umw, UserMeWrapper.class));
		
	}
}
