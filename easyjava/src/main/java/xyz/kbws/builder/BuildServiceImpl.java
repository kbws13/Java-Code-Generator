package xyz.kbws.builder;


import xyz.kbws.bean.Constants;
import xyz.kbws.bean.FieldInfo;
import xyz.kbws.bean.TableInfo;
import xyz.kbws.builder.BuildComment;
import xyz.kbws.utils.StringTools;

import java.io.*;
import java.util.List;
import java.util.Map;

public class BuildServiceImpl {

    public static void execute(TableInfo tableInfo) {

        File folder = new File(Constants.PATH_SERVICE_IMPL);
        if (!folder.exists()) {
            folder.mkdirs();
        }
        File beanFile = new File(Constants.PATH_SERVICE_IMPL,
                tableInfo.getBeanName() + Constants.SUFFIX_SERVICE_IMPL + ".java");
        OutputStream out = null;
        OutputStreamWriter outw = null;
        BufferedWriter bw = null;

        try {
            out = new FileOutputStream(beanFile);
            outw = new OutputStreamWriter(out, "utf-8");
            bw = new BufferedWriter(outw);
            bw.write("package " + Constants.PACKAGE_SERVICE_IMPL + ";");
            bw.newLine();
            bw.newLine();
            bw.write("import java.util.List;");
            bw.newLine();
            bw.newLine();
            bw.write("import javax.annotation.Resource;");
            bw.newLine();
            bw.newLine();
            bw.write("import org.springframework.stereotype.Service;");
            bw.newLine();
            bw.newLine();
            bw.write("import " + Constants.PACKAGE_ENUMS + ".PageSize;");
            bw.newLine();
            bw.write("import " + Constants.PACKAGE_PARAM + "." + tableInfo.getBeanParamName() + ";");
            bw.newLine();
            bw.write("import " + Constants.PACKAGE_BEAN + "." + tableInfo.getBeanName() + ";");
            bw.newLine();
            bw.write("import " + Constants.PACKAGE_VO + ".PaginationResultVO;");
            bw.newLine();
            bw.write("import " + Constants.PACKAGE_PARAM + ".SimplePage;");

            bw.newLine();
            bw.write("import " + Constants.PACKAGE_MAPPER + "." + tableInfo.getBeanName() + Constants.SUFFIX_MAPPER
                    + ";");
            bw.newLine();
            bw.write("import " + Constants.PACKAGE_SERVICE + "." + tableInfo.getBeanName() + Constants.SUFFIX_SERVICE
                    + ";");
            bw.newLine();
            bw.write("import " + Constants.PACKAGE_UTILS + ".StringTools;");
            bw.newLine();

            bw = BuildComment.buildClassComment(bw, tableInfo.getComment() + " 业务接口实现");
            bw.newLine();
            String anServiceBean = tableInfo.getBeanName() + Constants.SUFFIX_SERVICE;
            anServiceBean = anServiceBean.substring(0, 1).toLowerCase() + anServiceBean.substring(1);
            bw.write("@Service(\"" + anServiceBean + "\")");
            bw.newLine();
            bw.write("public class " + tableInfo.getBeanName() + Constants.SUFFIX_SERVICE_IMPL + " implements "
                    + tableInfo.getBeanName() + Constants.SUFFIX_SERVICE + " {");
            bw.newLine();
            bw.newLine();
            bw.write("\t@Resource");
            bw.newLine();

            String beanName = tableInfo.getBeanName();
            String paramMapper = beanName + Constants.SUFFIX_MAPPER;
            paramMapper = paramMapper.substring(0, 1).toLowerCase() + paramMapper.substring(1);
            bw.write("\tprivate " + tableInfo.getBeanName() + Constants.SUFFIX_MAPPER + "<" + tableInfo.getBeanName()
                    + ", " + tableInfo.getBeanParamName() + "> " + paramMapper + ";");
            bw.newLine();

            //根据条件查询列表
            bw = BuildComment.buildMethodComment(bw, "根据条件查询列表");
            bw.newLine();
            bw.write("\t@Override");
            bw.newLine();
            bw.write("\tpublic List<" + beanName + "> findListByParam(" + tableInfo.getBeanParamName() + " param) {");
            bw.newLine();
            bw.write("\t\treturn this." + paramMapper + ".selectList(param);");
            bw.newLine();
            bw.write("\t}");
            bw.newLine();

            //根据条件查询记录数
            bw = BuildComment.buildMethodComment(bw, "根据条件查询列表");
            bw.newLine();
            bw.write("\t@Override");
            bw.newLine();
            bw.write("\tpublic Integer findCountByParam(" + tableInfo.getBeanParamName() + " param) {");
            bw.newLine();
            bw.write("\t\treturn this." + paramMapper + ".selectCount(param);");
            bw.newLine();
            bw.write("\t}");
            bw.newLine();

            //分页查询的方法
            bw = BuildComment.buildMethodComment(bw, "分页查询方法");
            bw.newLine();
            bw.write("\t@Override");
            bw.newLine();
            bw.write("\tpublic PaginationResultVO<" + beanName + "> findListByPage(" + tableInfo.getBeanParamName()
                    + " param) {");
            bw.newLine();
            bw.write("\t\tint count = this.findCountByParam(param);");
            bw.newLine();
            bw.write("\t\tint pageSize = param.getPageSize() == null ? PageSize.SIZE15.getSize() : param.getPageSize();");
            bw.newLine();
            bw.newLine();
            bw.write("\t\tSimplePage page = new SimplePage(param.getPageNo(), count, pageSize);");
            bw.newLine();
            bw.write("\t\tparam.setSimplePage(page);");
            bw.newLine();
            bw.write("\t\tList<" + beanName + "> list = this.findListByParam(param);");
            bw.newLine();
            bw.write("\t\tPaginationResultVO<" + beanName + "> result = new PaginationResultVO(count, page.getPageSize(), page.getPageNo(), page.getPageTotal(), list);");
            bw.newLine();
            bw.write("\t\treturn result;");
            bw.newLine();
            bw.write("\t}");
            bw.newLine();

            //新增
            bw = BuildComment.buildMethodComment(bw, "新增");
            bw.newLine();
            bw.write("\t@Override");
            bw.newLine();
            bw.write("\tpublic Integer add(" + tableInfo.getBeanName() + " bean) {");
            bw.newLine();
            bw.write("\t\treturn this." + paramMapper + ".insert(bean);");
            bw.newLine();
            bw.write("\t}");
            bw.newLine();

            //批量新增
            bw = BuildComment.buildMethodComment(bw, "批量新增");
            bw.newLine();
            bw.write("\t@Override");
            bw.newLine();
            bw.write("\tpublic Integer addBatch(List<" + tableInfo.getBeanName() + "> listBean) {");
            bw.newLine();
            bw.write("\t\tif (listBean == null || listBean.isEmpty()) {");
            bw.newLine();
            bw.write("\t\t\treturn 0;");
            bw.newLine();
            bw.write("\t\t}");
            bw.newLine();
            bw.write("\t\treturn this." + paramMapper + ".insertBatch(listBean);");

            bw.newLine();
            bw.write("\t}");
            bw.newLine();

            //批量新增
            bw = BuildComment.buildMethodComment(bw, "批量新增或者修改");
            bw.newLine();
            bw.write("\t@Override");
            bw.newLine();
            bw.write("\tpublic Integer addOrUpdateBatch(List<" + tableInfo.getBeanName() + "> listBean) {");
            bw.newLine();
            bw.write("\t\tif (listBean == null || listBean.isEmpty()) {");
            bw.newLine();
            bw.write("\t\t\treturn 0;");
            bw.newLine();
            bw.write("\t\t}");
            bw.newLine();
            bw.write("\t\treturn this." + paramMapper + ".insertOrUpdateBatch(listBean);");
            bw.newLine();
            bw.write("\t}");
            bw.newLine();

            //多条件更新
            bw = BuildComment.buildMethodComment(bw, "多条件更新");
            bw.newLine();
            bw.write("\t@Override");
            bw.newLine();
            bw.write("\tpublic Integer updateByParam(" + tableInfo.getBeanName() + " bean, " + tableInfo.getBeanParamName() + " param) {");
            bw.newLine();
            bw.write("\t\tStringTools.checkParam(param);");
            bw.newLine();
            bw.write("\t\treturn this." + paramMapper + ".updateByParam(bean, param);");
            bw.newLine();
            bw.write("\t}");
            bw.newLine();

            //多条件删除
            bw = BuildComment.buildMethodComment(bw, "多条件删除");
            bw.newLine();
            bw.write("\t@Override");
            bw.newLine();
            bw.write("\tpublic Integer deleteByParam(" + tableInfo.getBeanParamName() + " param) {");
            bw.newLine();
            bw.write("\t\tStringTools.checkParam(param);");
            bw.newLine();
            bw.write("\t\treturn this." + paramMapper + ".deleteByParam(param);");
            bw.newLine();
            bw.write("\t}");
            bw.newLine();

            Map<String, List<FieldInfo>> keyMap = tableInfo.getKeyIndexMap();
            for (Map.Entry<String, List<FieldInfo>> entry : keyMap.entrySet()) {
                List<FieldInfo> keyColumnList = entry.getValue();
                StringBuffer paramStr = new StringBuffer();
                StringBuffer paramValueStr = new StringBuffer();
                StringBuffer methodName = new StringBuffer();
                int index = 0;
                for (FieldInfo column : keyColumnList) {
                    if (index > 0) {
                        paramStr.append(", ");
                        methodName.append("And");
                        paramValueStr.append(", ");
                    }
                    paramStr.append(column.getJavaType() + " " + column.getPropertyName());
                    paramValueStr.append(column.getPropertyName());
                    methodName.append(StringTools.upperCaseFirstLetter(column.getPropertyName()));
                    index++;
                }
                if (paramStr.length() > 0) {
                    //根据主键查询
                    BuildComment.buildMethodComment(bw, "根据" + methodName + "获取对象");
                    bw.newLine();
                    bw.write("\t@Override");
                    bw.newLine();
                    bw.write("\tpublic " + tableInfo.getBeanName() + " get" + tableInfo.getBeanName() + "By" + methodName.toString() + "("
                            + paramStr.toString() + ") {");
                    bw.newLine();
                    bw.write("\t\treturn this." + paramMapper + ".selectBy" + methodName.toString() + "(" + paramValueStr.toString() + ");");

                    bw.newLine();
                    bw.write("\t}");
                    bw.newLine();


                    //根据主键修改
                    bw = BuildComment.buildMethodComment(bw, "根据" + methodName + "修改");
                    bw.newLine();
                    bw.write("\t@Override");
                    bw.newLine();
                    bw.write("\tpublic Integer update" + tableInfo.getBeanName() + "By" + methodName.toString() + "(" + tableInfo.getBeanName() + " bean, "
                            + paramStr.toString() + ") {");
                    bw.newLine();
                    bw.write("\t\treturn this." + paramMapper + ".updateBy" + methodName.toString() + "(bean, " + paramValueStr.toString() + ");");


                    bw.newLine();
                    bw.write("\t}");
                    bw.newLine();


                    //根据主键删除
                    bw = BuildComment.buildMethodComment(bw, "根据" + methodName + "删除");
                    bw.newLine();
                    bw.write("\t@Override");
                    bw.newLine();
                    bw.write("\tpublic Integer delete" + tableInfo.getBeanName() + "By" + methodName.toString() + "(" + paramStr.toString() + ") {");
                    bw.newLine();
                    bw.write("\t\treturn this." + paramMapper + ".deleteBy" + methodName.toString() + "(" + paramValueStr.toString() + ");");

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
