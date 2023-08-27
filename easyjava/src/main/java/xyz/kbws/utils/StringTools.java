package xyz.kbws.utils;

public class StringTools {
    public static String upperCaseFirstLetter(String field) {
        if (org.apache.commons.lang3.StringUtils.isEmpty(field)) {
            return field;
        }
        //如果第二个字母是大写，第一个字母不大写
        if (field.length() > 1 && Character.isUpperCase(field.charAt(1))) {
            return field;
        }
        return field.substring(0, 1).toUpperCase() + field.substring(1);
    }

    public static String lowerCaseFirstLetter(String field) {
        if (org.apache.commons.lang3.StringUtils.isEmpty(field)) {
            return field;
        }
        return field.substring(0, 1).toLowerCase() + field.substring(1);
    }

    public static void main(String[] args) {
        String tes = "user_id";
        System.out.println(upperCaseFirstLetter(tes));
    }
}
