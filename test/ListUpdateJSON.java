import static org.junit.Assert.*;

import org.junit.Test;

import routes.ListUpdateRoute;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;


public class ListUpdateJSON {

	@Test
	public void test() {
		String JSONstring = "{\"timestamp\" : 12345, \"items\" : [{"
				+ "\"UPC\" : \"123456789012\", \"quantity\" : 10"
				+ "}]}";
		Gson gson = new GsonBuilder().setPrettyPrinting().serializeNulls().excludeFieldsWithoutExposeAnnotation().create();
		routes.ListUpdateRoute.ListUpdateJSON luj = gson.fromJson(JSONstring, ListUpdateRoute.ListUpdateJSON.class);
		
		
	}

}
