package xyz.kbws.builder;


import org.apache.commons.lang3.ArrayUtils;
import org.apache.commons.lang3.StringUtils;
import xyz.kbws.bean.Constants;
import xyz.kbws.bean.FieldInfo;
import xyz.kbws.bean.TableInfo;
import xyz.kbws.builder.BuildComment;
import xyz.kbws.utils.StringTools;

import java.io.*;
import java.util.List;

public class BuildBeanPo {

    public static void execute(TableInfo tableInfo) {

        File folder = new File(Constants.PATH_BEAN);
        if (!folder.exists()) {
            folder.mkdirs();
        }
        File beanFile = new File(Constants.PATH_BEAN, tableInfo.getBeanName() + ".java");
        OutputStream out = null;
        OutputStreamWriter outw = null;
        BufferedWriter bw = null;

        try {
            out = new FileOutputStream(beanFile);
            outw = new OutputStreamWriter(out, "utf-8");
            bw = new BufferedWriter(outw);
            bw.write("package " + Constants.PACKAGE_BEAN + ";");
            bw.newLine();
            bw.newLine();
            //导入 需要忽略的属性包 JsonIgnore包
            if (Constants.IGNORE_BEAN_TOJSON_CLASS != null) {
                bw.write("import " + Constants.IGNORE_BEAN_TOJSON_CLASS + ";");
                bw.newLine();
            }
            if (tableInfo.getHaveBigDecimal() != null && tableInfo.getHaveBigDecimal()) {
                bw.write("import java.math.BigDecimal;");
                bw.newLine();
            }
            if (tableInfo.getHaveDate() != null && tableInfo.getHaveDate() || tableInfo.getHaveDateTime() != null && tableInfo.getHaveDateTime()) {
                bw.write("import java.util.Date;");
                bw.newLine();
                bw.write("import " + Constants.PACKAGE_ENUMS + ".DateTimePatternEnum;");
                bw.newLine();
                bw.write("import " + Constants.PACKAGE_BASE + ".utils.DateUtil;");
                bw.newLine();
                bw.write("import " + Constants.BEAN_DATE_EXPRESSION_CLASS + ";");
                bw.newLine();
                bw.write("import " + Constants.BEAN_DATE_FORMAT_CLASS + ";");
                bw.newLine();
                bw.newLine();

            }
            bw.write("import java.io.Serializable;");
            bw.newLine();
            bw = BuildComment.buildClassComment(bw, tableInfo.getComment());
            bw.newLine();
            bw.write("public class " + tableInfo.getBeanName() + " implements Serializable {");
            bw.newLine();
            bw.newLine();
            List<FieldInfo> columnList = tableInfo.getFieldList();

            for (FieldInfo columnInfo : columnList) {
                BuildComment.buildPropertyComment(bw, columnInfo.getComment());
                bw.newLine();
                //需要过滤的属性添加@JsonIgnore
                if (Constants.IGNORE_BEAN_TOJSON_COLUMN != null) {
                    Boolean ignore = false;
                    for (String column : Constants.IGNORE_BEAN_TOJSON_COLUMN) {
                        if (columnInfo.getFieldName().equalsIgnoreCase(column)) {
                            ignore = true;
                        }
                    }
                    if (ignore) {
                        bw.write("\t" + Constants.IGNORE_BEAN_TOJSON_EXPRESSION);
                        bw.newLine();
                    }
                }
                //判断是否是日期，或者时间类型
                if (ArrayUtils.contains(Constants.SQL_DATE_TYPES, columnInfo.getSqlType()) || ArrayUtils.contains(Constants.SQL_DATE_TIIME_TYPES,
                        columnInfo.getSqlType())) {
                    String dateTimePattern = "yyyy-MM-dd HH:mm:ss";
                    if (ArrayUtils.contains(Constants.SQL_DATE_TYPES, columnInfo.getSqlType())) {
                        dateTimePattern = "yyyy-MM-dd";
                    }
                    bw.write("\t" + String.format(Constants.BEAN_DATE_EXPRESSION, dateTimePattern));
                    bw.newLine();
                    //参数格式化
                    bw.write("\t" + String.format(Constants.BEAN_DATE_FORMAT, dateTimePattern));
                    bw.newLine();
                }
                bw.write("\tprivate " + columnInfo.getJavaType() + " " + columnInfo.getPropertyName() + ";");
                bw.newLine();
            }
            bw.newLine();
            // 生成get 和 set方法
            String tempField = null;
            for (FieldInfo columnInfo : columnList) {
                tempField = StringTools.upperCaseFirstLetter(columnInfo.getPropertyName());
                bw.newLine();
                bw.write("\tpublic void set" + tempField + "(" + columnInfo.getJavaType() + " "
                        + columnInfo.getPropertyName() + "){");
                bw.newLine();
                bw.write("\t\tthis." + columnInfo.getPropertyName() + " = " + columnInfo.getPropertyName() + ";");
                bw.newLine();
                bw.write("\t}");
                bw.newLine();
                bw.newLine();
                bw.write("\tpublic " + columnInfo.getJavaType() + " get" + tempField + "(){");
                bw.newLine();
                bw.write("\t\treturn this." + columnInfo.getPropertyName() + ";");
                bw.newLine();
                bw.write("\t}");
                bw.newLine();
            }

            bw.newLine();
            //重写toString方法
            bw.write("\t@Override");
            bw.newLine();
            bw.write("\tpublic String toString (){");
            StringBuilder tostringStr = new StringBuilder();
            for (FieldInfo columnInfo : columnList) {
                if (Constants.IGNORE_BEAN_TOSTRING_COLUMN != null) {
                    Boolean ignore = false;
                    for (String column : Constants.IGNORE_BEAN_TOSTRING_COLUMN) {
                        if (columnInfo.getFieldName().equalsIgnoreCase(column)) {
                            ignore = true;
                        }
                    }
                    if (ignore) {
                        continue;
                    }
                }
                String properName = columnInfo.getComment();
                properName = StringUtils.isEmpty(properName) ? columnInfo.getPropertyName() : properName;
                String clumn = columnInfo.getPropertyName();
                if (ArrayUtils.contains(Constants.SQL_DATE_TIIME_TYPES, columnInfo.getSqlType())) {
                    clumn = "(" + clumn + " == null ? \"空\" : DateUtil.format(" + clumn + ", " +
                            "DateTimePatternEnum.YYYY_MM_DD_HH_MM_SS" +
                            ".getPattern()))";
                } else if (ArrayUtils.contains(Constants.SQL_DATE_TYPES, columnInfo.getSqlType())) {
                    clumn = "(" + clumn + " == null ? \"空\" : DateUtil.format(" + clumn + ", " +
                            "DateTimePatternEnum.YYYY_MM_DD" +
                            ".getPattern()))";
                } else {
                    clumn = "(" + clumn + " == null ? \"空\" : " + clumn + ")";
                }

                tostringStr.append("\"，" + properName + ":\"+" + clumn + "+");
            }

            String strResult = "\"" + tostringStr.substring(2, tostringStr.length() - 1) + ";";

            bw.newLine();
            bw.write("\t\treturn " + strResult);
            bw.newLine();
            bw.write("\t}");

            bw.newLine();
            bw.write("}");
            bw.newLine();
            bw.flush();
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            if (null != out) {
                try {
                    out.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
            if (outw != null) {
                try {
                    outw.close();
                } catch (IOException e) {
                    e.printStackTrace();

                }
            }
            if (null != bw) {
                try {
                    bw.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }
    }
}
