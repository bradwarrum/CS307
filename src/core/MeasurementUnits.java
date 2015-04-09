package core;
public enum MeasurementUnits{
	LB(1),
	OZ(2),
	G(3),
	MG(4),
	L(5),
	ML(6),
	GAL(7),
	QT(8),
	PT(9),
	CUPS(10),
	TSP(11),
	TBSP(12),
	FL_OZ(13),
	UNITS(14);
	
	private final int id;
	private MeasurementUnits(int id) {
		this.id = id;
	}
	
	public int getID() {
		return id;
	}
	
	public static int NUM_UNITS = MeasurementUnits.values().length;

};