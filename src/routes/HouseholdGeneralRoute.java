package routes;

import java.io.IOException;

import com.sun.net.httpserver.HttpExchange;

public class HouseholdGeneralRoute extends Route {

	private static final int HOUSEHOLD_OFS = 12;
	
	private static final Route LINK_ROUTE = new UPCLinkRoute();
	private static final Route LIST_CREATE_ROUTE = new ListCreateRoute();
	private static final Route LIST_UPDATE_ROUTE = new ListUpdateRoute();
	private static final Route LIST_FETCH_ROUTE = new ListFetchRoute();
	@Override
	public void handle(HttpExchange xchg) throws IOException {
		String path = xchg.getRequestURI().getPath();
		try {
			String hidStr = path.substring(HOUSEHOLD_OFS, path.indexOf('/', HOUSEHOLD_OFS));
			int householdID = Integer.parseUnsignedInt(hidStr);
			xchg.setAttribute("householdID", householdID);
			String remainder = path.substring(HOUSEHOLD_OFS + hidStr.length());
			String separated = remainder.split("\\?")[0];
			
			if (separated.startsWith("/items/")) {
				String UPC = separated.substring(7, separated.indexOf('/', 7));
				xchg.setAttribute("UPC", UPC);
				String itemCommand = separated.substring(7+ UPC.length());
				if (itemCommand.equals("/link")) {LINK_ROUTE.handle(xchg); return;}
			} else if (separated.equals("/lists/create")) {LIST_CREATE_ROUTE.handle(xchg); return;}
			else if (separated.startsWith("/lists/")) {
				String liststr = separated.substring(7, Math.max(separated.length(), separated.indexOf('/', 7)));
				int listID = Integer.parseUnsignedInt(liststr);
				xchg.setAttribute("listID", listID);
				String listCommand = separated.substring(7 + liststr.length());
				if (listCommand.equals("/update")) {LIST_UPDATE_ROUTE.handle(xchg); return;}
				else if (listCommand.equals("")) {LIST_FETCH_ROUTE.handle(xchg);return;}
			}
				
		} catch (IndexOutOfBoundsException e) {
			respond(xchg, 404);
		} catch (NumberFormatException e) {
			respond(xchg, 404);
		}
		respond(xchg, 404);
	}
}
