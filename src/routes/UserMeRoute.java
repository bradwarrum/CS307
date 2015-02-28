package routes;

import java.io.IOException;

import sql.wrappers.UserMeWrapper;

import com.sun.net.httpserver.HttpExchange;

import core.Server;

public class UserMeRoute extends Route {
	@Override
	public void handle(HttpExchange xchg) throws IOException {
		if (!"get".equalsIgnoreCase(xchg.getRequestMethod())) {respond(xchg, 404); return;}
		if (!xchg.getRequestURI().getPath().equals("/users/me")) {respond(xchg, 404); return;}
		int userID = -1;
		if ((userID = Server.sessionTable().authenticate(getToken(xchg))) < 0) {respond(xchg, 403); return;}
		UserMeWrapper umw = new UserMeWrapper(userID);
		if (!umw.fetch()) {respond(xchg, 500); return;}
		respond(xchg, 200, gson.toJson(umw, UserMeWrapper.class));
		
	}
}
