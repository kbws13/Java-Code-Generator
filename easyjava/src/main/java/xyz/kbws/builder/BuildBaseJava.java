package xyz.kbws.builder;

import xyz.kbws.bean.Constants;
import xyz.kbws.utils.PropertiesUtils;

import java.io.*;
import java.util.ArrayList;
import java.util.List;

public class BuildBaseJava {

    public static void execute() {

        //构建ResponseCodeEnum类
        List<String> headInfoList = new ArrayList();
        headInfoList.add("package " + Constants.PACKAGE_ENUMS);
        build(headInfoList, "ResponseCodeEnum", Constants.PATH_ENUMS);

        //构建 DateUtils类
        headInfoList.clear();
        headInfoList.add("package " + Constants.PACKAGE_UTILS);
        build(headInfoList, "DateUtil", Constants.PATH_UTILS);

        //构建日期枚举
        headInfoList.clear();
        headInfoList.add("package " + Constants.PACKAGE_ENUMS);
        build(headInfoList, "DateTimePatternEnum", Constants.PATH_ENUMS);

        //构建分页枚举
        headInfoList.clear();
        headInfoList.add("package " + Constants.PACKAGE_ENUMS);
        build(headInfoList, "PageSize", Constants.PATH_ENUMS);


        //构建分页类
        headInfoList.clear();
        headInfoList.add("package " + Constants.PACKAGE_PARAM);
        headInfoList.add("import " + Constants.PACKAGE_ENUMS + ".PageSize");
        build(headInfoList, "SimplePage", Constants.PATH_PARAM);

        //构建分类对象类
        headInfoList.clear();
        headInfoList.add("package " + Constants.PACKAGE_VO);
        headInfoList.add("import java.util.ArrayList");
        headInfoList.add("import java.util.List");
        build(headInfoList, "PaginationResultVO", Constants.PATH_VO);

        //构建查询类父类
        headInfoList.clear();
        headInfoList.add("package " + Constants.PACKAGE_PARAM);
        build(headInfoList, "BaseParam", Constants.PATH_PARAM);

        //构建baseMapper
        headInfoList.clear();
        headInfoList.add("package " + Constants.PACKAGE_MAPPER);
        build(headInfoList, "BaseMapper", Constants.PATH_MAPPER);


        //构建BusinessException类
        headInfoList.clear();
        headInfoList.add("package " + Constants.PACKAGE_EXCEPTION);
        headInfoList.add("import " + Constants.PACKAGE_ENUMS + ".ResponseCodeEnum");
        build(headInfoList, "BusinessException", Constants.PATH_EXCEPTION);

        //构建ResponseVO类
        headInfoList.clear();
        headInfoList.add("package " + Constants.PACKAGE_VO);
        build(headInfoList, "ResponseVO", Constants.PATH_VO);

        //构建ABaseController类
        headInfoList.clear();
        headInfoList.add("package " + Constants.PACKAGE_CONTROLLER);
        headInfoList.add("import " + Constants.PACKAGE_ENUMS + ".ResponseCodeEnum");
        headInfoList.add("import " + Constants.PACKAGE_VO + ".ResponseVO");
        headInfoList.add("import " + Constants.PACKAGE_EXCEPTION + ".BusinessException");
        build(headInfoList, "ABaseController", Constants.PATH_CONTROLLER);

        //构建全局错误拦截
        headInfoList.clear();
        headInfoList.add("package " + Constants.PACKAGE_CONTROLLER);
        headInfoList.add("import " + Constants.PACKAGE_ENUMS + ".ResponseCodeEnum");
        headInfoList.add("import " + Constants.PACKAGE_VO + ".ResponseVO");
        headInfoList.add("import " + Constants.PACKAGE_EXCEPTION + ".BusinessException");
        build(headInfoList, "AGlobalExceptionHandlerController", Constants.PATH_CONTROLLER);

        //构建工具类
        headInfoList.clear();
        headInfoList.add("package " + Constants.PACKAGE_UTILS);
        headInfoList.add("import " + Constants.PACKAGE_EXCEPTION + ".BusinessException");
        headInfoList.add("import java.lang.reflect.Field");
        headInfoList.add("import java.lang.reflect.Method");
        build(headInfoList, "StringTools", Constants.PATH_UTILS);


    }

    private static void build(List<String> headInfoList, String fileName, String outPutPath) {
        File folder = new File(outPutPath);
        if (!folder.exists()) {
            folder.mkdirs();
        }
        File beanFile = new File(outPutPath, fileName + ".java");
        OutputStream out = null;
        OutputStreamWriter outw = null;
        BufferedWriter bw = null;

        InputStream in = null;
        InputStreamReader inr = null;
        BufferedReader bf = null;
        try {
            out = new FileOutputStream(beanFile);
            outw = new OutputStreamWriter(out, "utf-8");
            bw = new BufferedWriter(outw);
            in = PropertiesUtils.class.getClassLoader().getResourceAsStream("template/" + fileName + ".txt");
            inr = new InputStreamReader(in, "utf-8");
            bf = new BufferedReader(inr);

            //输出头部信息，比如包名，导入如的包
            for (String str : headInfoList) {
                bw.write(str + ";");
                bw.newLine();
            }

            bw.newLine();
            bw.newLine();
            String lineInfo = null;
            while ((lineInfo = bf.readLine()) != null) {
                bw.write(lineInfo);
                bw.newLine();
            }
            bw.flush();
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            if (null != in) {
                try {
                    in.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
            if (inr != null) {
                try {
                    inr.close();
                } catch (IOException e) {
                    e.printStackTrace();

                }
            }
            if (null != bf) {
                try {
                    bf.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }

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
