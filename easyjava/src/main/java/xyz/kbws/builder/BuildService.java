package xyz.kbws.builder;


import xyz.kbws.bean.Constants;
import xyz.kbws.bean.FieldInfo;
import xyz.kbws.bean.TableInfo;
import xyz.kbws.builder.BuildComment;
import xyz.kbws.utils.StringTools;

import java.io.*;
import java.util.List;
import java.util.Map;

public class BuildService {

    public static void execute(TableInfo tableInfo) {

        File folder = new File(Constants.PATH_SERVICE);
        if (!folder.exists()) {
            folder.mkdirs();
        }
        File beanFile = new File(Constants.PATH_SERVICE, tableInfo.getBeanName() + Constants.SUFFIX_SERVICE + ".java");
        OutputStream out = null;
        OutputStreamWriter outw = null;
        BufferedWriter bw = null;

        try {
            out = new FileOutputStream(beanFile);
            outw = new OutputStreamWriter(out, "utf-8");
            bw = new BufferedWriter(outw);
            bw.write("package " + Constants.PACKAGE_SERVICE + ";");
            bw.newLine();
            bw.newLine();
            bw.write("import java.util.List;");
            bw.newLine();
            bw.newLine();
            bw.write("import " + Constants.PACKAGE_PARAM + "." + tableInfo.getBeanParamName() + ";");
            bw.newLine();
            bw.write("import " + Constants.PACKAGE_BEAN + "." + tableInfo.getBeanName() + ";");
            bw.newLine();
            bw.write("import " + Constants.PACKAGE_VO + ".PaginationResultVO;");
            bw.newLine();

            //所有属性
            List<FieldInfo> fieldInfoList = tableInfo.getFieldList();
            bw = BuildComment.buildClassComment(bw, tableInfo.getComment() + " 业务接口");
            bw.newLine();
            bw.write("public interface " + tableInfo.getBeanName() + Constants.SUFFIX_SERVICE + " {");
            bw.newLine();

            String beanName = tableInfo.getBeanName();

            //根据条件查询列表
            bw = BuildComment.buildMethodComment(bw, "根据条件查询列表");
            bw.newLine();
            bw.write("\tList<" + beanName + "> findListByParam(" + tableInfo.getBeanParamName() + " param);");
            bw.newLine();
            //根据条件查询数量
            bw = BuildComment.buildMethodComment(bw, "根据条件查询列表");
            bw.newLine();
            bw.write("\tInteger findCountByParam(" + tableInfo.getBeanParamName() + " param);");
            bw.newLine();
            //分页查询的方法
            bw = BuildComment.buildMethodComment(bw, "分页查询");
            bw.newLine();
            bw.write("\tPaginationResultVO<" + beanName + "> findListByPage(" + tableInfo.getBeanParamName()
                    + " param);");
            bw.newLine();


            Map<String, List<FieldInfo>> keyMap = tableInfo.getKeyIndexMap();
            //新增的方法
            bw = BuildComment.buildMethodComment(bw, "新增");
            bw.newLine();
            bw.write("\tInteger add(" + tableInfo.getBeanName() + " bean);");
            bw.newLine();
            //批量新增的方法
            bw = BuildComment.buildMethodComment(bw, "批量新增");
            bw.newLine();
            bw.write("\tInteger addBatch(List<" + tableInfo.getBeanName() + "> listBean);");
            bw.newLine();

            bw = BuildComment.buildMethodComment(bw, "批量新增/修改");
            bw.newLine();
            bw.write("\tInteger addOrUpdateBatch(List<" + tableInfo.getBeanName() + "> listBean);");
            bw.newLine();

            bw = BuildComment.buildMethodComment(bw, "多条件更新");
            bw.newLine();
            bw.write("\tInteger updateByParam(" + tableInfo.getBeanName() + " bean," + tableInfo.getBeanParamName() + " param);");
            bw.newLine();

            bw = BuildComment.buildMethodComment(bw, "多条件删除");
            bw.newLine();
            bw.write("\tInteger deleteByParam(" + tableInfo.getBeanParamName() + " param);");
            bw.newLine();

            for (Map.Entry<String, List<FieldInfo>> entry : keyMap.entrySet()) {
                List<FieldInfo> keyfieldInfoList = entry.getValue();
                StringBuffer paramStr = new StringBuffer();
                StringBuffer methodName = new StringBuffer();
                int index = 0;
                for (FieldInfo column : keyfieldInfoList) {
                    if (index > 0) {
                        paramStr.append(",");
                        methodName.append("And");
                    }
                    paramStr.append(column.getJavaType() + " " + column.getPropertyName() + "");
                    methodName.append(StringTools.upperCaseFirstLetter(column.getPropertyName()));
                    index++;
                }
                if (paramStr.length() > 0) {
                    //根据主键查询
                    BuildComment.buildMethodComment(bw, "根据" + methodName + "查询对象");
                    bw.newLine();
                    bw.write("\t" + tableInfo.getBeanName() + " get" + tableInfo.getBeanName() + "By" + methodName.toString() + "("
                            + paramStr.toString() + ");");
                    bw.newLine();
                    bw.newLine();

                    //根据主键方法
                    bw = BuildComment.buildMethodComment(bw, "根据" + methodName + "修改");
                    bw.newLine();
                    bw.write("\tInteger update" + tableInfo.getBeanName() + "By" + methodName.toString() + "(" + tableInfo.getBeanName() + " bean,"
                            + paramStr.toString() + ");");
                    bw.newLine();
                    bw.newLine();
                    //根据主键删除
                    bw = BuildComment.buildMethodComment(bw, "根据" + methodName + "删除");
                    bw.newLine();
                    bw.write("\tInteger delete" + tableInfo.getBeanName() + "By" + methodName.toString() + "(" + paramStr.toString() + ");");
                    bw.newLine();
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
