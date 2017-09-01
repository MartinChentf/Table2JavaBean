package com.martin;

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
    private String[] colnames;

    /**
     * 列名类型数组
     */
    private String[] colTypes;


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
            colnames = new String[len+1];
            //字段类型 --->已经转化为java中的类名称了
            colTypes = new String[len+1];
            for(int i= 1;i<=len;i++){
                colnames[i] = metadata.getColumnName(i); //获取字段名称
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
            return SqlType.valueOf(sqlType.toUpperCase()).getType();
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
        if (null == colnames && null == colTypes) {
            return null;
        }

        //输出的类字符串
        StringBuffer str = new StringBuffer("");

        //拼接
        String className = toFirstUpper(convertUnderline2camel(tableName)) + "Do";
        str.append("@Table(\"").append(tableName).append("\")\n");
        str.append("public class ").append(className).append(" {\n\n");
        //拼接属性
        for(int index=1; index < colnames.length ; index++){
            str.append(getAttrbuteString(colnames[index],colTypes[index]));
        }
        //拼接get，Set方法
        for(int index=1; index < colnames.length ; index++){
            str.append(getGetMethodString(colnames[index],colTypes[index]));
            str.append(getSetMethodString(colnames[index],colTypes[index]));
        }
        str.append("}\n");
        //输出到文件中
        File file = new File(className + ".java");
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
    /*
     * 获取字段字符串*/
    private StringBuffer getAttrbuteString(String name, String type) {
        if(!check(name,type)) {
            System.out.println("类中有属性或者类型为空");
            return null;
        }

        String field = convertUnderline2camel(name);

        StringBuffer result = new StringBuffer();
        result.append("    /**\n")
              .append("     * ").append(field).append("\n")
              .append("     */\n")
              .append("    @Column(\"").append(name).append("\")\n")
              .append("    private ").append(type).append(" ").append(field).append(";\n\n");
        return result;
    }

    /*
     * 校验name和type是否合法*/
    private boolean check(String name, String type) {
        if("".equals(name) || name == null || name.trim().length() ==0){
            return false;
        }
        if("".equals(type) || type == null || type.trim().length() ==0){
            return false;
        }
        return true;

    }
    /*
     * 获取get方法字符串*/
    private StringBuffer getGetMethodString(String name, String type) {
        if(!check(name,type)) {
            System.out.println("类中有属性或者类型为空");
            return null;
        }

        String field = convertUnderline2camel(name);
        String methodname = "get" + toFirstUpper(field);
        String format = String.format("    public %s %s() {\n", type, methodname);
        format += String.format("        return this.%s;\n", field);
        format += "    }\n\n";
        return new StringBuffer(format);
    }

    //将名称首字符大写
    private String toFirstUpper(String name) {
        name = name.trim();
        if(name.length() > 1){
            name = name.substring(0, 1).toUpperCase()+name.substring(1);
        }else
        {
            name = name.toUpperCase();
        }
        return name;
    }

    /**
     * 下划线格式转换成驼峰命名法
     * @param name <br>
     * @return <br>
     */
    private String convertUnderline2camel(String name) {
        name = name.trim().toLowerCase();

        StringBuilder camel = new StringBuilder();
        boolean isUpper = false;
        for (char ch : name.toCharArray()) {
            if ('_' == ch) {
                isUpper = true;
                continue;
            }

            if (isUpper) {
                String upper = "" + ch;
                camel.append(upper.toUpperCase());
                isUpper = false;
            }
            else {
                camel.append(ch);
            }
        }

        return camel.toString();
    }

    /*
     * 获取字段的get方法字符串*/
    private Object getSetMethodString(String name, String type) {
        if(!check(name,type)) {
            System.out.println("类中有属性或者类型为空");
            return null;
        }

        String field = convertUnderline2camel(name);
        String methodname = "set" + toFirstUpper(field);
        String format = String.format("    public void %s(%s %s) {\n", methodname, type, field);
        format += String.format("        this.%s = %s;\n", field, field);
        format += "    }\n\n";
        return new StringBuffer(format);
    }
}