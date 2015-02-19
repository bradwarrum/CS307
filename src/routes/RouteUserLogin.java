package routes;

import java.io.IOException;

import com.sun.net.httpserver.Headers;
import com.sun.net.httpserver.HttpExchange;


import com.google.gson.*;
import com.google.gson.annotations.Expose;

import sql.*;
import sql.UserLoginModel.AuthResult;

public class RouteUserLogin extends Route {
	@Override
	public void handle(HttpExchange xchg) throws IOException {
		System.out.println("Got user login request");
		if (!"post".equalsIgnoreCase(xchg.getRequestMethod())) {respond(xchg, 404); return;}
		String request = getRequest(xchg.getRequestBody());
		UserCredentials cred;
		try {
			cred = gson.fromJson(request, UserCredentials.class);
		}catch (JsonSyntaxException jse) {
			respond(xchg, 403); return;
		}
		if (cred == null || cred.password == null || cred.emailAddress == null) {respond(xchg, 400, "Malformed input."); return;}
		UserLoginModel ulm = new UserLoginModel(cred.emailAddress, cred.password);
		AuthResult res = ulm.isAuthenticated();
		if (res == AuthResult.INTERNAL_ERROR) respond(xchg, 500);
		else if (res == AuthResult.INVALID_PWD) error(xchg, 403, "Password invalid.");
		else if (res == AuthResult.USER_NOT_FOUND) error(xchg, 404, "No user found with that email.");
		else if (res == AuthResult.MALFORMED_INPUT) error(xchg, 400, "Malformed input.");
		else {
			ulm.produceToken();
			respond(xchg, 200, gson.toJson(ulm));
		}
	}
	public static class UserCredentials {
		@Expose()
		public String emailAddress;
		@Expose()
		public String password;
	}

}

