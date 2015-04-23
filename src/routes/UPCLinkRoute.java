package routes;

import java.io.IOException;

import sql.wrappers.UPCLinkWrapper;

import com.google.gson.JsonSyntaxException;
import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;
import com.sun.net.httpserver.HttpExchange;

import core.Barcode;
import core.MeasurementUnits;
import core.ResponseCode;
import core.Server;

public class UPCLinkRoute extends Route {
	@Override
	public void handle(HttpExchange xchg) throws IOException {
		if (!"post".equalsIgnoreCase(xchg.getRequestMethod())) {
			respond(xchg, 404);
			return;
		}
		
		int userID = Server.sessionTable().authenticate(getToken(xchg));
		if (userID == -2) {error(xchg, ResponseCode.TOKEN_EXPIRED); return;}
		else if (userID == -1) {error(xchg, ResponseCode.INVALID_TOKEN); return;}
		
		int householdID = (int) xchg.getAttribute("householdID");
		String UPC = (String) xchg.getAttribute("UPC");
		String request = getRequest(xchg.getRequestBody());
		UPCJson upcjson = null;
		try {
			upcjson = gson.fromJson(request, UPCJson.class);
		} catch (JsonSyntaxException e) {
			error(xchg, ResponseCode.INVALID_PAYLOAD); return;
		}
		if (upcjson == null || !upcjson.valid() || householdID < 0 ) {
			error(xchg, ResponseCode.INVALID_PAYLOAD);
			return;
		}
		UPCLinkWrapper upclw;
		if (UPC == null) {
			upclw = new UPCLinkWrapper(userID, householdID, upcjson.description, upcjson.unitName, upcjson.size, upcjson.packageName, upcjson.version);
		}else {
			Barcode barcode = new Barcode(UPC);
			if (barcode.getFormat() == Barcode.Format.INVALID_FORMAT) {
				error(xchg, ResponseCode.UPC_FORMAT_NOT_SUPPORTED); 
				return;
			}
			else if (barcode.getFormat() == Barcode.Format.INVALID_CHECKSUM) {
				error(xchg, ResponseCode.UPC_CHECKSUM_INVALID);
				return;
			}
			upclw = new UPCLinkWrapper(userID, householdID, barcode, upcjson.description, upcjson.unitName, upcjson.size, upcjson.packageName, upcjson.version);
		}
		ResponseCode result = upclw.link();
		if (!result.success())
			error(xchg, result);
		else
			respond(xchg, result.getHttpCode(), gson.toJson(upclw, UPCLinkWrapper.class));
	}

	public static class UPCJson {
		@Expose(deserialize = true) 
		public String description;
		
		@Expose(deserialize = true)
		@SerializedName("packageUnits")
		public int unitName;
		
		@Expose(deserialize = true)
		@SerializedName("packageSize")
		public float size;
		
		@Expose(deserialize = true)
		public String packageName;
		
		@Expose(deserialize = true)
		public long version;
		
		public boolean valid() {
			if (description == null || description.length() > 40) return false;
			if (unitName < 0 || unitName > MeasurementUnits.NUM_UNITS) return false;
			if (packageName == null || packageName.length() > 20) return false;
			if (size < 0) return false;
			if (version < 0) return false;
			return true;
		}
	}
}
