package routes;

import java.io.IOException;

import sql.wrappers.UserLoginWrapper;
import sql.wrappers.UserLoginWrapper.AuthResult;

import com.google.gson.JsonSyntaxException;
import com.google.gson.annotations.Expose;
import com.sun.net.httpserver.HttpExchange;

public class UserLoginRoute extends Route {
	@Override
	public void handle(HttpExchange xchg) throws IOException {
		if (!"post".equalsIgnoreCase(xchg.getRequestMethod())) {respond(xchg, 404); return;}
		String request = getRequest(xchg.getRequestBody());
		UserCredentials cred;
		try {
			cred = gson.fromJson(request, UserCredentials.class);
		}catch (JsonSyntaxException jse) {
			respond(xchg, 400); return;
		}
		if (cred == null || !cred.valid()) {error(xchg, 400, "Malformed input."); return;}
		cred.clean();
		UserLoginWrapper ulm = new UserLoginWrapper(cred.emailAddress, cred.password);
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
		
		public boolean valid() {
			return (this.password != null && this.emailAddress != null);
		}
		
		public void clean() {
			emailAddress = emailAddress.trim();
			password = password.trim();
		}
	}

}

