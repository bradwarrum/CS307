package routes;

import java.io.IOException;

import com.sun.net.httpserver.HttpExchange;

public class HouseholdGeneralRoute extends Route {

	private static final int HOUSEHOLD_OFS = 12;
	
	private static final Route LINK_ROUTE = new UPCLinkRoute();
	@Override
	public void handle(HttpExchange xchg) throws IOException {
		String path = xchg.getRequestURI().getPath();
		try {
			String hidStr = path.substring(HOUSEHOLD_OFS, path.indexOf('/', HOUSEHOLD_OFS));
			int householdID = Integer.parseInt(hidStr);
			xchg.setAttribute("householdID", householdID);
			String remainder = path.substring(HOUSEHOLD_OFS + hidStr.length());
			String separated = remainder.split("\\?")[0];
			
			if (separated.startsWith("/items/")) {
				String UPC = separated.substring(7, separated.indexOf('/', 7));
				xchg.setAttribute("UPC", UPC);
				String itemCommand = separated.substring(7+ UPC.length());
				if (itemCommand.equals("/link")) {LINK_ROUTE.handle(xchg); return;}
			}
		} catch (IndexOutOfBoundsException e) {
			respond(xchg, 404);
		} catch (NumberFormatException e) {
			respond(xchg, 404);
		}
		respond(xchg, 404);
	}
}
