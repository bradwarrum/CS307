package routes;

import java.io.IOException;

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
		if ((userID = Server.sessionTable().authenticate(getToken(xchg))) < 0) {respond(xchg, 403); return;}
		String request = getRequest(xchg.getRequestBody());
		UPCJson upcjson = null;
		try {
			upcjson = gson.fromJson(request, UPCJson.class);
		} catch (JsonSyntaxException e) {
			respond(xchg, 400); return;
		}
		if (upcjson == null || !upcjson.valid()) {respond(xchg, 400); return;}
		Barcode barcode = new Barcode(upcjson.UPC);
		if (barcode.getFormat() == Barcode.Format.INVALID_FORMAT) {error(xchg, 400, "[1]Barcode format was invalid."); return;}
		else if (barcode.getFormat() == Barcode.Format.INVALID_CHECKSUM) {error(xchg, 400, "[2]Invalid checksum found for 12/13 digit barcode"); return;}
		else {
			
		}
	}
	
	private static class UPCJson {
		@Expose(deserialize = true)
		public String UPC;
		@Expose(deserialize = true) 
		public String description;
		
		public boolean valid() {
			if (description == null || description.length() > 40) return false;
			if (UPC == null) return false;
			return true;
		}
	}
}
