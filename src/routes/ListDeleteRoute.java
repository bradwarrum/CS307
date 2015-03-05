package routes;

import java.io.IOException;

import sql.wrappers.ListDeleteWrapper;
import sql.wrappers.ListDeleteWrapper.ListDeleteResult;

import com.sun.net.httpserver.HttpExchange;

import core.Server;

public class ListDeleteRoute extends Route {
	@Override
	public void handle(HttpExchange xchg) throws IOException {
		if (!"post".equalsIgnoreCase(xchg.getRequestMethod())) {respond(xchg, 404); return;}
		int userID = -1;
		int householdID = (int)xchg.getAttribute("householdID");
		int listID = (int)xchg.getAttribute("listID");
		if ((userID = Server.sessionTable().authenticate(getToken(xchg))) < 0) {respond(xchg, 403); return;}
		ListDeleteWrapper ldw = new ListDeleteWrapper(userID, listID, householdID);
		ListDeleteResult result = ldw.delete();
		if (result == ListDeleteResult.INTERNAL_ERROR) {respond(xchg, 500); return;}
		else if (result == ListDeleteResult.INSUFFICIENT_PERMISSIONS) {error(xchg, 403, "[1]Insufficient permissions to delete that list. Check that household ID is valid and user has modify permissions.");}
		else if (result == ListDeleteResult.LIST_NOT_FOUND) {error(xchg, 404, "[0]List not found for that household."); return;}
		else if (result == ListDeleteResult.OK) {respond(xchg, 200); return;}
		
	}
}
