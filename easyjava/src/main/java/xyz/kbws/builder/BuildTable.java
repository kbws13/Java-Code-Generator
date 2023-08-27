package xyz.kbws.builder;

import org.apache.commons.lang3.ArrayUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import xyz.kbws.bean.Constants;
import xyz.kbws.bean.FieldInfo;
import xyz.kbws.bean.TableInfo;
import xyz.kbws.utils.PropertiesUtils;
import xyz.kbws.utils.StringTools;

import java.sql.*;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class BuildTable {
    private static Connection conn = null;

    private static final Logger logger = LoggerFactory.getLogger(BuildTable.class);

    private static final String SQL_SHOW_TABLE_STATUS = "show table status";

    private static final String SQL_PREFIX_SHOW_FIELDS = "show full fields from ";

    private static final String KEY_AUTO_INCREMENT = "auto_increment";

    public BuildTable() throws ClassNotFoundException, SQLException {
        String driverName = PropertiesUtils.getString("db.driver.name");
        String url = PropertiesUtils.getString("db.url");
        String user = PropertiesUtils.getString("db.username");
        String password = PropertiesUtils.getString("db.password");
        Class.forName(driverName);
        conn = DriverManager.getConnection(url, user, password);
    }

    /**
     * 查询索引
     */
    private static final String SQL_PREFIX_SELECT_INDEX = "show index from ";

    public List<TableInfo> getTables() {
        PreparedStatement ps = null;
        ResultSet tableResults = null;
        try {
            ps = conn.prepareStatement(SQL_SHOW_TABLE_STATUS);
            tableResults = ps.executeQuery();


            List<TableInfo> tableInfoList = new ArrayList();
            //读取表
            while (tableResults.next()) {
                String tableName = tableResults.getString("name");
                String tableComment = tableResults.getString("comment");
                String beanName = tableName;
                if (Constants.IGNORE_TABLE_PREFIX) {
                    int index_prefix = tableName.indexOf("_");
                    if (index_prefix != -1) {
                        beanName = tableName.substring(index_prefix + 1);
                    }
                }
                beanName = processField(true, beanName);

                TableInfo tableInfo = new TableInfo();
                tableInfo.setTableName(tableName);
                tableInfo.setComment(tableComment);
                tableInfo.setBeanName(beanName);
                tableInfo.setBeanParamName(beanName + Constants.SUFFIX_BEAN_PARAM);
                // logger.info("表：{}，备注:{}，JaveBean:{}", tableName, tableComment, beanName);
                //获取字段信息

                Map<String, FieldInfo> fieldInfoMap = new HashMap();
                List<FieldInfo> fieldInfoList = readFieldInfo(tableInfo, fieldInfoMap);
                tableInfo.setFieldList(fieldInfoList);


                //读取主键
                getKeyIndexInfo(tableInfo, fieldInfoMap);

                tableInfoList.add(tableInfo);
            }
            return tableInfoList;
        } catch (Exception e) {
            logger.error("获取数据库表失败", e);
            throw new RuntimeException("获取数据库表失败");
        } finally {
            if (null != tableResults) {
                try {
                    tableResults.close();
                } catch (SQLException e) {
                    e.printStackTrace();
                }
            }
            if (ps != null) {
                try {
                    ps.close();
                } catch (SQLException e) {
                    e.printStackTrace();
                }
            }

            if (conn != null) {
                try {
                    conn.close();
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        }
    }

    private static void getKeyIndexInfo(TableInfo tableInfo, Map<String, FieldInfo> fieldInfoMap) {
        PreparedStatement ps = null;
        ResultSet results = null;
        try {
            ps = conn.prepareStatement(SQL_PREFIX_SELECT_INDEX + tableInfo.getTableName());
            results = ps.executeQuery();
            //获取表信息
            while (results.next()) {
                String keyName = results.getString("KEY_NAME");
                int nonUnique = results.getInt("NON_UNIQUE");
                String columnName = results.getString("COLUMN_NAME");
                if (nonUnique == 0) {//unique  唯一索引
                    List<FieldInfo> keyColumnList = tableInfo.getKeyIndexMap().get(keyName);
                    if (null == keyColumnList) {
                        keyColumnList = new ArrayList();
                        tableInfo.getKeyIndexMap().put(keyName, keyColumnList);
                    }
                    keyColumnList.add(fieldInfoMap.get(columnName));
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            if (null != results) {
                try {
                    results.close();
                } catch (SQLException e) {
                    e.printStackTrace();
                }
            }
            if (ps != null) {
                try {
                    ps.close();
                } catch (SQLException e) {
                    e.printStackTrace();
                }
            }
        }
    }

    private static List<FieldInfo> readFieldInfo(TableInfo tableInfo, Map<String, FieldInfo> fieldInfoMap) {
        PreparedStatement ps = null;
        ResultSet fieldResult = null;
        try {
            ps = conn.prepareStatement(SQL_PREFIX_SHOW_FIELDS + tableInfo.getTableName());
            fieldResult = ps.executeQuery();
            List<FieldInfo> filedInfoList = new ArrayList();
            while (fieldResult.next()) {
                String field = fieldResult.getString("FIELD");
                String type = fieldResult.getString("TYPE");
                String extra = fieldResult.getString("EXTRA");
                String comment = fieldResult.getString("COMMENT");

                if (type.indexOf("(") > 0) {
                    type = type.substring(0, type.indexOf("("));
                }


                String propertyName = processField(false, field);
                String javaType = processFieldType(type);

                //判断是否date类型
                if (ArrayUtils.contains(Constants.SQL_DATE_TYPES, type)) {
                    tableInfo.setHaveDate(true);
                }

                //判断是否有datetime类型
                if (ArrayUtils.contains(Constants.SQL_DATE_TIIME_TYPES, type)) {
                    tableInfo.setHaveDateTime(true);
                }
                //判断是否有bigdecimal类型
                if (ArrayUtils.contains(Constants.SQL_DECIMAL_TYPE, type)) {
                    tableInfo.setHaveBigDecimal(true);
                }

                FieldInfo fieldInfo = new FieldInfo();
                fieldInfo.setFieldName(field);
                fieldInfo.setPropertyName(propertyName);
                fieldInfo.setSqlType(type);
                fieldInfo.setJavaType(javaType);
                fieldInfo.setComment(comment);
                if (KEY_AUTO_INCREMENT.equalsIgnoreCase(extra)) {
                    fieldInfo.setAutoIncrement(true);
                } else {
                    fieldInfo.setAutoIncrement(false);
                }
                // logger.info("字段名:{},类型:{}，扩展:{}，备注:{}，Java类型:{},Jave属性名:{}", field, type, extra, comment, javaType, propertyName);
                filedInfoList.add(fieldInfo);

                fieldInfoMap.put(field, fieldInfo);

            }
            return filedInfoList;
        } catch (Exception e) {
            logger.error("读取表属性失败", e);
            throw new RuntimeException("读取表属性失败" + tableInfo.getTableName(), e);
        } finally {
            if (fieldResult != null) {
                try {
                    fieldResult.close();
                } catch (SQLException e) {
                    e.printStackTrace();
                }
            }
            if (ps != null) {
                try {
                    ps.close();
                } catch (SQLException e) {
                    e.printStackTrace();
                }
            }
        }
    }

    private static String processField(Boolean uperCaseFirstLetter, String field) {
        StringBuffer sb = new StringBuffer(field.length());
        String[] fields = field.toLowerCase().split("_");
        sb.append(uperCaseFirstLetter ? StringTools.upperCaseFirstLetter(fields[0]) : fields[0]);
        for (int i = 1, len = fields.length; i < len; i++) {
            sb.append(StringTools.upperCaseFirstLetter(fields[i]));
        }
        return sb.toString();
    }


    private static String processFieldType(String type) {

        if (ArrayUtils.contains(Constants.SQL_INTEGER_TYPE, type)) {
            return "Integer";
        } else if (ArrayUtils.contains(Constants.SQL_LONG_TYPE, type)) {
            return "Long";
        } else if (ArrayUtils.contains(Constants.SQL_STRING_TYPE, type)) {
            return "String";
        } else if (ArrayUtils.contains(Constants.SQL_DATE_TIIME_TYPES, type) || ArrayUtils.contains(Constants.SQL_DATE_TYPES, type)) {
            return "Date";
        } else if (ArrayUtils.contains(Constants.SQL_DECIMAL_TYPE, type)) {
            return "BigDecimal";
        } else {
            throw new RuntimeException("无法识别的类型:" + type);
        }
    }

}
