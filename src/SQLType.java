
public enum SQLType {
	BYTE(java.sql.Types.TINYINT),
	DATE(java.sql.Types.DATE),
	DOUBLE(java.sql.Types.DOUBLE),
	FLOAT(java.sql.Types.FLOAT),
	INT(java.sql.Types.INTEGER),
	LONG(java.sql.Types.BIGINT),
	SHORT(java.sql.Types.SMALLINT),
	STRING(java.sql.Types.VARCHAR),
	TIME(java.sql.Types.TIME),
	TIMESTAMP(java.sql.Types.TIMESTAMP),
	AUTO(0);
	
	private int value;
	private SQLType(int value) {
		this.value = value;
	}
	public int getValue() {
		return value;
	}
	
}
