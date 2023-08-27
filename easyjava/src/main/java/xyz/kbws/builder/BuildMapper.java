package xyz.kbws.builder;


import xyz.kbws.bean.Constants;
import xyz.kbws.bean.FieldInfo;
import xyz.kbws.bean.TableInfo;
import xyz.kbws.builder.BuildComment;
import xyz.kbws.utils.StringTools;

import java.io.*;
import java.util.List;
import java.util.Map;

public class BuildMapper {

    public static void execute(TableInfo tableInfo) {

        File folder = new File(Constants.PATH_MAPPER);
        if (!folder.exists()) {
            folder.mkdirs();
        }
        File beanFile = new File(Constants.PATH_MAPPER, tableInfo.getBeanName() + Constants.SUFFIX_MAPPER + ".java");
        OutputStream out = null;
        OutputStreamWriter outw = null;
        BufferedWriter bw = null;

        try {
            out = new FileOutputStream(beanFile);
            outw = new OutputStreamWriter(out, "utf-8");
            bw = new BufferedWriter(outw);
            bw.write("package " + Constants.PACKAGE_MAPPER + ";");
            bw.newLine();
            bw.newLine();
            bw.write("import org.apache.ibatis.annotations.Param;");
            bw = BuildComment.buildClassComment(bw, tableInfo.getComment() + " 数据库操作接口");
            bw.newLine();
            bw.write("public interface " + tableInfo.getBeanName() + Constants.SUFFIX_MAPPER + "<T,P> extends Base"
                    + Constants.SUFFIX_MAPPER + "<T,P> {");

            bw.newLine();

            Map<String, List<FieldInfo>> keyMap = tableInfo.getKeyIndexMap();
            for (Map.Entry<String, List<FieldInfo>> entry : keyMap.entrySet()) {
                List<FieldInfo> keyColumnList = entry.getValue();
                StringBuffer paramStr = new StringBuffer();
                StringBuffer methodName = new StringBuffer();
                int index = 0;
                for (FieldInfo column : keyColumnList) {
                    if (index > 0) {
                        paramStr.append(",");
                        methodName.append("And");
                    }
                    paramStr.append("@Param(\"" + column.getPropertyName() + "\") " + column.getJavaType() + " "
                            + column.getPropertyName() + "");
                    methodName.append(StringTools.upperCaseFirstLetter(column.getPropertyName()));
                    index++;
                }
                if (paramStr.length() > 0) {
                    BuildComment.buildMethodComment(bw, "根据" + methodName + "更新");
                    bw.newLine();
                    bw.write("\t Integer updateBy" + methodName.toString() + "(@Param(\"bean\") T t," + paramStr.toString
                            () + ")" + ";");
                    bw.newLine();
                    bw.newLine();

                    BuildComment.buildMethodComment(bw, "根据" + methodName + "删除");
                    bw.newLine();
                    bw.write("\t Integer deleteBy" + methodName.toString() + "(" + paramStr.toString() + ");");
                    bw.newLine();
                    bw.newLine();
                    BuildComment.buildMethodComment(bw, "根据" + methodName + "获取对象");
                    bw.newLine();
                    bw.write("\t T selectBy" + methodName.toString() + "(" + paramStr.toString() + ");");
                    bw.newLine();
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
