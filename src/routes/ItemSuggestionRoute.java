package routes;

import java.io.IOException;

import sql.wrappers.ItemSuggestionWrapper;

import com.sun.net.httpserver.HttpExchange;

import core.Barcode;
import core.ResponseCode;
import core.Server;

public class ItemSuggestionRoute extends Route {
	@Override
	public void handle(HttpExchange xchg) throws IOException {
		if (!"get".equalsIgnoreCase(xchg.getRequestMethod())){
			respond(xchg, 404); 
			return;
		}
		int userID = Server.sessionTable().authenticate(getToken(xchg));
		if (userID == -2) {error(xchg, ResponseCode.TOKEN_EXPIRED); return;}
		else if (userID == -1) {error(xchg, ResponseCode.INVALID_TOKEN); return;}
		int householdID = (int) xchg.getAttribute("householdID");
		String UPC=(String) xchg.getAttribute("UPC");
		Barcode b = new Barcode(UPC);
		if (b.getFormat() == Barcode.Format.INVALID_FORMAT) {error(xchg, ResponseCode.UPC_FORMAT_NOT_SUPPORTED); return;}
		else if (b.getFormat() == Barcode.Format.INVALID_CHECKSUM) {error(xchg, ResponseCode.UPC_CHECKSUM_INVALID); return;}
		ItemSuggestionWrapper idw= new ItemSuggestionWrapper(householdID, userID, UPC);
		ResponseCode results = idw.fetch();
		if (!results.success())
			error(xchg, results);
		else
			respond(xchg,results.getHttpCode(),gson.toJson(idw,ItemSuggestionWrapper.class));
		
	}
}

