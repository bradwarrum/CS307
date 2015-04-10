package core.json;

import com.google.gson.annotations.Expose;

public class FetchItemJSON {
	@Expose(serialize = true)		
	public final String UPC;
	@Expose(serialize = true)
	public final boolean isInternalUPC;
	@Expose(serialize = true)
	public final String description;
	@Expose(serialize = true)		
	public final int quantity;
	@Expose(serialize = true)
	public final int fractional;
	@Expose(serialize = true)
	public final PackagingJSON packaging;

	public FetchItemJSON (String UPC, String description, int quantity, int fractional, boolean isInternalUPC, PackagingJSON packaging) {
		this.packaging = packaging;
		this.UPC = UPC;
		this.description = description;
		this.quantity = quantity;
		this.fractional = fractional;
		this.isInternalUPC = isInternalUPC;
	}
}