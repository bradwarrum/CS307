import static org.junit.Assert.*;

import java.util.HashMap;
import java.util.Map;

import org.junit.Test;

import core.Barcode;
import core.Barcode.Format;


public class BarcodeTest {

	@Test
	public void nullString() {
		Barcode b = new Barcode(null);
		assertEquals("Null generates INVALID FORMAT", b.getFormat(), Barcode.Format.INVALID_FORMAT);
	}
	
	@Test 
	public void invalidNumChar() {
		Barcode b = new Barcode("123456");
		assertEquals("Invalid character count", b.getFormat(), Barcode.Format.INVALID_FORMAT);
	}
	@Test
	public void nonNumeric() {
		Barcode b = new Barcode("123a456789123");
		assertEquals("Non-numeric Barcode", b.getFormat(), Barcode.Format.INVALID_FORMAT);
		b = new Barcode("1234ABCD4923");
		assertEquals("Non-numeric Barcode", b.getFormat(), Barcode.Format.INVALID_FORMAT);
	}
	
	@Test
	public void invalidChecksumUPCA() {
		Barcode b = new Barcode("040000231324");
		assertEquals("Invalid UPC-A Checksum", b.getFormat(), Barcode.Format.INVALID_CHECKSUM);
		b = new Barcode("040000231325");
		assertEquals("Valid UPC-A Checksum", b.getFormat(), Barcode.Format.UPC_A);
	}
	
	@Test
	public void invalidChecksumEAN13() {
		Barcode b = new Barcode("0029000071857");
		assertEquals("Invalid EAN-13 Checksum", b.getFormat(), Barcode.Format.INVALID_CHECKSUM);
		b = new Barcode("0029000071858");
		assertEquals("Valid EAN-13 Checksum", b.getFormat(), Barcode.Format.EAN_13);
	}
	
	@Test
	public void multipleValid() {
		Map<String, Barcode.Format> tests = new HashMap<String, Barcode.Format>();
		tests.put("075967902001", Format.UPC_A);
		tests.put("04567891", Format.UPC_E_EAN_8);
		tests.put("12345", Format.PRODUCE_5);
		tests.put("0070972139862", Format.EAN_13);
		Barcode b;
		int i = 1;
		for (String s : tests.keySet()) {
			b = new Barcode(s);
			assertEquals("Multiple valid test " + i, b.getFormat(), tests.get(s));
		}
	}

}
