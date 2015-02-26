package routes;

import java.io.IOException;
import java.util.List;

import sql.wrappers.ListUpdateWrapper;
import sql.wrappers.ListUpdateWrapper.ListUpdateResult;

import com.google.gson.JsonSyntaxException;
import com.google.gson.annotations.Expose;
import com.sun.net.httpserver.HttpExchange;

import core.Server;

public class ListUpdateRoute extends Route {
	@Override
	public void handle(HttpExchange xchg) throws IOException {
		if (!"post".equalsIgnoreCase(xchg.getRequestMethod())) {respond(xchg, 404); return;}
		int userID = -1;
		int householdID = (int)xchg.getAttribute("householdID");
		int listID = (int)xchg.getAttribute("listID");
		if ((userID = Server.sessionTable().authenticate(getToken(xchg))) < 0) {respond(xchg, 403); return;}
		String request = getRequest(xchg.getRequestBody());
		ListUpdateJSON luj = null;
		try {
			luj = gson.fromJson(request, ListUpdateJSON.class);
		} catch (JsonSyntaxException e) {
			respond(xchg, 400); return;
		}
		if (luj == null || !luj.valid()) { respond(xchg, 400); return;}
		ListUpdateWrapper luw = new ListUpdateWrapper(userID, householdID, listID, luj.timestamp, luj.items);
		ListUpdateResult result = luw.update();
		if (result == ListUpdateResult.INTERNAL_ERROR) {respond(xchg, 500);}
		else if (result == ListUpdateResult.INSUFFICIENT_PERMISSIONS) {respond(xchg, 403);}
		else if (result == ListUpdateResult.OUTDATED_INFORMATION) {error(xchg, 400, "[0]Outdated timestamp.");}
		else if (result == ListUpdateResult.ITEM_NOT_FOUND) {error(xchg, 400, "[1]One or more invalid UPCs for this household. Ensure they are added to the inventory");}
		else if (result == ListUpdateResult.OK) {xchg.getResponseHeaders().set("ETag", "\"" + luw.getTimestamp() + "\"");respond(xchg, 200, gson.toJson(luw, ListUpdateWrapper.class));}
		
	}
	public static class ListUpdateJSON {
		@Expose(deserialize = true)
		public long timestamp;
		@Expose(deserialize = true)
		public List<ListUpdateItemJSON> items;
		
		public boolean valid() {
			if (timestamp < 0) return false;
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
