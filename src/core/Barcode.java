package core;

public class Barcode {
	public static enum Format {
		INVALID_FORMAT,
		INVALID_CHECKSUM,
		PRODUCE_5,
		EAN_13,
		UPC_A,
		UPC_E_EAN_8
	}
	private String barcode = null;
	private Format format = Format.INVALID_FORMAT;
	
	public Barcode (String barcode) {
		this.barcode = barcode;
		if (barcode == null) return;
		if (barcode.length() != 12 && barcode.length() != 13 && barcode.length() != 8 && barcode.length() != 5) return;
		int [] numbers = new int[13];
		for (int i = 0; i < 13; i++) {
			numbers[i] = 0;
		}
		for (int i = 0; i< barcode.length(); i++) {
			if (!(barcode.charAt(i) >= '0' && barcode.charAt(i) <= '9')) return;
			numbers[13 - (barcode.length() - i)] = barcode.charAt(i) - '0';
		}
		if (barcode.length() == 12 || barcode.length() == 13) {
			int total = 0;
			for (int i = 0; i < 12; i++) {
				total += (i % 2 == 0) ? numbers[i] : numbers[i] * 3;
			}
			int checksum = 10 - (total % 10);
			checksum = (checksum != 10) ? checksum : 0;
			if (checksum != numbers[12]) format = Format.INVALID_CHECKSUM;
			else if (barcode.length() == 12) format = Format.UPC_A;
			else if (barcode.length() == 13) format = Format.EAN_13;
		} else if (barcode.length() == 8) format = Format.UPC_E_EAN_8;
		else if (barcode.length() == 5) format = Format.PRODUCE_5;
	}
	
	public Format getFormat() {
		return format;
	}
	@Override
	public String toString() {
		return barcode;
	}
}
