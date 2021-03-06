package com.martin;

import com.martin.util.StringUtil;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.datasource.DataSourceUtils;
import org.springframework.stereotype.Component;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSetMetaData;
import java.sql.SQLException;
import java.text.SimpleDateFormat;
import java.util.Date;

/**
 * <Description> 将数据库中的表转换成对应的Do <br>
 *
 * @author chen.tengfei <br>
 * @version 1.0<br>
 * @CreateDate 2017/8/25 <br>
 * @see com.martin <br>
 * @since R9.0<br>
 */
@Component
public class Table2JavaBean {

    /**
     * jdbcTemplate
     */
    @Autowired
    private JdbcTemplate jdbcTemplate;

    /**
     * 列名数组
     */
    private String[] colNames;

    /**
     * 列名类型数组
     */
    private String[] colTypes;

    /**
     * 是否包含日期属性
     */
    private boolean haveDate = false;


    private void toPojo(String tableName) {

        String sql = "SELECT * FROM " + tableName;

        Connection conn = null;
        try {

            conn = DataSourceUtils.getConnection(jdbcTemplate.getDataSource());

            PreparedStatement statement = conn.prepareStatement(sql);
            //获取数据库的元数据
            ResultSetMetaData metadata = statement.getMetaData();
            //数据库的字段个数
            int len = metadata.getColumnCount();
            //字段名称
            colNames = new String[len+1];
            //字段类型 --->已经转化为java中的类名称了
            colTypes = new String[len+1];
            for(int i= 1;i<=len;i++){
                colNames[i] = metadata.getColumnName(i); //获取字段名称
                colTypes[i] = convertSqlType2JavaType(metadata.getColumnTypeName(i)); //获取字段类型
            }
        }
        catch (SQLException e) {
            e.printStackTrace();
        }
        finally {
            DataSourceUtils.releaseConnection(conn, jdbcTemplate.getDataSource());
        }
    }

    /**
     * <Description> 将mysql的字段类型转化为java的类型 <br>
     *
     * @author chen.tengfei <br>
     * @param sqlType <br>
     * @return <br>
     */
    private String convertSqlType2JavaType(String sqlType) {
        try {
            String type = SqlType.valueOf(sqlType.toUpperCase()).getType();

            if ("Date".equalsIgnoreCase(type)) {
                haveDate = true;
            }

            return type;
        }
        catch (Exception ex) {
            ex.printStackTrace();
            return null;
        }
    }

    /**
     * <Description> 获取整个类的字符串并且输出为java文件 <br>
     *
     * @author chen.tengfei <br>
     * @param tableName 表名<br>
     * @return <br>
     */
    public  StringBuffer getClassStr(String tableName){
        //获取表类型和表名的字段名
        this.toPojo(tableName);
        //校验
        if (null == colNames && null == colTypes) {
            return null;
        }

        //输出的类字符串
        StringBuffer str = new StringBuffer("");

        //拼接类头部
        str.append(getClassHeadString(tableName));
        //拼接属性
        for(int index = 1; index < colNames.length ; index++){
            str.append(getAttrbuteString(colNames[index],colTypes[index]));
        }
        //拼接get，Set方法
        for(int index = 1; index < colNames.length ; index++){
            str.append(getGetMethodString(colNames[index],colTypes[index]));
            str.append(getSetMethodString(colNames[index],colTypes[index]));
        }
        str.append("}\n");
        //输出到文件中
        File file = new File(getClassName(tableName) + ".java");
        BufferedWriter write = null;

        try {
            write = new BufferedWriter(new FileWriter(file));
            write.write(str.toString());
            write.close();
        } catch (IOException e) {

            e.printStackTrace();
            if (write != null)
                try {
                    write.close();
                } catch (IOException e1) {
                    e1.printStackTrace();
                }
        }
        return str;
    }

    /**
     * 将表名转换成类名
     * @param tableName <br>
     * @return <br>
     */
    private String getClassName(String tableName) {
        return StringUtil.underscoreToLowerCamelTitleCase(tableName) + "Do";
    }

    /**
     * <Description> 生成类头 <br>
     *
     * @author chen.tengfei <br>
     * @param tableName <br>
     * @return <br>
     */
    private StringBuilder getClassHeadString(String tableName) {
        String className = getClassName(tableName);
        SimpleDateFormat format = new SimpleDateFormat("YYYY/MM/dd");

        StringBuilder result = new StringBuilder();
        result.append(getImportPacket())
              .append("\n")
              .append("/**\n")
              .append(" * ").append(className).append(" for ").append(tableName).append(" <br>\n")
              .append(" *\n")
              .append(" * @author <br>\n")
              .append(" * @version 1.0 <br>\n")
              .append(" * @CreateDate ").append(format.format(new Date())).append(" <br>\n")
              .append(" */\n")
              .append("@Table(\"").append(tableName).append("\")\n")
              .append("public class ").append(className).append(" {\n\n");

        return result;
    }

    /**
     * 导入可能包含的Java包
     */
    private StringBuilder getImportPacket() {
        StringBuilder result = new StringBuilder();

        if (haveDate) {
            result.append("import java.util.Date;\n");
        }

        return result;
    }

    /**
     * 校验name和type是否合法
     */
    private boolean check(String name, String type) {
        return StringUtils.isNotEmpty(name.trim()) && StringUtils.isNotEmpty(type.trim());
    }

    /**
     * 获取字段字符串 <br>
     *
     * @author chen.tengfei <br>
     * @param name <br>
     * @param type <br>
     * @return <br>
     */
    private StringBuffer getAttrbuteString(String name, String type) {
        if(!check(name,type)) {
            System.out.println("类中有属性或者类型为空");
            return null;
        }

        String field = StringUtil.underscoreToLowerCamel(name);

        StringBuffer result = new StringBuffer();
        result.append("    /**\n")
              .append("     * ").append(field).append("\n")
              .append("     */\n")
              .append("    @Column(\"").append(name).append("\")\n")
              .append("    private ").append(type).append(" ").append(field).append(";\n\n");
        return result;
    }

    /**
     * 获取get方法字符串
     */
    private StringBuffer getGetMethodString(String name, String type) {
        if(!check(name,type)) {
            System.out.println("类中有属性或者类型为空");
            return null;
        }

        String field = StringUtil.underscoreToLowerCamel(name);
        String methodname = "get" + StringUtils.capitalize(field);
        String format = String.format("    public %s %s() {\n", type, methodname);
        format += String.format("        return this.%s;\n", field);
        format += "    }\n\n";
        return new StringBuffer(format);
    }

    /**
     * 获取set方法字符串
     */
    private Object getSetMethodString(String name, String type) {
        if(!check(name,type)) {
            System.out.println("类中有属性或者类型为空");
            return null;
        }

        String field = StringUtil.underscoreToLowerCamel(name);
        String methodName = "set" + StringUtils.capitalize(field);
        String format = String.format("    public void %s(%s %s) {\n", methodName, type, field);
        format += String.format("        this.%s = %s;\n", field, field);
        format += "    }\n\n";
        return new StringBuffer(format);
    }
}