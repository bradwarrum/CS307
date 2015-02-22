package routes;

import java.io.IOException;

import sql.wrappers.HouseholdCreationWrapper;
import sql.wrappers.HouseholdCreationWrapper.HouseholdCreationResult;

import com.google.gson.JsonSyntaxException;
import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;
import com.sun.net.httpserver.HttpExchange;

import core.Server;

public class HouseholdCreateRoute extends Route {
	
	@Override
	public void handle(HttpExchange xchg) throws IOException {
		if (!"post".equalsIgnoreCase(xchg.getRequestMethod())) {respond(xchg, 404); return;}
		String path = xchg.getRequestURI().getPath();
		if (!path.equals("/households/create")) {respond(xchg, 404); return;}
		int userID = -1;
		if ((userID = Server.sessionTable().authenticate(getToken(xchg))) < 0) {respond(xchg, 403); return;}
		String request = getRequest(xchg.getRequestBody());
		HouseholdCreationJSON hcrt = null;
		try {
			hcrt = gson.fromJson(request, HouseholdCreationJSON.class);
		} catch (JsonSyntaxException e) {
			respond(xchg, 400); return;
		}
		if (hcrt == null || !hcrt.valid()) {error(xchg, 400, "Malformed input."); return;}
		hcrt.clean();
		HouseholdCreationWrapper hcw = new HouseholdCreationWrapper(hcrt.name, hcrt.description, userID);
		HouseholdCreationResult result = hcw.create();
		if (result == HouseholdCreationResult.INTERNAL_ERROR) respond(xchg, 500);
		else if (result == HouseholdCreationResult.CREATED) respond(xchg, 201, gson.toJson(hcw, HouseholdCreationWrapper.class));
		else respond(xchg, 404);
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
