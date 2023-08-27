package xyz.kbws.builder;


import xyz.kbws.bean.Constants;
import xyz.kbws.bean.FieldInfo;
import xyz.kbws.bean.TableInfo;
import xyz.kbws.builder.BuildComment;
import xyz.kbws.utils.StringTools;

import java.io.*;
import java.util.List;
import java.util.Map;

public class BuildController {

    public static void execute(TableInfo tableInfo) {

        File folder = new File(Constants.PATH_CONTROLLER);
        if (!folder.exists()) {
            folder.mkdirs();
        }
        File beanFile = new File(Constants.PATH_CONTROLLER, tableInfo.getBeanName() + Constants.SUFFIX_CONTROLLER + ".java");
        OutputStream out = null;
        OutputStreamWriter outw = null;
        BufferedWriter bw = null;

        try {
            out = new FileOutputStream(beanFile);
            outw = new OutputStreamWriter(out, "utf-8");
            bw = new BufferedWriter(outw);
            bw.write("package " + Constants.PACKAGE_CONTROLLER + ";");
            bw.newLine();
            bw.newLine();
            bw.write("import java.util.List;");
            bw.newLine();
            bw.newLine();
            bw.write("import " + Constants.PACKAGE_PARAM + "." + tableInfo.getBeanParamName() + ";");
            bw.newLine();
            bw.write("import " + Constants.PACKAGE_BEAN + "." + tableInfo.getBeanName() + ";");
            bw.newLine();
            bw.write("import " + Constants.PACKAGE_VO + ".ResponseVO;");
            bw.newLine();
            bw.write("import " + Constants.PACKAGE_SERVICE + "." + tableInfo.getBeanName() + "Service;");
            bw.newLine();
            bw.write("import org.springframework.web.bind.annotation.RequestBody;");
            bw.newLine();
            bw.write("import org.springframework.web.bind.annotation.RequestMapping;");
            bw.newLine();
            bw.write("import org.springframework.web.bind.annotation.RestController;");
            bw.newLine();
            bw.newLine();
            bw.write("import javax.annotation.Resource;");

            //所有属性
            List<FieldInfo> fieldInfoList = tableInfo.getFieldList();
            bw = BuildComment.buildClassComment(bw, tableInfo.getComment() + " Controller");
            bw.newLine();
            bw.write("@RestController(\"" + StringTools.lowerCaseFirstLetter(tableInfo.getBeanName()) + Constants.SUFFIX_CONTROLLER + "\")");
            bw.newLine();
            bw.write("@RequestMapping(\"/" + StringTools.lowerCaseFirstLetter(tableInfo.getBeanName()) + "\")");
            bw.newLine();
            bw.write("public class " + tableInfo.getBeanName() + Constants.SUFFIX_CONTROLLER + " extends ABaseController{");
            bw.newLine();

            String beanName = tableInfo.getBeanName();

            bw.newLine();
            bw.write("\t@Resource");
            bw.newLine();
            String serviceBean = StringTools.lowerCaseFirstLetter(beanName + "Service");
            bw.write("\tprivate " + tableInfo.getBeanName() + Constants.SUFFIX_SERVICE + " " + serviceBean + ";");

            //根据条件查询列表
            bw = BuildComment.buildMethodComment(bw, "根据条件分页查询");
            bw.newLine();
            bw.write("\t@RequestMapping(\"/loadDataList\")");
            bw.newLine();
            String paramName = StringTools.lowerCaseFirstLetter(Constants.SUFFIX_BEAN_PARAM);
            bw.write("\tpublic ResponseVO loadDataList(" + tableInfo.getBeanParamName() + " " + paramName + "){");
            bw.newLine();

            bw.write("\t\treturn getSuccessResponseVO(" + serviceBean + ".findListByPage(" + paramName + "));");
            bw.newLine();
            bw.write("\t}");
            bw.newLine();

            //新增
            bw = BuildComment.buildMethodComment(bw, "新增");
            bw.newLine();
            bw.write("\t@RequestMapping(\"/add\")");
            bw.newLine();
            bw.write("\tpublic ResponseVO add(" + tableInfo.getBeanName() + " bean) {");
            bw.newLine();
            bw.write("\t\t" + serviceBean + ".add(bean);");
            bw.newLine();
            bw.write("\t\treturn getSuccessResponseVO(null);");
            bw.newLine();
            bw.write("\t}");
            bw.newLine();

            //批量新增的方法
            bw = BuildComment.buildMethodComment(bw, "批量新增");
            bw.newLine();
            bw.write("\t@RequestMapping(\"/addBatch\")");
            bw.newLine();
            bw.write("\tpublic ResponseVO addBatch(@RequestBody List<" + tableInfo.getBeanName() + "> listBean) {");
            bw.newLine();
            bw.write("\t\t" + serviceBean + ".addBatch(listBean);");
            bw.newLine();
            bw.write("\t\treturn getSuccessResponseVO(null);");
            bw.newLine();
            bw.write("\t}");
            bw.newLine();

            //批量新增的方法
            bw = BuildComment.buildMethodComment(bw, "批量新增/修改");
            bw.newLine();
            bw.write("\t@RequestMapping(\"/addOrUpdateBatch\")");
            bw.newLine();
            bw.write("\tpublic ResponseVO addOrUpdateBatch(@RequestBody List<" + tableInfo.getBeanName() + "> listBean) {");
            bw.newLine();
            bw.write("\t\t" + serviceBean + ".addBatch(listBean);");
            bw.newLine();
            bw.write("\t\treturn getSuccessResponseVO(null);");
            bw.newLine();
            bw.write("\t}");
            bw.newLine();

            Map<String, List<FieldInfo>> keyMap = tableInfo.getKeyIndexMap();
            for (Map.Entry<String, List<FieldInfo>> entry : keyMap.entrySet()) {
                List<FieldInfo> keyfieldInfoList = entry.getValue();
                StringBuffer paramStr = new StringBuffer();
                StringBuffer paramNameStr = new StringBuffer();
                StringBuffer methodName = new StringBuffer();
                int index = 0;
                for (FieldInfo column : keyfieldInfoList) {
                    if (index > 0) {
                        paramStr.append(",");
                        paramNameStr.append(",");
                        methodName.append("And");
                    }
                    paramStr.append(column.getJavaType() + " " + column.getPropertyName() + "");
                    paramNameStr.append(column.getPropertyName());
                    methodName.append(StringTools.upperCaseFirstLetter(column.getPropertyName()));
                    index++;
                }
                if (paramStr.length() > 0) {
                    //根据主键查询
                    BuildComment.buildMethodComment(bw, "根据" + methodName + "查询对象");
                    bw.newLine();
                    String methodNameStr = "get" + tableInfo.getBeanName() + "By" + methodName.toString();
                    bw.write("\t@RequestMapping(\"/" + methodNameStr + "\")");
                    bw.newLine();
                    bw.write("\tpublic ResponseVO " + methodNameStr + "(" + paramStr.toString() + ") {");
                    bw.newLine();
                    bw.write("\t\treturn getSuccessResponseVO(" + serviceBean + "." + methodNameStr + "(" + paramNameStr + "));");
                    bw.newLine();
                    bw.write("\t}");
                    bw.newLine();


                    BuildComment.buildMethodComment(bw, "根据" + methodName + "修改对象");
                    bw.newLine();
                    methodNameStr = "update" + tableInfo.getBeanName() + "By" + methodName.toString();
                    bw.write("\t@RequestMapping(\"/" + methodNameStr + "\")");
                    bw.newLine();
                    bw.write("\tpublic ResponseVO " + methodNameStr + "(" + tableInfo.getBeanName() + " bean,"
                            + paramStr.toString() + ") {");
                    bw.newLine();
                    bw.write("\t\t" + serviceBean + "." + methodNameStr + "(bean," + paramNameStr + ");");
                    bw.newLine();
                    bw.write("\t\treturn getSuccessResponseVO(null);");
                    bw.newLine();
                    bw.write("\t}");
                    bw.newLine();

                    //根据主键删除
                    BuildComment.buildMethodComment(bw, "根据" + methodName + "删除");
                    bw.newLine();
                    methodNameStr = "delete" + tableInfo.getBeanName() + "By" + methodName.toString();
                    bw.write("\t@RequestMapping(\"/" + methodNameStr + "\")");
                    bw.newLine();
                    bw.write("\tpublic ResponseVO " + methodNameStr + "(" + paramStr.toString() + ") {");
                    bw.newLine();
                    bw.write("\t\t" + serviceBean + "." + methodNameStr + "(" + paramNameStr + ");");
                    bw.newLine();
                    bw.write("\t\treturn getSuccessResponseVO(null);");
                    bw.newLine();
                    bw.write("\t}");
                    bw.newLine();

                }
            }
            bw.write("}");
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
