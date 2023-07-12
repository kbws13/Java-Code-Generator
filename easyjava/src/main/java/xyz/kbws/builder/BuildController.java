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
 * @date 2023/7/3
 */
public class BuildController {
    private static final Logger logger = LoggerFactory.getLogger(BuildController.class);

    public static void execute(TableInfo tableInfo) {
        File folder = new File(Constants.PATH_CONTROLLER);
        if (!folder.exists()) {
            folder.mkdirs();
        }
        String className = tableInfo.getBeanName()+"Controller";
        File poFile = new File(folder, className + ".java");

        OutputStream out = null;
        OutputStreamWriter outWrite = null;
        BufferedWriter bw = null;
        try {
            out = new FileOutputStream(poFile);
            outWrite = new OutputStreamWriter(out, "utf8");
            bw = new BufferedWriter(outWrite);

            bw.write("package " + Constants.PACKAGE_CONTROLLER + ";");
            bw.newLine();

            String serviceName = tableInfo.getBeanName()+"Service";
            String serviceBeanName = StringUtils.lowCaseFirstLetter(serviceName);
            bw.write("import "+Constants.PACKAGE_PO+"."+tableInfo.getBeanName()+";");
            bw.newLine();
            bw.write("import "+Constants.PACKAGE_QUERY+"."+tableInfo.getBeanParamName()+";");
            bw.newLine();
            bw.write("import "+Constants.PACKAGE_SERVICE+"."+serviceName+";");
            bw.newLine();
            bw.write("import "+Constants.PACKAGE_VO+".ResponseVO;");
            bw.newLine();
            bw.write("import org.springframework.web.bind.annotation.RestController;");
            bw.newLine();
            bw.write("import org.springframework.web.bind.annotation.RequestMapping;");
            bw.newLine();
            bw.write("import org.springframework.web.bind.annotation.RequestMapping;");
            bw.newLine();
            bw.write("import org.springframework.web.bind.annotation.RequestBody;");
            bw.newLine();
            bw.write("import javax.annotation.Resource;");
            bw.newLine();
            bw.write("import java.util.List;");
            bw.newLine();


            BuildComment.createClassComment(bw, tableInfo.getComment()+"Controller");
            //bw.write("@RestController(\""+mapperBeanName+"\")");
            bw.write("@RestController");
            bw.newLine();
            bw.write("@RequestMapping(\"/"+StringUtils.lowCaseFirstLetter(tableInfo.getBeanName())+"\")");
            bw.newLine();
            bw.write("public class "+className+" extends ABaseController{");
            bw.newLine();
            bw.newLine();

            bw.write("\t@Resource");
            bw.newLine();
            bw.write("\tprivate "+serviceName+" "+serviceBeanName+";");
            bw.newLine();

            bw.write("\t@RequestMapping(\"localDataList\")");
            bw.newLine();
            bw.write("\tpublic ResponseVO loadDataList ("+tableInfo.getBeanParamName()+" query) {");
            bw.newLine();
            bw.write("\t\treturn getSuccessResponseVO("+serviceBeanName+".findListByPage(query));");
            bw.newLine();
            bw.write("\t}");
            bw.newLine();

            BuildComment.createFieldComment(bw, "新增");
            bw.write("\t@RequestMapping(\"add\")");
            bw.newLine();
            bw.write("\tpublic ResponseVO add("+tableInfo.getBeanParamName()+" bean){");
            bw.newLine();
            bw.write("\t\tthis."+serviceBeanName+".add(bean);");
            bw.newLine();
            bw.write("\t\treturn getSuccessResponseVO(\"添加成功\");");
            bw.newLine();
            bw.write("\t}");
            bw.newLine();

            BuildComment.createFieldComment(bw, "批量新增");
            bw.write("\t@RequestMapping(\"addBatch\")");
            bw.newLine();
            bw.write("\tpublic ResponseVO addBatch(@RequestBody List<"+tableInfo.getBeanParamName()+"> listBean){");
            bw.newLine();
            bw.write("\t\tthis."+serviceBeanName+".addBatch(listBean);");
            bw.newLine();
            bw.write("\t\treturn getSuccessResponseVO(\"批量添加成功\");");
            bw.newLine();
            bw.write("\t}");
            bw.newLine();

            BuildComment.createFieldComment(bw, "批量新增/修改");
            bw.write("\t@RequestMapping(\"addOrUpdateBatch\")");
            bw.newLine();
            bw.write("\tpublic ResponseVO addOrUpdateBatch(@RequestBody List<"+tableInfo.getBeanParamName()+"> listBean){");
            bw.newLine();
            bw.write("\t\tthis."+serviceBeanName+".addOrUpdateBatch(listBean);");
            bw.newLine();
            bw.write("\t\treturn getSuccessResponseVO(\"批量添加/修改成功\");");
            bw.newLine();
            bw.write("\t}");
            bw.newLine();

            for (Map.Entry<String, List<FieldInfo>> entry : tableInfo.getKeyIndexMap().entrySet()){
                List<FieldInfo> keyFieldInfoList = entry.getValue();
                Integer index = 0;
                StringBuilder methodName = new StringBuilder();

                StringBuilder methodParams = new StringBuilder();
                StringBuilder paramsBuilder = new StringBuilder();
                for (FieldInfo fieldInfo : keyFieldInfoList){
                    index++;
                    methodName.append(StringUtils.upCaseFirstLetter(fieldInfo.getPropertyName()));
                    if (index < keyFieldInfoList.size()){
                        methodName.append("And");
                    }

                    methodParams.append(fieldInfo.getJavaType() +" "+fieldInfo.getPropertyName());
                    paramsBuilder.append(fieldInfo.getPropertyName());
                    if (index < keyFieldInfoList.size()){
                        methodParams.append(", ");
                        paramsBuilder.append(", ");
                    }
                }
                //查询
                BuildComment.createFieldComment(bw, "根据" + methodName + "查询");
                bw.write("\t@RequestMapping(\"get"+tableInfo.getBeanName()+"By" + methodName+"\")");
                bw.newLine();
                bw.write("\tpublic ResponseVO get"+tableInfo.getBeanName()+"By" + methodName + "("+methodParams+"){");
                bw.newLine();
                bw.write("\t\treturn getSuccessResponseVO(this."+serviceBeanName+".get"+tableInfo.getBeanName()+"By"+methodName+"("+paramsBuilder+"));");
                bw.newLine();
                bw.write("\t}");
                bw.newLine();
                //更新
                BuildComment.createFieldComment(bw, "根据" + methodName + "更新");
                bw.write("\t@RequestMapping(\"update"+tableInfo.getBeanName()+"By" + methodName+"\")");
                bw.newLine();
                bw.write("\tpublic ResponseVO update"+tableInfo.getBeanName()+"By" + methodName +
                        "("+tableInfo.getBeanName()+" bean, "+methodParams+"){");
                bw.newLine();
                bw.write("\t\tthis."+serviceBeanName+".update"+tableInfo.getBeanName()+"By"+methodName+"(bean,"+paramsBuilder+");");
                bw.newLine();
                bw.write("\t\treturn getSuccessResponseVO(\"更新成功\");");
                bw.newLine();
                bw.write("\t}");
                bw.newLine();
                //删除
                BuildComment.createFieldComment(bw, "根据" + methodName + "删除");
                bw.write("\t@RequestMapping(\"delete"+tableInfo.getBeanName()+"By" + methodName+"\")");
                bw.newLine();
                bw.write("\tpublic ResponseVO delete"+tableInfo.getBeanName()+"By" + methodName + "("+methodParams+"){");
                bw.newLine();
                bw.write("\t\tthis."+serviceBeanName+".delete"+tableInfo.getBeanName()+"By"+methodName+"("+paramsBuilder+");");
                bw.newLine();
                bw.write("\t\treturn getSuccessResponseVO(\"删除成功\");");
                bw.newLine();
                bw.write("\t}");
                bw.newLine();
            }

            bw.write("}");

            bw.flush();
        } catch (Exception e) {
            logger.error("创建Service Impl失败", e);
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
