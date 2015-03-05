package routes;

import java.io.IOException;

import sql.wrappers.HouseholdCreationWrapper;
import sql.wrappers.HouseholdCreationWrapper.HouseholdCreationResult;

import com.google.gson.JsonSyntaxException;
import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;
import com.sun.net.httpserver.HttpExchange;

import core.Server;

public class InventoryDescriptionRoute extends Route {
	@Override
	public void handle(HttpExchange xchg) throws IOException {
		if (!"get".equalsIgnoreCase(xchg.getRequestMethod())){
			respond(xchg, 404); 
			return;
		}
		int userID = -1;
		if ((userID = Server.sessionTable().authenticate(getToken(xchg))) < 0){
			respond(xchg, 403); 
			return;
		}
		int householdID = (int) xchg.getAttribute("householdID");
		if(householdID<0){
			respond(xchg,400);
			return;			
		}
		String UPC=(String) xchg.getAttribute("UPC");
		InventoryDescriptionWrapper idw= new InventoryDescriptionWrapper(householdID, upc);
		if(!idw.fetch()){
			respond(xchg,500);
			return;
		}
		respond(xchg,200,gson.toJson(idw,InventoryDescriptionWrapper.class));
	}
}

