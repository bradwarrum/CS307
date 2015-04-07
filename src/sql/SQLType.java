package sql;

public enum SQLType {
	BYTE(java.sql.Types.TINYINT),
	DATE(java.sql.Types.DATE),
	DOUBLE(java.sql.Types.DOUBLE),
	FLOAT(java.sql.Types.FLOAT),
	INT(java.sql.Types.INTEGER),
	LONG(java.sql.Types.BIGINT),
	SHORT(java.sql.Types.SMALLINT),
	VARCHAR(java.sql.Types.VARCHAR),
	CHAR(java.sql.Types.CHAR),
	VARBINARY(java.sql.Types.VARBINARY),
	BINARY(java.sql.Types.BINARY),
	TIME(java.sql.Types.TIME),
	TIMESTAMP(java.sql.Types.TIMESTAMP),
	BOOLEAN(java.sql.Types.BOOLEAN),
	AUTO(0);
	
	private int value;
	private SQLType(int value) {
		this.value = value;
	}
	public int getValue() {
		return value;
	}
	
}
