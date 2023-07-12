package xyz.kbws.builder;

import org.apache.commons.lang3.ArrayUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import xyz.kbws.bean.Constants;
import xyz.kbws.bean.FieldInfo;
import xyz.kbws.bean.TableInfo;
import xyz.kbws.utils.DateUtils;

import java.io.*;

/**
 * @author hsy
 * @date 2023/6/26
 */
public class BuildPo {
    private static final Logger logger = LoggerFactory.getLogger(BuildPo.class);

    public static void execute(TableInfo tableInfo) {
        File folder = new File(Constants.PATH_PO);
        if (!folder.exists()) {
            folder.mkdirs();
        }

        File poFile = new File(folder, tableInfo.getBeanName() + ".java");

        OutputStream out = null;
        OutputStreamWriter outWrite = null;
        BufferedWriter bw = null;
        try {
            out = new FileOutputStream(poFile);
            outWrite = new OutputStreamWriter(out, "utf8");
            bw = new BufferedWriter(outWrite);

            bw.write("package " + Constants.PACKAGE_PO + ";");
            bw.newLine();
            bw.newLine();

            bw.write("import java.io.Serializable;");
            bw.newLine();
            bw.write("import lombok.Data;");
            bw.newLine();
            bw.write("import lombok.AllArgsConstructor;");
            bw.newLine();
            bw.write("import lombok.NoArgsConstructor;");
            bw.newLine();

            if (tableInfo.getHaveDateTime() || tableInfo.getHaveDate()) {
                bw.write("import java.util.Date;");
                bw.newLine();
                bw.write(Constants.BEAN_DATE_FORMAT_CLASS);
                bw.newLine();
                bw.write(Constants.BEAN_DATE_UNFORMAT_CLASS);
                bw.newLine();
                bw.write("import " + Constants.PACKAGE_ENUMS + ".DateTimePatternEnum;");
                bw.newLine();
                bw.write("import " + Constants.PACKAGE_UTILS + ".DateUtils;");
                bw.newLine();
            }

            //忽略属性
            Boolean haveIgnoreBean = false;
            for (FieldInfo field : tableInfo.getFieldList()) {
                if (ArrayUtils.contains(Constants.IGNORE_BEAN_TOJSON_FIELD.split(","), field.getPropertyName())) {
                    haveIgnoreBean = true;
                    break;

                }
            }

            if (haveIgnoreBean) {
                bw.write(Constants.IGNORE_BEAN_TOJSON_CLASS);
                bw.newLine();
            }

            if (tableInfo.getHaveBigDecimal()) {
                bw.write("import java.math.BigDecimal;");
            }
            bw.newLine();
            bw.newLine();

            //构建类注释
            BuildComment.createClassComment(bw, tableInfo.getComment());

            bw.write("@Data");
            bw.newLine();
            bw.write("@AllArgsConstructor");
            bw.newLine();
            bw.write("@NoArgsConstructor");
            bw.newLine();

            bw.write("public class " + tableInfo.getBeanName() + " implements Serializable {");
            bw.newLine();


            for (FieldInfo field : tableInfo.getFieldList()) {
                BuildComment.createFieldComment(bw, field.getComment());

                if (ArrayUtils.contains(Constants.SQL_DATE_TIME_TYPES, field.getSqlType())) {
                    bw.write("\t" + String.format(Constants.BEAN_DATE_FORMAT_EXPRESSION, DateUtils.YYYY_MM_DD_HH_MM_SS));
                    bw.newLine();
                    bw.write("\t" + String.format(Constants.BEAN_DATE_UNFORMAT_EXPRESSION, DateUtils.YYYY_MM_DD_HH_MM_SS));
                    bw.newLine();
                }

                if (ArrayUtils.contains(Constants.SQL_DATE_TYPES, field.getSqlType())) {
                    bw.write("\t" + String.format(Constants.BEAN_DATE_FORMAT_EXPRESSION, DateUtils.YYYY_MM_DD));
                    bw.newLine();
                    bw.write("\t" + String.format(Constants.BEAN_DATE_UNFORMAT_EXPRESSION, DateUtils.YYYY_MM_DD));
                    bw.newLine();
                }


                if (ArrayUtils.contains(Constants.IGNORE_BEAN_TOJSON_FIELD.split(","), field.getPropertyName())) {
                    bw.write("\t" + String.format(Constants.IGNORE_BEAN_TOJSON_EXPRESSION, DateUtils.YYYY_MM_DD));
                    bw.newLine();
                }

                bw.write("\tprivate " + field.getJavaType() + " " + field.getPropertyName() + ";");
                bw.newLine();
                bw.newLine();
            }

            StringBuffer toString = new StringBuffer();
            //重写toString方法
            Integer index = 0;
            for (FieldInfo field : tableInfo.getFieldList()) {
                index++;

                String properName = field.getPropertyName();
                if (ArrayUtils.contains(Constants.SQL_DATE_TIME_TYPES, field.getSqlType())) {
                    properName = "DateUtils.format(" + properName + ", DateTimePatternEnum.YYYY_MM_DD_HH_MM_SS.getPattern())";
                } else if (ArrayUtils.contains(Constants.SQL_DATE_TYPES, field.getSqlType())) {
                    properName = "DateUtils.format(" + properName + ", DateTimePatternEnum.YYYY_MM_DD.getPattern())";
                }
                toString.append(field.getComment() + ":\" + ( " + field.getPropertyName() + "== null ? \"空\" : " + properName + ")");
                if (index < tableInfo.getFieldList().size()) {
                    toString.append(" + ").append("\",");
                }
            }
            String toStringStr = toString.toString();
            toStringStr = "\"" + toStringStr;
            toStringStr.substring(0, toStringStr.lastIndexOf(","));
            bw.write("\t@Override");
            bw.newLine();
            bw.write("\tpublic String toString (){");
            bw.newLine();
            bw.write("\t\treturn " + toStringStr + ";");
            bw.newLine();
            bw.write("\t}");
            bw.newLine();

            bw.write("}");
            bw.flush();
        } catch (Exception e) {
            logger.error("创建po失败", e);
        } finally {
            if (bw != null) {
                try {
                    bw.close();
                } catch (IOException e) {
                    throw new RuntimeException(e);
                }
            }
            if (outWrite != null) {
                try {
                    outWrite.close();
                } catch (IOException e) {
                    throw new RuntimeException(e);
                }
            }
            if (out != null) {
                try {
                    out.close();
                } catch (IOException e) {
                    throw new RuntimeException(e);
                }
            }
        }
    }
}
