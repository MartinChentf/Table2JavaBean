package com.martin;

/**
 * <Description> <br>
 *
 * @author chen.tengfei <br>
 * @version 1.0<br>
 * @taskId <br>
 * @CreateDate 2017/8/31 <br>
 * @see com.martin <br>
 * @since R9.0<br>
 */
public enum SqlType {

    BIT("boolean"),

    TINYINT("byte"),

    SMALLINT("short"),

    INT("int"),

    BIGINT("Long"),

    NUMBER("Long"),

    FLOAT("float"),

    DECIMAL("double"),

    NUMERIC("double"),

    REAL("double"),

    MONEY("double"),

    SMALLMONEY("double"),

    VARCHAR2("String"),

    VARCHAR("String"),

    CHAR("String"),

    NVARCHAR("String"),

    NCHAR("String"),

    TEXT("String"),

    DATETIME("Date"),

    DATE("Date"),

    IMAGE("Blod");

    /**
     * 对应的Java类型
     */
    String type;

    SqlType(String type) {
        this.type = type;
    }

    public String getType() {
        return type;
    }
}
