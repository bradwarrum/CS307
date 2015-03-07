package routes;

import java.io.IOException;

import sql.wrappers.UserLoginWrapper;

import com.google.gson.JsonSyntaxException;
import com.google.gson.annotations.Expose;
import com.sun.net.httpserver.HttpExchange;

import core.ResponseCode;

public class UserLoginRoute extends Route {
	@Override
	public void handle(HttpExchange xchg) throws IOException {
		if (!"post".equalsIgnoreCase(xchg.getRequestMethod())) {respond(xchg, 404); return;}
		String path = xchg.getRequestURI().getPath();
		if (!(path.equals("/users/login") || path.equals("/users/login/"))) {respond(xchg, 404); return;}
		String request = getRequest(xchg.getRequestBody());
		UserCredentials cred;
		try {
			cred = gson.fromJson(request, UserCredentials.class);
		}catch (JsonSyntaxException jse) {
			error(xchg, ResponseCode.INVALID_PAYLOAD); return;
		}
		if (cred == null || !cred.valid()) {error(xchg, ResponseCode.INVALID_PAYLOAD); return;}
		cred.clean();
		UserLoginWrapper ulm = new UserLoginWrapper(cred.emailAddress, cred.password);
		ResponseCode result = ulm.isAuthenticated();
		if (!result.success()) 
			error(xchg, result);
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

