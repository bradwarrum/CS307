package core.json;

import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;

public class PackagingJSON {
	@Expose(serialize = true)
	@SerializedName("packageSize")
	private float unitQuantity;
	@Expose(serialize = true)
	private int unitID;
	@Expose(serialize = true)
	private String unitName;
	@Expose(serialize = true)
	private String unitAbbreviation;
	@Expose(serialize = true)
	private String packageName;
	
	public PackagingJSON (float unitQuantity, int unitID, String unitName, String unitAbbreviation, String packageName) {
		this.unitQuantity = unitQuantity;
		this.unitID = unitID;
		this.unitName = unitName;
		this.unitAbbreviation = unitAbbreviation;
		this.packageName = packageName;
	}
}
