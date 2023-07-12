package xyz.kbws.builder;

import xyz.kbws.bean.Constants;
import xyz.kbws.utils.DateUtils;

import java.io.BufferedWriter;
import java.text.SimpleDateFormat;
import java.util.Date;

/**
 * @author hsy
 * @date 2023/6/26
 */
public class BuildComment {
    public static void createClassComment(BufferedWriter bw, String classComment) throws Exception{
        bw.write("/**");
        bw.newLine();
        bw.write(" * @Description " + classComment);
        bw.newLine();
        bw.write(" * @author " + Constants.AUTHOR);
        bw.newLine();
        bw.write(" * @Date " + DateUtils.format(new Date(), DateUtils._YYYYMMDD));
        bw.newLine();
        bw.write(" */");
        bw.newLine();
    }

    public static void createFieldComment(BufferedWriter bw, String fieldComment) throws Exception{
        bw.write("\t/**");
        bw.newLine();
        bw.write("\t * " + (fieldComment == null ? "" : fieldComment));
        bw.newLine();
        bw.write("\t */");
        bw.newLine();
    }

    public static void createMethodComment(){

    }
}
