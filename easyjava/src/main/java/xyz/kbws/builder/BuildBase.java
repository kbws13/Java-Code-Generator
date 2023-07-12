package xyz.kbws.builder;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import xyz.kbws.bean.Constants;

import java.io.*;
import java.util.ArrayList;
import java.util.List;

/**
 * @author hsy
 * @date 2023/6/27
 */
public class BuildBase {
    private static Logger logger = LoggerFactory.getLogger(BuildBase.class);
    public static void execute(){
        List<String> headerInfoList = new ArrayList<String>();
        //生成Date枚举
        headerInfoList.add("package " + Constants.PACKAGE_ENUMS);
        build(headerInfoList, "DateTimePatternEnum", Constants.PATH_ENUMS);

        headerInfoList.clear();
        headerInfoList.add("package "+ Constants.PACKAGE_UTILS);
        build(headerInfoList, "DateUtils", Constants.PATH_UTILS);

        //生成BaseMapper
        headerInfoList.clear();
        headerInfoList.add("package "+ Constants.PACKAGE_MAPPER);
        build(headerInfoList, "BaseMapper", Constants.PATH_MAPPER);

        //生成PageSize枚举
        headerInfoList.clear();
        headerInfoList.add("package "+ Constants.PACKAGE_ENUMS);
        build(headerInfoList, "PageSize", Constants.PATH_ENUMS);

        //生成SimplePage
        headerInfoList.clear();
        headerInfoList.add("package "+ Constants.PACKAGE_QUERY);
        headerInfoList.add("import "+ Constants.PACKAGE_ENUMS + ".PageSize");
        build(headerInfoList, "SimplePage", Constants.PATH_QUERY);

        //生成BaseQuery
        headerInfoList.clear();
        headerInfoList.add("package "+ Constants.PACKAGE_QUERY);
        build(headerInfoList, "BaseQuery", Constants.PATH_QUERY);

        //生成PaginationResultVO
        headerInfoList.clear();
        headerInfoList.add("package "+ Constants.PACKAGE_VO);
        build(headerInfoList, "PaginationResultVO", Constants.PATH_VO);

        //生成返回结果枚举
        headerInfoList.clear();
        headerInfoList.add("package "+ Constants.PACKAGE_ENUMS);
        build(headerInfoList, "ResponseCodeEnum", Constants.PATH_ENUMS);

        //生成异常枚举
        headerInfoList.clear();
        headerInfoList.add("package "+ Constants.PACKAGE_EXCEPTION);
        headerInfoList.add("import "+ Constants.PACKAGE_ENUMS+".ResponseCodeEnum");
        build(headerInfoList, "BusinessException", Constants.PATH_EXCEPTION);

        //生成BaseController
        headerInfoList.clear();
        headerInfoList.add("package "+ Constants.PACKAGE_CONTROLLER);
        headerInfoList.add("import "+ Constants.PACKAGE_ENUMS+".ResponseCodeEnum");
        headerInfoList.add("import "+ Constants.PACKAGE_VO+".ResponseVO;");
        build(headerInfoList, "ABaseController", Constants.PATH_CONTROLLER);

        //生成ResponseVO
        headerInfoList.clear();
        headerInfoList.add("package "+ Constants.PACKAGE_VO);
        build(headerInfoList, "ResponseVO", Constants.PATH_VO);

        //生成AGlobalExceptionHandlerController
        headerInfoList.clear();
        headerInfoList.add("package "+ Constants.PACKAGE_CONTROLLER);
        headerInfoList.add("import "+ Constants.PACKAGE_ENUMS+".ResponseCodeEnum");
        headerInfoList.add("import "+ Constants.PACKAGE_VO+".ResponseVO");
        headerInfoList.add("import "+ Constants.PACKAGE_EXCEPTION+".BusinessException");
        build(headerInfoList, "AGlobalExceptionHandlerController", Constants.PATH_CONTROLLER);

        //构建启动类
        headerInfoList.clear();
        headerInfoList.add("package "+Constants.PACKAGE_BASE);
        build(headerInfoList, "RunApplication", Constants.PATH_MAIN);
    }

    public static void build(List<String>headerInfoList, String fileName, String outPutPath){
        File folder = new File(outPutPath);
        if (!folder.exists()){
            folder.mkdirs();
        }

        File javaFile = new File(outPutPath, fileName + ".java");

        OutputStream out = null;
        OutputStreamWriter outWriter = null;
        BufferedWriter bw = null;

        InputStream in = null;
        InputStreamReader inReader = null;
        BufferedReader bf = null;
        try {
            out = new FileOutputStream(javaFile);
            outWriter = new OutputStreamWriter(out, "utf-8");
            bw = new BufferedWriter(outWriter);

            String templatePath = BuildBase.class.getClassLoader().getResource("template/" + fileName + ".txt").getPath();
            in = new FileInputStream(templatePath);
            inReader = new InputStreamReader(in, "utf-8");
            bf = new BufferedReader(inReader);

            for (String head:headerInfoList){
                bw.write(head+";");
                bw.newLine();
                bw.newLine();
            }

            String lineInfo = null;
            while ((lineInfo = bf.readLine()) != null){
                bw.write(lineInfo);
                bw.newLine();
            }
            bw.flush();

        }catch (Exception e){
            logger.error("生成基础类:{},失败", fileName, e);
        }finally {
            try {
                if (bf!=null){
                    bf.close();
                }
                if (inReader!=null){
                    inReader.close();
                }
                if (in!=null){
                    in.close();
                }
                if (bw!=null){
                    bw.close();
                }
                if (outWriter!=null){
                    outWriter.close();
                }
                if (out!=null){
                    out.close();
                }
            }catch (IOException e){
                e.printStackTrace();
            }
        }
    }
}
