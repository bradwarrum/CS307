package routes;

import java.io.IOException;
import java.util.Arrays;
import java.util.LinkedList;

import com.sun.net.httpserver.HttpExchange;

public class GeneralRoute extends Route {	
	private static final Route USER_REGISTRATION_ROUTE = new UserRegistrationRoute();
	private static final Route USER_LOGIN_ROUTE = new UserLoginRoute();
	private static final Route USER_ME_ROUTE = new UserMeRoute();

	private static final Route HOUSEHOLD_CREATE_ROUTE = new HouseholdCreateRoute();
	private static final Route HOUSEHOLD_FETCH_ROUTE = new HouseholdFetchRoute();
	
	private static final Route LIST_CREATE_ROUTE = new ListCreateRoute();
	private static final Route LIST_UPDATE_ROUTE = new ListUpdateRoute();
	private static final Route LIST_FETCH_ROUTE = new ListFetchRoute();
	private static final Route LIST_REMOVE_ROUTE = new ListDeleteRoute();
	
	private static final Route ITEM_SUGGESTION_ROUTE = new ItemSuggestionRoute();
	private static final Route INVENTORY_LINK_ROUTE = new UPCLinkRoute();
	private static final Route INVENTORY_UPDATE_ROUTE = new InventoryUpdateRoute();
	private static final Route INVENTORY_FETCH_ROUTE = new InventoryFetchRoute();
	private static final Route INVENTORY_DELETE_ROUTE = new InventoryDeleteRoute();
	
	private static final Route RECIPE_CREATE_ROUTE = new RecipeCreateRoute();
	private static final Route RECIPE_UPDATE_ROUTE = new RecipeUpdateRoute();
	private static final Route RECIPE_FETCH_ROUTE = new RecipeFetchRoute();
	private static final Route RECIPE_DELETE_ROUTE = new RecipeDeleteRoute();
	
	private static final Route HOUSEHOLD_INVITE_CREATE_ROUTE = new HouseholdInviteCreateRoute();
	private static final Route HOUSEHOLD_INVITE_CHECK_ROUTE = new HouseholdInviteCheckRoute();
	
	@Override
	public void handle(HttpExchange xchg) throws IOException {
		String path = xchg.getRequestURI().getPath().substring(1);
		LinkedList<String> params = new LinkedList<String>(Arrays.asList(path.split("/")));
		
		String op = params.poll();
		if (op == null) {
			respond(xchg, 404); 
			return;
		} else if (op.equals("users")) {
			op = params.poll();
			if (op == null) {
				respond(xchg, 404);
				return;
			} else if (op.equals("register") && params.isEmpty()) {
				USER_REGISTRATION_ROUTE.handle(xchg); return;
			} else if (op.equals("login") && params.isEmpty()) {
				USER_LOGIN_ROUTE.handle(xchg); return;
			} else if (op.equals("me") && params.isEmpty()) {
				USER_ME_ROUTE.handle(xchg); return;
			}	
		} else if (op.equals("households")) {
			op = params.poll();
			if (op == null) {
				respond(xchg, 404); return;
			} else if (op.equals("create") && params.isEmpty()) {
				HOUSEHOLD_CREATE_ROUTE.handle(xchg); return;
			} else {
				int householdID = Integer.parseUnsignedInt(op);
				xchg.setAttribute("householdID", householdID);
				
				op = params.poll();
				if (op == null) {
					HOUSEHOLD_FETCH_ROUTE.handle(xchg); return;
				} else if (op.equals("items")) {
					handleInventory(xchg, params);
				} else if (op.equals("lists")) {
					handleLists(xchg, params);
				} else if (op.equals("recipes")) {
					handleRecipes(xchg, params);
				} else if (op.equalst("invite")) {
					handleInvite(xchg, params);
				}
			}
		}
		try {
		} catch (NumberFormatException e) {
			respond(xchg, 404);
		}
		respond(xchg, 404);
	}
	
	private void handleInventory(HttpExchange xchg, LinkedList<String> params) throws IOException {
		String op = params.poll();
		if (op == null) {
			INVENTORY_FETCH_ROUTE.handle(xchg); return;
		} else if (op.equals("update") && params.isEmpty()) {
			INVENTORY_UPDATE_ROUTE.handle(xchg); return;
		} else if (op.equals("generate") && params.isEmpty()) {
			xchg.setAttribute("UPC", null);
			INVENTORY_LINK_ROUTE.handle(xchg); return;
		} else {
			xchg.setAttribute("UPC", op);
			op = params.poll();
			if (op == null) {
				respond(xchg, 404); return;
			} else if (op.equals("link") && params.isEmpty()) {
				INVENTORY_LINK_ROUTE.handle(xchg); return;
			} else if (op.equals("unlink") && params.isEmpty()) {
				INVENTORY_DELETE_ROUTE.handle(xchg); return;
			} else if (op.equals("suggestions") && params.isEmpty()) {
				ITEM_SUGGESTION_ROUTE.handle(xchg); return;
			}
		}
	}
	
	private void handleLists(HttpExchange xchg, LinkedList<String> params) throws IOException {
		String op = params.poll();
		if (op == null) {
			respond(xchg, 404); return;
		} else if (op.equals("create") && params.isEmpty()) {
			LIST_CREATE_ROUTE.handle(xchg); return;
		} else {
			int listID = Integer.parseUnsignedInt(op);
			xchg.setAttribute("listID", listID);
			op = params.poll();
			if (op == null) {
				LIST_FETCH_ROUTE.handle(xchg); return;
			} else if (op.equals("update") && params.isEmpty()) {
				LIST_UPDATE_ROUTE.handle(xchg); return;
			} else if (op.equals("remove") && params.isEmpty()) {
				LIST_REMOVE_ROUTE.handle(xchg); return;
			}
		}
	}
	
	private void handleRecipes(HttpExchange xchg, LinkedList<String> params) throws IOException {
		String op = params.poll();
		if (op == null) {
			respond(xchg, 404); return;
		} else if (op.equals("create") && params.isEmpty()) {
			RECIPE_CREATE_ROUTE.handle(xchg); return;
		} else {
			int recipeID = Integer.parseUnsignedInt(op);
			xchg.setAttribute("recipeID", recipeID);
			op = params.poll();
			if (op == null) {
				RECIPE_FETCH_ROUTE.handle(xchg); return;
			} else if (op.equals("update") && params.isEmpty()) {
				RECIPE_UPDATE_ROUTE.handle(xchg); return;
			} else if (op.equals("remove") && params.isEmpty()) {
				RECIPE_DELETE_ROUTE.handle(xchg); return;
			}
		}
	}
	private void handleInvite(HttpEx)HttpExchange xchg, LinkedList<String> params) throws IOException {
		String op = params.poll();
		if(op == null){
			respond(xchg,404);
			return;
		}else{
			int userId = Integer.parseUnsignedInt(op);
			xchg.setAttribute("userID", userID);
			op = params.poll();
			if(op.equals("create")){
				HOUSEHOLD_INVITE_CREATE_ROUTE.HANDLE(xchg);
				return;
			}else{
				HOUSEHOLD_INVITE_CHECK_ROUTE.handle(xchg);
				return;
			}
		}
	}

}
