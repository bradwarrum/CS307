package routes;

import java.io.IOException;

import sql.wrappers.UPCLinkWrapper;
import sql.wrappers.UPCLinkWrapper.UPCLinkResult;

import com.google.gson.JsonSyntaxException;
import com.google.gson.annotations.Expose;
import com.sun.net.httpserver.HttpExchange;

import core.Barcode;
import core.Server;

public class UPCLinkRoute extends Route {
	@Override
	public void handle(HttpExchange xchg) throws IOException {
		if (!"post".equalsIgnoreCase(xchg.getRequestMethod())) {respond(xchg, 404); return;}
		int userID = -1;
		int householdID = (int) xchg.getAttribute("householdID");
		String UPC = (String) xchg.getAttribute("UPC");
		if ((userID = Server.sessionTable().authenticate(getToken(xchg))) < 0) {respond(xchg, 403); return;}
		String request = getRequest(xchg.getRequestBody());
		UPCJson upcjson = null;
		try {
			upcjson = gson.fromJson(request, UPCJson.class);
		} catch (JsonSyntaxException e) {
			respond(xchg, 400); return;
		}
		if (upcjson == null || !upcjson.valid() || householdID < 0 || UPC == null ) {respond(xchg, 400); return;}
		Barcode barcode = new Barcode(UPC);
		if (barcode.getFormat() == Barcode.Format.INVALID_FORMAT || barcode.getFormat() == Barcode.Format.PRODUCE_5) {error(xchg, 400, "[1]Barcode format was invalid."); return;}
		else if (barcode.getFormat() == Barcode.Format.INVALID_CHECKSUM) {error(xchg, 400, "[2]Invalid checksum found for 12/13 digit barcode"); return;}
		UPCLinkWrapper upclw =  new UPCLinkWrapper(userID, householdID, barcode, upcjson.description, upcjson.unitName);
		UPCLinkResult result = upclw.link();
		if (result == UPCLinkResult.HOUSEHOLD_NOT_FOUND) error(xchg, 400, "[3]Household not found.");
		else if (result == UPCLinkResult.INSUFFICIENT_PERMISSIONS) error(xchg, 403, "[4]Insufficient permissions");
		else if (result == UPCLinkResult.INTERNAL_ERROR) respond(xchg, 500);
		else respond(xchg, 200);
	}

	private static class UPCJson {
		@Expose(deserialize = true) 
		public String description;
		@Expose(deserialize = true)
		public String unitName;
		public boolean valid() {
			if (description == null || description.length() > 40) return false;
			if (unitName == null || unitName.length() > 5) return false;
			return true;
		}
	}
}
