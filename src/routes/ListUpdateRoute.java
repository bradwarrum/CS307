package routes;

import java.io.IOException;
import java.util.List;

import sql.wrappers.ListUpdateWrapper;

import com.google.gson.JsonSyntaxException;
import com.google.gson.annotations.Expose;
import com.sun.net.httpserver.HttpExchange;

import core.ResponseCode;
import core.Server;

public class ListUpdateRoute extends Route {
	@Override
	public void handle(HttpExchange xchg) throws IOException {
		if (!"post".equalsIgnoreCase(xchg.getRequestMethod())) {respond(xchg, 404); return;}
		
		int userID = Server.sessionTable().authenticate(getToken(xchg));
		if (userID == -2) {error(xchg, ResponseCode.TOKEN_EXPIRED); return;}
		else if (userID == -1) {error(xchg, ResponseCode.INVALID_TOKEN); return;}
		
		int householdID = (int)xchg.getAttribute("householdID");
		int listID = (int)xchg.getAttribute("listID");
		String request = getRequest(xchg.getRequestBody());
		ListUpdateJSON luj = null;
		try {
			luj = gson.fromJson(request, ListUpdateJSON.class);
		} catch (JsonSyntaxException e) {
			error(xchg, ResponseCode.INVALID_PAYLOAD); return;
		}
		if (luj == null || !luj.valid()) { error(xchg, ResponseCode.INVALID_PAYLOAD); return;}
		ListUpdateWrapper luw = new ListUpdateWrapper(userID, householdID, listID, luj.version, luj.items);
		ResponseCode result = luw.update();
		if (!result.success()) 
			error(xchg, result);
		else {
			xchg.getResponseHeaders().set("ETag",
					"\"" + luw.getTimestamp() + "\"");
			respond(xchg, 200, gson.toJson(luw, ListUpdateWrapper.class));
		}
	}
	public static class ListUpdateJSON {
		@Expose(deserialize = true)
		public long version;
		@Expose(deserialize = true)
		public List<ListUpdateItemJSON> items;
		
		public boolean valid() {
			if (version < 0) return false;
			if (items == null) return false;
			for (ListUpdateItemJSON l : items) {
				if (!l.valid()) return false;
			}
			return true;
		}
	}
	public static class ListUpdateItemJSON {
		@Expose(deserialize = true)
		public String UPC;
		@Expose(deserialize = true)
		public int quantity;
		@Expose(deserialize = true)
		public int fractional = 0;
		
		public boolean valid() {
			if (UPC == null || UPC.length() > 13 ) return false;
			if (quantity < 0) return false;
			if (fractional <0 || fractional > 99) return false;
			return true;
		}
	}
}
