package routes;

import java.io.IOException;

import com.sun.net.httpserver.HttpExchange;

public class HouseholdGeneralRoute extends Route {

	private static final int HOUSEHOLD_OFS = 12;
	
	private static final Route LINK_ROUTE = new UPCLinkRoute();
	private static final Route LIST_CREATE_ROUTE = new ListCreateRoute();
	private static final Route LIST_UPDATE_ROUTE = new ListUpdateRoute();
	private static final Route LIST_FETCH_ROUTE = new ListFetchRoute();
	private static final Route LIST_REMOVE_ROUTE = new ListDeleteRoute();
	private static final Route HOUSEHOLD_FETCH_ROUTE = new HouseholdFetchRoute();
	private static final Route ITEM_SUGGESTION_ROUTE = new ItemSuggestionRoute();
	private static final Route INVENTORY_UPDATE_ROUTE = new InventoryUpdateRoute();
	private static final Route INVENTORY_FETCH_ROUTE = new InventoryFetchRoute();
	private static final Route INVENTORY_DELETE_ROUTE = new InventoryDeleteRoute();
	@Override
	public void handle(HttpExchange xchg) throws IOException {
		String path = xchg.getRequestURI().getPath();
		try {
			int slindex = path.indexOf('/', HOUSEHOLD_OFS);
			String hidStr;
			if (slindex < 0) {
				hidStr = path.substring(HOUSEHOLD_OFS, path.length());
			}else {
				hidStr = path.substring(HOUSEHOLD_OFS, slindex);
			}
			int householdID = Integer.parseUnsignedInt(hidStr);
			xchg.setAttribute("householdID", householdID);
			String remainder = path.substring(HOUSEHOLD_OFS + hidStr.length());
			if (remainder.equals("")) {HOUSEHOLD_FETCH_ROUTE.handle(xchg); return;}
			
			String separated = remainder.split("\\?")[0];
			if (separated.equals("/items")) {
				INVENTORY_FETCH_ROUTE.handle(xchg);
				return;
			}else if (separated.equals("/items/update")) {
				INVENTORY_UPDATE_ROUTE.handle(xchg);
				return;
			}else if (separated.startsWith("/items/")) {
				String UPC = separated.substring(7, separated.indexOf('/', 7));
				xchg.setAttribute("UPC", UPC);
				String itemCommand = separated.substring(7+ UPC.length());
				if (itemCommand.equals("/link")) {
					LINK_ROUTE.handle(xchg); 
					return;
				}else if(itemCommand.equals("/suggestions")){
					ITEM_SUGGESTION_ROUTE.handle(xchg);
					return;
				}else if (itemCommand.equals("/unlink")) {
					INVENTORY_DELETE_ROUTE.handle(xchg);
					return;
				}
			} else if (separated.equals("/lists/create")) {
				LIST_CREATE_ROUTE.handle(xchg);
				return;
			}
			else if (separated.startsWith("/lists/")) {
				slindex = separated.indexOf('/', 7);
				String liststr;
				if (slindex < 0) {
					liststr = separated.substring(7, separated.length());
				}else {
					liststr = separated.substring(7, slindex);
				}
				int listID = Integer.parseUnsignedInt(liststr);
				xchg.setAttribute("listID", listID);
				String listCommand = separated.substring(7 + liststr.length());
				if (listCommand.equals("/update")) {LIST_UPDATE_ROUTE.handle(xchg); return;}
				else if (listCommand.equals("/remove")) {LIST_REMOVE_ROUTE.handle(xchg); return;}
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
