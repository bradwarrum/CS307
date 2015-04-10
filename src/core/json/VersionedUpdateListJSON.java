package core.json;

import java.util.List;

import com.google.gson.annotations.Expose;

public class VersionedUpdateListJSON {
	@Expose(deserialize = true)
	public long version;
	@Expose(deserialize = true)
	public List<UpdateItemJSON> items;
	
	public boolean valid() {
		if (version < 0) return false;
		if (items == null) return false;
		for (UpdateItemJSON l : items) {
			if (!l.valid()) return false;
		}
		return true;
	}
}
