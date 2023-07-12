package xyz.kbws.utils;

/**
 * @author hsy
 * @date 2023/6/25
 */
public class StringUtils {
    public static String upCaseFirstLetter(String field){
        if (org.apache.commons.lang3.StringUtils.isEmpty(field)){
            return field;
        }
        return field.substring(0, 1).toUpperCase() + field.substring(1);
    }

    public static String lowCaseFirstLetter(String field){
        if (org.apache.commons.lang3.StringUtils.isEmpty(field)){
            return field;
        }
        return field.substring(0, 1).toLowerCase() + field.substring(1);
    }
}
