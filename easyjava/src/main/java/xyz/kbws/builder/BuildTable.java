package xyz.kbws.builder;

import org.apache.commons.lang3.ArrayUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import xyz.kbws.bean.Constants;
import xyz.kbws.bean.FieldInfo;
import xyz.kbws.bean.TableInfo;
import xyz.kbws.utils.JsonUtils;
import xyz.kbws.utils.PropertiesUtils;
import xyz.kbws.utils.StringUtils;

import java.sql.*;
import java.util.*;

/**
 * @author hsy
 * @date 2023/6/25
 */
public class BuildTable {

    private static final Logger logger = LoggerFactory.getLogger(BuildTable.class);
    private static Connection conn = null;

    private static  String SQL_SHOW_TABLE_STATUS = "show table status";
    private static String SQL_SHOW_TABLE_FIELDS = "show full fields from %s";
    private static String SQL_SHOW_TABLE_INDEX = "show index from %s";
    static {
        String driverName = PropertiesUtils.getString("db.driver.name");
        String url = PropertiesUtils.getString("db.url");
        String user = PropertiesUtils.getString("db.username");
        String password = PropertiesUtils.getString("db.password");
        try {
            Class.forName(driverName);
            conn = DriverManager.getConnection(url, user, password);
        }catch (Exception e){
            logger.error("数据库连接失败", e);
        }
    }

    public static List<TableInfo> getTables(){
        PreparedStatement ps = null;
        ResultSet tableResult = null;

        List<TableInfo> tableInfoList = new ArrayList<TableInfo>();
        try {
            ps = conn.prepareStatement(SQL_SHOW_TABLE_STATUS);
            tableResult = ps.executeQuery();
            while (tableResult.next()){
                String tableName = tableResult.getString("name");
                String comment = tableResult.getString("comment");
                //logger.info("tableName:{},comment:{}",tableName,comment);
                String beanName = tableName;
                if (Constants.IGNORE_TABLE_PREFIX){
                    beanName = tableName.substring(beanName.indexOf("_")+1);
                }
                beanName = processField(beanName, true);
                TableInfo tableInfo = new TableInfo();
                tableInfo.setTableName(tableName);
                tableInfo.setBeanName(beanName);
                tableInfo.setComment(comment);
                tableInfo.setBeanParamName(beanName+Constants.SUFFIX_BEAN_QUERY);

                readFieldInfo(tableInfo);
                getKeyIndexInfo(tableInfo);
                //logger.info("tableInfo:{}", JsonUtils.covertObj2Json(tableInfo));
                //logger.info("tableInfo:{}", tableInfo.toString());
                tableInfoList.add(tableInfo);
            }
        }catch (Exception e){
            logger.error("读取表失败");
        }finally {
            try {
                if (tableResult != null){
                    tableResult.close();
                }
                if (ps != null){
                    ps.close();
                }
                if (conn != null){
                    conn.close();
                }
            }catch (SQLException e){

            }
        }
        return tableInfoList;
    }

    private static void readFieldInfo(TableInfo tableInfo){
        PreparedStatement ps = null;
        ResultSet fieldResult = null;

        List<FieldInfo> fieldInfoList = new ArrayList<FieldInfo>();
        List<FieldInfo> fieldExtendList = new ArrayList<FieldInfo>();
        try {
            ps = conn.prepareStatement(String.format(SQL_SHOW_TABLE_FIELDS, tableInfo.getTableName()));
            fieldResult = ps.executeQuery();

            Boolean haveDateTime = false;
            Boolean haveDate = false;
            Boolean haveBigDecimal = false;
            while (fieldResult.next()){
                String field = fieldResult.getString("field");
                String type = fieldResult.getString("type");
                String extra = fieldResult.getString("extra");
                String comment = fieldResult.getString("comment");
                if (type.indexOf("(")>0){
                    type = type.substring(0, type.indexOf("("));
                }
                String propertyName = processField(field, false);
                FieldInfo fieldInfo = new FieldInfo();
                fieldInfoList.add(fieldInfo);

                fieldInfo.setFieldName(field);
                fieldInfo.setComment(comment);
                fieldInfo.setSqlType(type);
                fieldInfo.setAutoIncrement("auto_increment".equalsIgnoreCase(extra));
                fieldInfo.setPropertyName(propertyName);
                fieldInfo.setJavaType(processJavaType(type));

                if (ArrayUtils.contains(Constants.SQL_DATE_TIME_TYPES, type)){
                    haveDateTime = true;
                }
                if (ArrayUtils.contains(Constants.SQL_DATE_TYPES, type)){
                    haveDate = true;
                }
                if (ArrayUtils.contains(Constants.SQL_DECIMAL_TYPES, type)){
                    haveBigDecimal = true;
                }

                if (ArrayUtils.contains(Constants.SQL_STRING_TYPES, type)){
                    FieldInfo fuzzyField = new FieldInfo();
                    fuzzyField.setJavaType(fieldInfo.getJavaType());
                    fuzzyField.setPropertyName(propertyName + Constants.SUFFIX_BEAN_QUERY_FUZZY);
                    fuzzyField.setFieldName(fieldInfo.getFieldName());
                    fuzzyField.setSqlType(type);
                    fieldExtendList.add(fuzzyField);
                }

                if (ArrayUtils.contains(Constants.SQL_DATE_TIME_TYPES, type) ||
                        ArrayUtils.contains(Constants.SQL_DATE_TYPES, type)){
                    FieldInfo timeStartField = new FieldInfo();
                    timeStartField.setJavaType("String");
                    timeStartField.setPropertyName(propertyName + Constants.SUFFIX_BEAN_QUERY_FUZZY_TIME_START);
                    timeStartField.setFieldName(fieldInfo.getFieldName());
                    timeStartField.setSqlType(type);
                    fieldExtendList.add(timeStartField);

                    FieldInfo timeEndField = new FieldInfo();
                    timeEndField.setJavaType("String");
                    timeEndField.setPropertyName(propertyName + Constants.SUFFIX_BEAN_QUERY_FUZZY_TIME_END);
                    timeEndField.setFieldName(fieldInfo.getFieldName());
                    timeEndField.setSqlType(type);
                    fieldExtendList.add(timeEndField);
                }

            }
            tableInfo.setFieldExtendList(fieldExtendList);
            tableInfo.setHaveDateTime(haveDateTime);
            tableInfo.setHaveDate(haveDate);
            tableInfo.setHaveBigDecimal(haveBigDecimal);
            tableInfo.setFieldList(fieldInfoList);
        }catch (Exception e){
            logger.error("读取表失败");
        }finally {
            try {
                if (fieldResult != null){
                    fieldResult.close();
                }
                if (ps != null){
                    ps.close();
                }
            }catch (SQLException e){
                e.printStackTrace();
            }
        }
    }

    private static List<FieldInfo> getKeyIndexInfo(TableInfo tableInfo){
        PreparedStatement ps = null;
        ResultSet fieldResult = null;

        List<FieldInfo> fieldInfoList = new ArrayList<FieldInfo>();

        try {
            Map<String, FieldInfo> tempMap = new HashMap<String, FieldInfo>();
            for (FieldInfo fieldInfo : tableInfo.getFieldList()){
                tempMap.put(fieldInfo.getFieldName(), fieldInfo);
            }
            ps = conn.prepareStatement(String.format(SQL_SHOW_TABLE_INDEX, tableInfo.getTableName()));
            fieldResult = ps.executeQuery();
            while (fieldResult.next()){
                String keyName = fieldResult.getString("key_name");
                int nonUnique = fieldResult.getInt("non_unique");
                String columnName = fieldResult.getString("column_name");
                if (nonUnique == 1){
                    continue;
                }
                List<FieldInfo> keyFieldList = tableInfo.getKeyIndexMap().get(keyName);
                if (keyFieldList == null){
                    keyFieldList = new ArrayList<FieldInfo>();
                    tableInfo.getKeyIndexMap().put(keyName, keyFieldList);
                }
                keyFieldList.add(tempMap.get(columnName));

            }
        }catch (Exception e){
            logger.error("读取索引失败");
        }finally {
            try {
                if (fieldResult != null){
                    fieldResult.close();
                }
                if (ps != null){
                    ps.close();
                }
            }catch (SQLException e){
                e.printStackTrace();
            }
        }
        return fieldInfoList;
    }

    private static String processField(String field,Boolean upCaseFirstLetter){
        StringBuffer sb = new StringBuffer();
        String[] fields = field.split("_");
        sb.append(upCaseFirstLetter ? StringUtils.upCaseFirstLetter(fields[0]) : fields[0]);
        for (int i = 1, len= fields.length; i < len; i++) {
            sb.append(StringUtils.upCaseFirstLetter(fields[i]));
        }
        return sb.toString();
    }

    private static String processJavaType(String type){
        if (ArrayUtils.contains(Constants.SQL_INTEGER_TYPES, type)){
            return "Integer";
        } else if (ArrayUtils.contains(Constants.SQL_LONG_TYPES, type)) {
            return "Long";
        } else if (ArrayUtils.contains(Constants.SQL_STRING_TYPES, type)) {
            return "String";
        } else if (ArrayUtils.contains(Constants.SQL_DATE_TIME_TYPES, type) || ArrayUtils.contains(Constants.SQL_DATE_TYPES, type)) {
            return "Date";
        } else if (ArrayUtils.contains(Constants.SQL_DECIMAL_TYPES, type)) {
            return "BigDecimal";
        }else {
            throw new RuntimeException("无法识别的类型:" + type);
        }
    }
}
