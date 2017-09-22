package com.martin;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.apache.commons.lang3.StringUtils;

/**
 * 字符串工具类
 *
 * @author henry
 * @version 9.0.2
 * @CreateDate May 24, 2017
 */
public abstract class StringUtil {

    /**
     * 下划线
     */
    private static final String UNDERSCORE = "_";

    /**
     * 驼峰正则表达式
     */
    private static final Pattern CAMEL_PATTERN = Pattern.compile("[A-Z]");

    /**
     * 下划线正则表达式
     */
    private static final Pattern UNDERSCORE_PATTERN = Pattern.compile("_(\\w)");;

    /**
     * 占位符格式化
     *
     * @param format 格式
     * @param args 参数
     * @return String
     */
    public static String format(String format, Object... args) {
        if (StringUtils.isEmpty(format)) {
            return null;
        }
        return String.format(format.replace("%", "%%").replace("{}", "%s"), args);
    }

    /**
     * 将驼峰式命名转换成下划线式命名
     *
     * @param camel 驼峰字符串
     * @return String
     */
    public static String camelToUnderscore(String camel) {
        if (StringUtils.isNotEmpty(camel) && !camel.contains(UNDERSCORE)) {
            Matcher matcher = CAMEL_PATTERN.matcher(camel);
            StringBuffer underscore = new StringBuffer();
            while (matcher.find()) {
                matcher.appendReplacement(underscore, UNDERSCORE + matcher.group(0));
            }
            matcher.appendTail(underscore);
            return underscore.toString().toUpperCase();
        }
        return camel;
    }

    /**
     * 将下划线式命名转换成驼峰式命名
     *
     * @param underscore 下划线字符串
     * @return String
     */
    public static String underscoreToCamel(String underscore) {
        if (StringUtils.isNotEmpty(underscore) && underscore.contains(UNDERSCORE)) {
            underscore = underscore.toLowerCase();

            Matcher matcher = UNDERSCORE_PATTERN.matcher(underscore);
            StringBuffer camel = new StringBuffer();
            while (matcher.find()) {
                matcher.appendReplacement(camel, matcher.group(1).toUpperCase());
            }
            matcher.appendTail(camel);
            return camel.toString();
        }
        return underscore;
    }

    /**
     * 将下划线式命名转换成驼峰式命名
     * <p>
     * 将第一个字母转换大写
     * <p>
     *
     * @author henry
     * @param underscore 下划线字符串
     * @return String
     */
    public static String underscoreToUpperCamel(String underscore) {
        if (StringUtils.isNotEmpty(underscore)) {
            String camel = underscoreToCamel(underscore);
            return StringUtils.capitalize(camel);
        }
        return underscore;
    }

    /**
     * 将下划线式命名转换成驼峰式命名
     * <p>
     * 将不含'_'的转换小写
     *
     * @author henry
     * @param underscore 下划线字符串
     * @return String
     */
    public static String underscoreToLowerCamel(String underscore) {
        if (StringUtils.isNotEmpty(underscore)) {
            String camel = underscoreToCamel(underscore);
            if (underscore.equals(camel)) {
                return camel.toLowerCase();
            }
            return camel;
        }
        return underscore;
    }

    public static String underscoreToLowerCamelTitleCase(String underscore) {
        if (StringUtils.isNotEmpty(underscore)) {
            String camel = underscoreToCamel(underscore);
            if (underscore.equals(camel)) {
                return camel.toLowerCase();
            }
            return camel;
        }
        return underscore;
    }
}
