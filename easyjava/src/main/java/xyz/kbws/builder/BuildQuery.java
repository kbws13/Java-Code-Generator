package xyz.kbws.builder;

import org.apache.commons.lang3.ArrayUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import xyz.kbws.bean.Constants;
import xyz.kbws.bean.FieldInfo;
import xyz.kbws.bean.TableInfo;
import xyz.kbws.utils.DateUtils;

import java.io.*;
import java.util.ArrayList;
import java.util.List;

/**
 * @author hsy
 * @date 2023/6/28
 */
public class BuildQuery {
    private static final Logger logger = LoggerFactory.getLogger(BuildPo.class);

    public static void execute(TableInfo tableInfo) {
        File folder = new File(Constants.PATH_QUERY);
        if (!folder.exists()) {
            folder.mkdirs();
        }

        String className = tableInfo.getBeanName() + Constants.SUFFIX_BEAN_QUERY;
        File poFile = new File(folder, className + ".java");

        OutputStream out = null;
        OutputStreamWriter outWrite = null;
        BufferedWriter bw = null;
        try {
            out = new FileOutputStream(poFile);
            outWrite = new OutputStreamWriter(out, "utf8");
            bw = new BufferedWriter(outWrite);

            bw.write("package " + Constants.PACKAGE_QUERY + ";");
            bw.newLine();
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

            bw.write("public class " + className +" extends BaseQuery {");
            bw.newLine();

            for (FieldInfo field : tableInfo.getFieldList()) {
                BuildComment.createFieldComment(bw, field.getComment());
                bw.write("\tprivate " + field.getJavaType() + " " + field.getPropertyName() + ";");
                bw.newLine();
                bw.newLine();

                //String类型的参数
                if (ArrayUtils.contains(Constants.SQL_STRING_TYPES, field.getSqlType())){
                    String propertyName = field.getPropertyName() + Constants.SUFFIX_BEAN_QUERY_FUZZY;
                    bw.write("\tprivate "+ field.getJavaType() + " " +propertyName +";");
                    bw.newLine();
                }

                if (ArrayUtils.contains(Constants.SQL_DATE_TIME_TYPES, field.getSqlType()) ||
                        ArrayUtils.contains(Constants.SQL_DATE_TYPES, field.getSqlType())){
                    bw.write("\tprivate String " + field.getPropertyName() + Constants.SUFFIX_BEAN_QUERY_FUZZY_TIME_START +";");
                    bw.newLine();
                    bw.newLine();

                    bw.write("\tprivate String " + field.getPropertyName() + Constants.SUFFIX_BEAN_QUERY_FUZZY_TIME_END +";");
                    bw.newLine();
                    bw.newLine();
                }
            }
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
