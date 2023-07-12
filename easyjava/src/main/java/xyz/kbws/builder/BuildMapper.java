package xyz.kbws.builder;

import org.apache.commons.lang3.ArrayUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import xyz.kbws.bean.Constants;
import xyz.kbws.bean.FieldInfo;
import xyz.kbws.bean.TableInfo;
import xyz.kbws.utils.DateUtils;
import xyz.kbws.utils.StringUtils;

import java.io.*;
import java.util.List;
import java.util.Map;

/**
 * @author hsy
 * @date 2023/6/29
 */
public class BuildMapper {
    private static final Logger logger = LoggerFactory.getLogger(BuildMapper.class);

    public static void execute(TableInfo tableInfo) {
        File folder = new File(Constants.PATH_MAPPER);
        if (!folder.exists()) {
            folder.mkdirs();
        }

        String className = tableInfo.getBeanName() + Constants.SUFFIX_MAPPER;
        File poFile = new File(folder, className + ".java");

        OutputStream out = null;
        OutputStreamWriter outWrite = null;
        BufferedWriter bw = null;
        try {
            out = new FileOutputStream(poFile);
            outWrite = new OutputStreamWriter(out, "utf8");
            bw = new BufferedWriter(outWrite);

            bw.write("package " + Constants.PACKAGE_MAPPER + ";");
            bw.newLine();
            bw.newLine();

            bw.write("import org.apache.ibatis.annotations.Param;");
            bw.newLine();
            bw.write("import org.apache.ibatis.annotations.Mapper;");
            bw.newLine();
            bw.newLine();

            //构建类注释
            BuildComment.createClassComment(bw, tableInfo.getComment()+"Mapper");
            bw.write("@Mapper");
            bw.newLine();
            bw.write("public interface " + className + "<T, P> extends BaseMapper {");
            bw.newLine();

            Map<String, List<FieldInfo>> keyIndexMap = tableInfo.getKeyIndexMap();

            for (Map.Entry<String, List<FieldInfo>> entry : keyIndexMap.entrySet()){
                List<FieldInfo> keyFieldInfoList = entry.getValue();
                Integer index = 0;
                StringBuffer methodName = new StringBuffer();

                StringBuffer methodParams = new StringBuffer();

                for (FieldInfo fieldInfo : keyFieldInfoList){
                    index++;
                    methodName.append(StringUtils.upCaseFirstLetter(fieldInfo.getPropertyName()));
                    if (index < keyFieldInfoList.size()){
                        methodName.append("And");
                    }

                    methodParams.append("@Param(\""+fieldInfo.getPropertyName()+"\") "+fieldInfo.getJavaType()
                            +" "+fieldInfo.getPropertyName());
                    if (index < keyFieldInfoList.size()){
                        methodParams.append(", ");
                    }
                }
                //查询
                BuildComment.createFieldComment(bw, "根据" + methodName + "查询");
                bw.write("\tT selectBy" + methodName + "("+methodParams+");");
                bw.newLine();
                bw.newLine();
                //更新
                BuildComment.createFieldComment(bw, "根据" + methodName + "更新");
                bw.write("\tInteger updateBy" + methodName + "(@Param(\"bean\") T t, "+methodParams+");");
                bw.newLine();
                bw.newLine();
                //删除
                BuildComment.createFieldComment(bw, "根据" + methodName + "删除");
                bw.write("\tInteger deleteBy" + methodName + "("+methodParams+");");
                bw.newLine();
                bw.newLine();
            }

            bw.write("}");
            bw.flush();
        } catch (Exception e) {
            logger.error("创建mapper失败", e);
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
