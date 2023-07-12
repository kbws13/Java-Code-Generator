package xyz.kbws.builder;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import xyz.kbws.bean.Constants;
import xyz.kbws.bean.FieldInfo;
import xyz.kbws.bean.TableInfo;
import xyz.kbws.utils.StringUtils;

import java.io.*;
import java.util.List;
import java.util.Map;

/**
 * @author hsy
 * @date 2023/7/2
 */
public class BuildService {
    private static final Logger logger = LoggerFactory.getLogger(BuildService.class);

    public static void execute(TableInfo tableInfo) {
        File folder = new File(Constants.PATH_SERVICE);
        if (!folder.exists()) {
            folder.mkdirs();
        }

        String className = tableInfo.getBeanName()+"Service";
        File poFile = new File(folder, className + ".java");

        OutputStream out = null;
        OutputStreamWriter outWrite = null;
        BufferedWriter bw = null;
        try {
            out = new FileOutputStream(poFile);
            outWrite = new OutputStreamWriter(out, "utf8");
            bw = new BufferedWriter(outWrite);

            bw.write("package " + Constants.PACKAGE_SERVICE + ";");
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

            bw.write("import "+Constants.PACKAGE_VO+".PaginationResultVO;");
            bw.newLine();
            bw.write("import "+Constants.PACKAGE_PO+"."+tableInfo.getBeanName()+";");
            bw.newLine();
            bw.write("import "+Constants.PACKAGE_QUERY+"."+tableInfo.getBeanParamName()+";");
            bw.newLine();
            bw.newLine();
            bw.write("import java.util.List;");
            bw.newLine();


            BuildComment.createClassComment(bw, tableInfo.getComment()+"Service");
            bw.write("public interface "+className+"{");
            bw.newLine();
            bw.newLine();

            BuildComment.createFieldComment(bw, "根据条件查询列表");
            bw.write("\tList<"+tableInfo.getBeanName()+"> findListByParam("+tableInfo.getBeanParamName()+" query);");
            bw.newLine();
            bw.newLine();

            BuildComment.createFieldComment(bw, "根据条件查询数量");
            bw.write("\tInteger findCountByParam("+tableInfo.getBeanParamName()+" query);");
            bw.newLine();
            bw.newLine();

            BuildComment.createFieldComment(bw, "分页查询");
            bw.write("\tPaginationResultVO<"+tableInfo.getBeanName()+"> findListByPage("+tableInfo.getBeanParamName()+" query);");
            bw.newLine();
            bw.newLine();

            BuildComment.createFieldComment(bw, "新增");
            bw.write("\tInteger add("+tableInfo.getBeanParamName()+" bean);");
            bw.newLine();
            bw.newLine();

            BuildComment.createFieldComment(bw, "批量新增");
            bw.write("\tInteger addBatch(List<"+tableInfo.getBeanParamName()+"> listBean);");
            bw.newLine();
            bw.newLine();

            BuildComment.createFieldComment(bw, "批量新增/修改");
            bw.write("\tInteger addOrUpdateBatch(List<"+tableInfo.getBeanParamName()+"> listBean);");
            bw.newLine();
            bw.newLine();

            for (Map.Entry<String, List<FieldInfo>> entry : tableInfo.getKeyIndexMap().entrySet()){
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

                    methodParams.append(fieldInfo.getJavaType() +" "+fieldInfo.getPropertyName());
                    if (index < keyFieldInfoList.size()){
                        methodParams.append(", ");
                    }
                }
                //查询
                BuildComment.createFieldComment(bw, "根据" + methodName + "查询");
                bw.write("\t"+tableInfo.getBeanName()+" get"+tableInfo.getBeanName()+"By" + methodName + "("+methodParams+");");
                bw.newLine();
                bw.newLine();
                //更新
                BuildComment.createFieldComment(bw, "根据" + methodName + "更新");
                bw.write("\tInteger update"+tableInfo.getBeanName()+"By" + methodName + "("+tableInfo.getBeanName()+" bean, "+methodParams+");");
                bw.newLine();
                bw.newLine();
                //删除
                BuildComment.createFieldComment(bw, "根据" + methodName + "删除");
                bw.write("\tInteger delete"+tableInfo.getBeanName()+"By" + methodName + "("+methodParams+");");
                bw.newLine();
                bw.newLine();
            }

            bw.write("}");

            bw.flush();
        } catch (Exception e) {
            logger.error("创建Service失败", e);
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
