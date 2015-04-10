package core.json;

import com.google.gson.annotations.Expose;

public class UpdateItemJSON {
	@Expose(deserialize = true)
	public String UPC;
	@Expose(deserialize = true)
	public int quantity;
	@Expose(deserialize = true)
	public int fractional = 0;
	
	public boolean valid() {
		if (UPC == null || UPC.length() > 13 ) return false;
		if (quantity < 0) return false;
		if (fractional <0 || fractional > 99) return false;
		return true;
	}
}
