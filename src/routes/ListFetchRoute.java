package routes;

import java.io.IOException;

import sql.wrappers.ListFetchWrapper;
import sql.wrappers.ListFetchWrapper.ListFetchResults;

import com.sun.net.httpserver.HttpExchange;

import core.Server;

public class ListFetchRoute extends Route {
	@Override
	public void handle(HttpExchange xchg) throws IOException {
		if (!"get".equalsIgnoreCase(xchg.getRequestMethod())) {respond(xchg, 404); return;}
		int userID = -1;
		int householdID = (int)xchg.getAttribute("householdID");
		int listID = (int)xchg.getAttribute("listID");
		if ((userID = Server.sessionTable().authenticate(getToken(xchg))) < 0) {respond(xchg, 403); return;}
		if (householdID < 0 || listID < 0) {respond(xchg,400); return;}
		long etag = 0;
		String etagstr = xchg.getRequestHeaders().getFirst("If-None-Match");
		if (etagstr != null) {
			try {
				etag = Long.parseLong(etagstr.replaceAll("\"", ""));
			}catch (NumberFormatException e) {
				etag = 0;
			}
		}
		ListFetchWrapper lfw = new ListFetchWrapper(userID, householdID, listID, etag);
		ListFetchResults results = lfw.fetch();
		if (results == ListFetchResults.INTERNAL_ERROR) respond(xchg, 500);
		else if (results == ListFetchResults.NOT_MODIFIED) {
			xchg.getResponseHeaders().set("ETag", "\"" + lfw.getTimestamp() + "\"");
			respond(xchg, 304);
		}else if (results == ListFetchResults.INSUFFICIENT_PERMISSIONS) respond(xchg, 403);
		else if (results == ListFetchResults.OK) {
			xchg.getResponseHeaders().set("ETag", "\"" + lfw.getTimestamp() + "\"");
			respond(xchg, 200, gson.toJson(lfw, ListFetchWrapper.class));
		}
	}
	
}
