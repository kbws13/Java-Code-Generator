package xyz.kbws.builder;


import xyz.kbws.bean.Constants;
import xyz.kbws.bean.FieldInfo;
import xyz.kbws.bean.TableInfo;
import xyz.kbws.builder.BuildComment;
import xyz.kbws.utils.StringTools;

import java.io.*;
import java.util.List;

public class BuildBeanQuery {

    public static void execute(TableInfo tableInfo) {
        File folder = new File(Constants.PATH_PARAM);
        if (!folder.exists()) {
            folder.mkdirs();
        }
        File beanFile = new File(Constants.PATH_PARAM, tableInfo.getBeanName() + Constants.SUFFIX_BEAN_PARAM + ".java");
        OutputStream out = null;
        OutputStreamWriter outw = null;
        BufferedWriter bw = null;

        try {
            out = new FileOutputStream(beanFile);
            outw = new OutputStreamWriter(out, "utf-8");
            bw = new BufferedWriter(outw);
            bw.write("package " + Constants.PACKAGE_PARAM + ";");
            bw.newLine();
            bw.newLine();

            if (tableInfo.getHaveBigDecimal() != null && tableInfo.getHaveBigDecimal()) {
                bw.write("import java.math.BigDecimal;");
                bw.newLine();
            }
            if (tableInfo.getHaveDate() != null && tableInfo.getHaveDate() || tableInfo.getHaveDateTime() != null && tableInfo.getHaveDateTime()) {
                bw.write("import java.util.Date;");
                bw.newLine();
            }

            bw = BuildComment.buildClassComment(bw, tableInfo.getComment() + "参数");
            bw.newLine();
            bw.write("public class " + tableInfo.getBeanName() + Constants.SUFFIX_BEAN_PARAM + " extends BaseParam {");
            bw.newLine();
            bw.newLine();
            List<FieldInfo> columnList = tableInfo.getFieldList();

            for (FieldInfo columnInfo : columnList) {
                BuildComment.buildPropertyComment(bw, columnInfo.getComment());
                bw.newLine();
                if (!Constants.TYPE_DATE.equals(columnInfo.getJavaType())) {
                    bw.write("\tprivate " + columnInfo.getJavaType() + " " + columnInfo.getPropertyName() + ";");
                    bw.newLine();
                }
                //添加模糊搜索条件
                if (Constants.TYPE_STRING.equals(columnInfo.getJavaType())) {
                    bw.newLine();
                    bw.write("\tprivate " + columnInfo.getJavaType() + " " + columnInfo.getPropertyName() + Constants.SUFFIX_PROPERTY_FUZZY + ";");
                    bw.newLine();
                }
                //添加时间日期搜索
                if (Constants.TYPE_DATE.equals(columnInfo.getJavaType())) {
                    bw.write("\tprivate " + Constants.TYPE_STRING + " " + columnInfo.getPropertyName() + ";");
                    bw.newLine();
                    String start = columnInfo.getPropertyName() + Constants.SUFFIX_BEAN_PARAM_TIME_START;
                    String end = columnInfo.getPropertyName() + Constants.SUFFIX_BEAN_PARAM_TIME_END;
                    bw.newLine();
                    bw.write("\tprivate " + Constants.TYPE_STRING + " " + start + ";");
                    bw.newLine();
                    bw.newLine();
                    bw.write("\tprivate " + Constants.TYPE_STRING + " " + end + ";");
                    bw.newLine();
                }
            }
            bw.newLine();
            // 生成get 和 set方法
            String tempField = null;
            for (FieldInfo columnInfo : columnList) {
                tempField = StringTools.upperCaseFirstLetter(columnInfo.getPropertyName());
                if (!Constants.TYPE_DATE.equals(columnInfo.getJavaType())) {
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
                } else {
                    bw.newLine();
                    bw.write("\tpublic void set" + tempField + "(" + Constants.TYPE_STRING + " "
                            + columnInfo.getPropertyName() + "){");
                    bw.newLine();
                    bw.write("\t\tthis." + columnInfo.getPropertyName() + " = " + columnInfo.getPropertyName() + ";");
                    bw.newLine();
                    bw.write("\t}");
                    bw.newLine();
                    bw.newLine();
                    bw.write("\tpublic " + Constants.TYPE_STRING + " get" + tempField + "(){");
                    bw.newLine();
                    bw.write("\t\treturn this." + columnInfo.getPropertyName() + ";");
                    bw.newLine();
                    bw.write("\t}");
                    bw.newLine();
                }

                if (Constants.TYPE_STRING.equals(columnInfo.getJavaType())) {
                    bw.newLine();
                    bw.write("\tpublic void set" + tempField + Constants.SUFFIX_PROPERTY_FUZZY + "("
                            + columnInfo.getJavaType() + " " + columnInfo.getPropertyName()
                            + Constants.SUFFIX_PROPERTY_FUZZY + "){");
                    bw.newLine();
                    bw.write("\t\tthis." + columnInfo.getPropertyName() + Constants.SUFFIX_PROPERTY_FUZZY + " = "
                            + columnInfo.getPropertyName() + Constants.SUFFIX_PROPERTY_FUZZY + ";");
                    bw.newLine();
                    bw.write("\t}");
                    bw.newLine();
                    bw.newLine();
                    bw.write("\tpublic " + columnInfo.getJavaType() + " get" + tempField + Constants.SUFFIX_PROPERTY_FUZZY
                            + "(){");
                    bw.newLine();
                    bw.write("\t\treturn this." + columnInfo.getPropertyName() + Constants.SUFFIX_PROPERTY_FUZZY + ";");
                    bw.newLine();
                    bw.write("\t}");
                    bw.newLine();
                }

                if (Constants.TYPE_DATE.equals(columnInfo.getJavaType())) {
                    String start = columnInfo.getPropertyName() + Constants.SUFFIX_BEAN_PARAM_TIME_START;
                    String end = columnInfo.getPropertyName() + Constants.SUFFIX_BEAN_PARAM_TIME_END;
                    tempField = start.substring(0, 1).toUpperCase() + start.substring(1);
                    //开始日期
                    bw.newLine();
                    bw.write("\tpublic void set" + tempField + "(" + Constants.TYPE_STRING + " " + start + "){");
                    bw.newLine();
                    bw.write("\t\tthis." + start + " = " + start + ";");
                    bw.newLine();
                    bw.write("\t}");
                    bw.newLine();
                    bw.newLine();
                    bw.write("\tpublic " + Constants.TYPE_STRING + " get" + tempField + "(){");
                    bw.newLine();
                    bw.write("\t\treturn this." + start + ";");
                    bw.newLine();
                    bw.write("\t}");

                    //结束日期
                    tempField = end.substring(0, 1).toUpperCase() + end.substring(1);
                    bw.newLine();
                    bw.write("\tpublic void set" + tempField + "(" + Constants.TYPE_STRING + " " + end + "){");
                    bw.newLine();
                    bw.write("\t\tthis." + end + " = " + end + ";");
                    bw.newLine();
                    bw.write("\t}");
                    bw.newLine();
                    bw.newLine();
                    bw.write("\tpublic " + Constants.TYPE_STRING + " get" + tempField + "(){");
                    bw.newLine();
                    bw.write("\t\treturn this." + end + ";");
                    bw.newLine();
                    bw.write("\t}");
                    bw.newLine();
                }
            }
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
