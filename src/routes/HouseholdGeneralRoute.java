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
			String remainder = path.substring(HOUSEHOLD_OFS + hidStr.length() + 1);
			String separated = remainder.split("?")[0];
			if (separated == "/link") LINK_ROUTE.handle(xchg);
			else respond(xchg,404);
		} catch (IndexOutOfBoundsException e) {
			respond(xchg, 404);
		} catch (NumberFormatException e) {
			respond(xchg, 404);
		}
	}
}
