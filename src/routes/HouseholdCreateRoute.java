package routes;

import java.io.IOException;

import sql.wrappers.HouseholdCreationWrapper;

import com.google.gson.JsonSyntaxException;
import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;
import com.sun.net.httpserver.HttpExchange;

import core.ResponseCode;
import core.Server;

public class HouseholdCreateRoute extends Route {
	
	@Override
	public void handle(HttpExchange xchg) throws IOException {
		if (!"post".equalsIgnoreCase(xchg.getRequestMethod())) {respond(xchg, 404); return;}
		String path = xchg.getRequestURI().getPath();
		if (!path.equals("/households/create")) {respond(xchg, 404); return;}
		int userID = Server.sessionTable().authenticate(getToken(xchg));
		if (userID == -2) {error(xchg, ResponseCode.TOKEN_EXPIRED); return;}
		else if (userID == -1) {error(xchg, ResponseCode.INVALID_TOKEN); return;}
		String request = getRequest(xchg.getRequestBody());
		HouseholdCreationJSON hcrt = null;
		try {
			hcrt = gson.fromJson(request, HouseholdCreationJSON.class);
		} catch (JsonSyntaxException e) {
			error(xchg, ResponseCode.INVALID_PAYLOAD); return;
		}
		if (hcrt == null || !hcrt.valid()) {error(xchg, ResponseCode.INVALID_PAYLOAD); return;}
		hcrt.clean();
		HouseholdCreationWrapper hcw = new HouseholdCreationWrapper(hcrt.name, hcrt.description, userID);
		ResponseCode result = hcw.create();
		if (!result.success())
			error(xchg, result);
		else 
			respond(xchg, result.getHttpCode(), gson.toJson(hcw));
	}
	
	private static class HouseholdCreationJSON {
		@SerializedName("householdName")
		@Expose(deserialize = true)
		String name;
		@SerializedName("householdDescription")
		@Expose(deserialize = true)
		String description;
		
		public boolean valid() {
			return (this.name != null && this.name.length() < 20 && ((this.description != null) ? this.description.length() < 40 : true));
		}
		
		public void clean() {
			name = name.trim();
			if (description != null) {
				description = description.trim();
			}
		}
	}

}
