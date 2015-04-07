package sql;

public class SQLParam {
	private final SQLType _type;
	private final Object _value;
	/**
	 * Generates an SQL Parameter with the specified value and type AUTO. 
	 * @param value
	 * The value of the SQL Parameter
	 */
	public SQLParam (Object value) {
		_value = value;
		_type = SQLType.AUTO;
	}
	/**
	 * Generates an SQL Parameter with the specified value and type.
	 * <p>
	 * This is the preferred method of generating SQL parameters.
	 * Only use {@link SQLParam#SQLParam(Object) the auto constructor} if absolutely necessary.
	 * @param value
	 * The object to use as the parameter value
	 * @param type
	 * The enumerated {@link SQLType type} of the parameter
	 */
	public SQLParam (Object value, SQLType type) {
		_value = value;
		_type = type;
	}
	/**
	 * Generates an SQL Parameter with value of SQL NULL.
	 * @param type
	 * Required for casting SQL NULL to a known type. Type must not be AUTO.
	 * @throws Exception 
	 * if type is AUTO.
	 */
	public SQLParam (SQLType type) throws Exception {
		_value = null;
		if (type == SQLType.AUTO) throw new Exception ("Cannot create null SQL parameter with type AUTO.");
		_type = type;
	}
	
	public SQLType getType() {
		return _type;
	}
	
	public Object getValue() {
		return _value;
	}
	
	public static final SQLParam SQLTRUE = new SQLParam(true, SQLType.BOOLEAN);
	public static final SQLParam SQLFALSE = new SQLParam(false, SQLType.BOOLEAN);
}
