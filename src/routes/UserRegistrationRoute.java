package routes;

import java.io.IOException;

import sql.wrappers.UserRegistrationWrapper;

import com.google.gson.JsonSyntaxException;
import com.google.gson.annotations.Expose;
import com.sun.net.httpserver.HttpExchange;

import core.ResponseCode;

public class UserRegistrationRoute extends Route {
	@Override
	public void handle(HttpExchange xchg) throws IOException {
		if (!"post".equalsIgnoreCase(xchg.getRequestMethod())) {respond(xchg, 404); return;}
		String path = xchg.getRequestURI().getPath();
		if (!(path.equals("/users/register") || path.equals("/users/register/"))) {respond(xchg, 404); return;}
		String request = getRequest(xchg.getRequestBody());
		UserInformation info;
		try {
			info = gson.fromJson(request, UserInformation.class);
		}catch (JsonSyntaxException jse) {
			error(xchg, ResponseCode.INVALID_PAYLOAD); return;
		}
		if (info == null || !info.valid()) {error(xchg, ResponseCode.INVALID_PAYLOAD); return;}
		info.clean();
		UserRegistrationWrapper urm = new UserRegistrationWrapper(info.emailAddress, info.firstName, info.lastName, info.password);
		ResponseCode result = urm.register();
		if (!result.success()) 
			error(xchg, result);
		else
			respond(xchg, result.getHttpCode());
	}
	
	public static class UserInformation {
		@Expose(deserialize = true)
		public String emailAddress = null;
		@Expose(deserialize = true)
		public String firstName = null;
		@Expose(deserialize = true)
		public String lastName = null;
		@Expose(deserialize = true)
		public String password = null;
		
		public boolean valid() {
			return (emailAddress != null && firstName != null && lastName != null && password != null &&
					emailAddress.length() < 254 && firstName.length() < 20 && lastName.length() < 20);
		}
		
		public void clean() {
			emailAddress = emailAddress.trim();
			firstName = firstName.trim();
			lastName = lastName.trim();
			password = password.trim();
		}
		
	}
}
