package xyz.kbws.builder;

import xyz.kbws.bean.Constants;
import xyz.kbws.bean.FieldInfo;
import xyz.kbws.bean.TableInfo;
import xyz.kbws.utils.StringTools;

import java.io.*;
import java.util.List;
import java.util.Map;

public class BuildMapperXml {

    private static final String BASE_RESULT_MAP = "base_result_map";

    private static final String BASE_COLUMN_LIST = "base_column_list";

    private static final String BASE_CONDITION = "base_condition";

    private static final String QUERY_CONDITION = "query_condition";

    private static final String BASE_CONDITION_Filed = "base_condition_filed";

    public static void execute(TableInfo tableInfo) {

        File folder = new File(Constants.PATH_MAPPER_XML);
        if (!folder.exists()) {
            folder.mkdirs();
        }
        File beanFile = new File(Constants.PATH_MAPPER_XML, tableInfo.getBeanName() + Constants.SUFFIX_MAPPER_XML + ".xml");
        OutputStream out = null;
        OutputStreamWriter outw = null;
        BufferedWriter bw = null;
        try {
            List<FieldInfo> fieldInfoList = tableInfo.getFieldList();

            out = new FileOutputStream(beanFile);
            outw = new OutputStreamWriter(out, "utf-8");
            bw = new BufferedWriter(outw);
            bw.write("<?xml version=\"1.0\" encoding=\"UTF-8\"?>");
            bw.newLine();
            bw.write("<!DOCTYPE mapper PUBLIC \"-//mybatis.org//DTD Mapper 3.0//EN\" ");
            bw.newLine();
            bw.write("    \"http://mybatis.org/dtd/mybatis-3-mapper.dtd\">");
            bw.newLine();
            bw.write("<mapper namespace=\"" + Constants.PACKAGE_MAPPER + "." + tableInfo.getBeanName() + Constants.SUFFIX_MAPPER_XML + "\">");
            bw.newLine();
            bw.newLine();
            //实体映射
            bw.write("\t<!--实体映射-->");
            bw.newLine();
            bw.write("\t<resultMap id=\"" + BASE_RESULT_MAP + "\" type=\"" + Constants.PACKAGE_BEAN + "." + tableInfo.getBeanName() + "\">");
            bw.newLine();
            for (FieldInfo FieldInfo : fieldInfoList) {
                bw.write("\t\t<!--" + FieldInfo.getComment() + "-->");
                bw.newLine();
                //主键
                if (FieldInfo.getAutoIncrement() != null && FieldInfo.getAutoIncrement()) {
                    bw.write("\t\t<id column=\"" + FieldInfo.getFieldName() + "\" property=\"" + FieldInfo.getPropertyName() + "\"  />");
                } else {
                    bw.write("\t\t<result column=\"" + FieldInfo.getFieldName() + "\" property=\"" + FieldInfo.getPropertyName() + "\"  />");
                }
                bw.newLine();
            }
            bw.write("\t</resultMap>");
            bw.newLine();
            bw.newLine();
            bw.newLine();

            //通用结果集
            bw.write("\t<!-- 通用查询结果列-->");
            bw.newLine();
            bw.write("\t<sql id=\"" + BASE_COLUMN_LIST + "\">");
            bw.newLine();
            bw.write("\t\t ");
            for (int i = 0, _len = fieldInfoList.size(); i < _len; i++) {
                bw.write(fieldInfoList.get(i).getFieldName());
                if (i != _len - 1) {
                    bw.write(",");
                }
                if ((i + 1) % 5 == 0) {
                    bw.newLine();
                    bw.write("\t\t ");
                }
            }
            bw.newLine();
            bw.write("\t</sql>");
            bw.newLine();
            bw.newLine();

            //基本查询列
            bw.write("\t<sql id=\"" + BASE_CONDITION_Filed + "\">");
            bw.newLine();
            for (FieldInfo FieldInfo : fieldInfoList) {
                bw.write("\t\t\t<if test=\"query." + FieldInfo.getPropertyName() + " != null");
                if (Constants.TYPE_STRING.equals(FieldInfo.getJavaType()) || Constants.TYPE_DATE.equals(FieldInfo.getJavaType())) {
                    bw.write(" and query." + FieldInfo.getPropertyName() + "!=''");
                }
                bw.write("\">");
                bw.newLine();
                if (Constants.TYPE_DATE.equals(FieldInfo.getJavaType())) {
                    bw.write("\t\t\t\t <![CDATA[ and  " + FieldInfo.getFieldName() + "=str_to_date(#{query." + FieldInfo.getPropertyName() + "}, '%Y-%m-%d') ]]>");
                } else {
                    bw.write("\t\t\t\t and  " + FieldInfo.getFieldName() + " = #{query." + FieldInfo.getPropertyName() + "}");
                }
                bw.newLine();
                bw.write("\t\t\t</if>");
                bw.newLine();
            }
            bw.write("\t</sql>");

            //生成where条件
            bw.newLine();
            bw.write("\t<!-- 通用条件列-->");
            bw.newLine();
            bw.write("\t<sql id=\"" + BASE_CONDITION + "\">");
            bw.newLine();
            bw.write("\t <where>");
            bw.newLine();
            bw.write("\t\t <include refid=\"" + BASE_CONDITION_Filed + "\" />");
            bw.newLine();
            bw.write("\t </where>");
            bw.newLine();
            bw.write("\t</sql>");
            bw.newLine();
            bw.newLine();

            bw.write("\t<!-- 通用查询条件列-->");
            bw.newLine();
            bw.write("\t<sql id=\"" + QUERY_CONDITION + "\">");
            bw.newLine();
            bw.write("\t <where>");
            bw.newLine();
            bw.write("\t\t\t<include refid=\"" + BASE_CONDITION_Filed + "\" />");
            bw.newLine();
            for (FieldInfo FieldInfo : fieldInfoList) {
                if (Constants.TYPE_STRING.equals(FieldInfo.getJavaType())) {
                    bw.write("\t\t\t<if test=\"query." + FieldInfo.getPropertyName() + Constants.SUFFIX_PROPERTY_FUZZY + "!= null  and " +
                            "query." + FieldInfo.getPropertyName() + Constants.SUFFIX_PROPERTY_FUZZY + "!=''\">");
                    bw.newLine();
                    bw.write("\t\t\t\t and  " + FieldInfo.getFieldName() + " like concat('%', #{query." + FieldInfo.getPropertyName() + Constants
                            .SUFFIX_PROPERTY_FUZZY + "}, '%')");
                    bw.newLine();
                    bw.write("\t\t\t</if>");
                    bw.newLine();
                }

                if (Constants.TYPE_DATE.equals(FieldInfo.getJavaType())) {
                    String start = FieldInfo.getPropertyName() + Constants.SUFFIX_BEAN_PARAM_TIME_START;
                    String end = FieldInfo.getPropertyName() + Constants.SUFFIX_BEAN_PARAM_TIME_END;
                    //开始时间
                    bw.write("\t\t\t<if test=\"query." + start + "!= null and query." + start + "!=''\">");
                    bw.newLine();
                    bw.write("\t\t\t\t <![CDATA[ and  " + FieldInfo.getFieldName() + ">=str_to_date(#{query." + start + "}, '%Y-%m-%d') ]]>");
                    bw.newLine();
                    bw.write("\t\t\t</if>");
                    bw.newLine();

                    //结束日期
                    bw.write("\t\t\t<if test=\"query." + end + "!= null and query." + end + "!=''\">");
                    bw.newLine();
                    bw.write("\t\t\t\t <![CDATA[ and  " + FieldInfo.getFieldName() + "< date_sub(str_to_date(#{query." + end + "},'%Y-%m-%d')," +
                            "interval -1 day) ]]>");
                    bw.newLine();
                    bw.write("\t\t\t</if>");
                    bw.newLine();
                }
            }
            bw.write("\t </where>");
            bw.newLine();
            bw.write("\t</sql>");
            bw.newLine();
            bw.newLine();

            // 查询对象方法
            bw.write("\t<!-- 查询集合-->");
            bw.newLine();
            bw.write("\t<select id=\"selectList\" resultMap=\"" + BASE_RESULT_MAP + "\" >");
            bw.newLine();
            bw.write("\t\t SELECT");
            bw.write(" <include refid=\"" + BASE_COLUMN_LIST + "\" />");
            bw.write(" FROM " + tableInfo.getTableName());
            bw.write(" <include refid=\"" + QUERY_CONDITION + "\" />");
            bw.newLine();
            bw.write("\t\t <if test=\"query.orderBy!=null\">");
            bw.newLine();
            bw.write("\t\t\t order by ${query.orderBy}");
            bw.newLine();
            bw.write("\t\t </if>");
            bw.newLine();
            bw.write("\t\t <if test=\"query.simplePage!=null\">");
            bw.newLine();
            bw.write("\t\t\t limit #{query.simplePage.start},#{query.simplePage.end}");
            bw.newLine();
            bw.write("\t\t </if>");
            bw.newLine();
            bw.write("\t</select>");
            bw.newLine();
            bw.newLine();

            // 查询方法完毕
            bw.write("\t<!-- 查询数量-->");
            bw.newLine();
            bw.write("\t<select id=\"selectCount\" resultType=\"java.lang.Integer\" >");
            bw.newLine();
            bw.write("\t\t SELECT");
            bw.write(" count(1)");
            bw.write(" FROM " + tableInfo.getTableName());
            bw.write(" <include refid=\"" + QUERY_CONDITION + "\" />");
            bw.newLine();
            bw.write("\t</select>");
            bw.newLine();
            bw.newLine();

            //insert方法（匹配有值的字段）

            //获取自增长的属性
            FieldInfo autoIncrementColumn = null;
            for (FieldInfo FieldInfo : fieldInfoList) {
                if (null != FieldInfo.getAutoIncrement() && FieldInfo.getAutoIncrement()) {
                    autoIncrementColumn = FieldInfo;
                    break;
                }
            }

            bw.write("\t<!-- 插入 （匹配有值的字段）-->");
            bw.newLine();
            bw.write("\t<insert id=\"insert\" parameterType=\"" + Constants.PACKAGE_BEAN + "." + tableInfo.getBeanName() + "\">");

            bw.newLine();
            if (autoIncrementColumn != null) {
                bw.write("\t\t<selectKey keyProperty=\"bean." + autoIncrementColumn.getPropertyName() + "\" resultType=\"" + autoIncrementColumn
                        .getJavaType() + "\" order=\"AFTER\">");
                bw.newLine();
                bw.write("\t\t\tSELECT LAST_INSERT_ID()");
                bw.newLine();
                bw.write("\t\t</selectKey>");
                bw.newLine();
            }
            bw.write("\t\t INSERT INTO " + tableInfo.getTableName());
            bw.newLine();
            bw.write("\t\t <trim prefix=\"(\" suffix=\")\" suffixOverrides=\",\" >");
            bw.newLine();
            for (FieldInfo FieldInfo : fieldInfoList) {
                if (FieldInfo.getAutoIncrement()) {
                    continue;
                }
                bw.write("\t\t\t<if test=\"bean." + FieldInfo.getPropertyName() + " != null\">");
                bw.newLine();
                bw.write("\t\t\t\t " + FieldInfo.getFieldName() + ",");
                bw.newLine();
                bw.write("\t\t\t</if>");
                bw.newLine();
            }
            bw.write("\t\t </trim>");
            bw.newLine();
            bw.write("\t\t <trim prefix=\"values (\" suffix=\")\" suffixOverrides=\",\" >");
            bw.newLine();
            for (FieldInfo FieldInfo : fieldInfoList) {
                if (FieldInfo.getAutoIncrement()) {
                    continue;
                }
                bw.write("\t\t\t<if test=\"bean." + FieldInfo.getPropertyName() + "!=null\">");
                bw.newLine();
                bw.write("\t\t\t\t #{bean." + FieldInfo.getPropertyName() + "},");
                bw.newLine();
                bw.write("\t\t\t</if>");
                bw.newLine();
            }
            bw.write("\t\t </trim>");
            bw.newLine();
            bw.write("\t</insert>");
            bw.newLine();
            bw.newLine();


            bw.write("\t<!-- 插入或者更新 （匹配有值的字段）-->");
            bw.newLine();
            bw.write("\t<insert id=\"insertOrUpdate\" parameterType=\"" + Constants.PACKAGE_BEAN + "." + tableInfo.getBeanName() + "\">");
            bw.newLine();
            bw.write("\t\t INSERT INTO " + tableInfo.getTableName());
            bw.newLine();
            bw.write("\t\t <trim prefix=\"(\" suffix=\")\" suffixOverrides=\",\">");
            bw.newLine();
            for (FieldInfo FieldInfo : fieldInfoList) {
                bw.write("\t\t\t<if test=\"bean." + FieldInfo.getPropertyName() + " != null\">");
                bw.newLine();
                bw.write("\t\t\t\t " + FieldInfo.getFieldName() + ",");
                bw.newLine();
                bw.write("\t\t\t</if>");
                bw.newLine();
            }
            bw.write("\t\t </trim>");
            bw.newLine();
            bw.write("\t\t <trim prefix=\"values (\" suffix=\")\" suffixOverrides=\",\">");
            bw.newLine();
            for (FieldInfo FieldInfo : fieldInfoList) {
                bw.write("\t\t\t<if test=\"bean." + FieldInfo.getPropertyName() + "!=null\">");
                bw.newLine();
                bw.write("\t\t\t\t #{bean." + FieldInfo.getPropertyName() + "},");
                bw.newLine();
                bw.write("\t\t\t</if>");
                bw.newLine();
            }
            bw.write("\t\t </trim>");
            bw.newLine();

            /**
             * 更新语句
             */
            bw.write("\t\t on DUPLICATE key update ");
            bw.newLine();
            bw.write("\t\t <trim prefix=\"\" suffix=\"\" suffixOverrides=\",\">");
            bw.newLine();
            for (FieldInfo FieldInfo : fieldInfoList) {
                if (FieldInfo.getAutoIncrement()) {
                    continue;
                }
                bw.write("\t\t\t<if test=\"bean." + FieldInfo.getPropertyName() + "!=null\">");
                bw.newLine();
                bw.write("\t\t\t\t " + FieldInfo.getFieldName() + " = VALUES(" + FieldInfo.getFieldName() + "),");
                bw.newLine();
                bw.write("\t\t\t</if>");
                bw.newLine();
            }
            bw.write("\t\t </trim>");
            bw.newLine();
            bw.write("\t</insert>");
            bw.newLine();
            bw.newLine();
            //批量插入
            bw.write("\t<!-- 添加 （批量插入）-->");
            bw.newLine();
            bw.write("\t<insert id=\"insertBatch\" parameterType=\"" + Constants.PACKAGE_BEAN + "." + tableInfo.getBeanName() + "\"");
            if (autoIncrementColumn != null) {
                bw.write(" useGeneratedKeys=\"true\" keyProperty=\"" + autoIncrementColumn.getPropertyName() + "\"");
            }
            bw.write(">");
            bw.newLine();
            bw.write("\t\t INSERT INTO " + tableInfo.getTableName());
            bw.write("(");
            for (int i = 0, len = fieldInfoList.size(); i < len; i++) {
                if (null != fieldInfoList.get(i).getAutoIncrement() && fieldInfoList.get(i).getAutoIncrement()) {
                    continue;
                }
                bw.newLine();
                bw.write("\t\t\t " + fieldInfoList.get(i).getFieldName());
                if (i != len - 1) {
                    bw.write(",");
                }
            }
            bw.newLine();
            bw.write("\t\t )values");
            bw.newLine();
            bw.write("\t\t <foreach collection=\"list\" item=\"item\" separator=\",\">");
            bw.newLine();
            bw.write("\t\t\t (");
            for (int i = 0, len = fieldInfoList.size(); i < len; i++) {
                if (null != fieldInfoList.get(i).getAutoIncrement() && fieldInfoList.get(i).getAutoIncrement()) {
                    continue;
                }
                bw.newLine();
                bw.write("\t\t\t\t #{item." + fieldInfoList.get(i).getPropertyName() + "}");
                if (i != len - 1) {
                    bw.write(",");
                }
            }
            bw.newLine();
            bw.write("\t\t\t )");
            bw.newLine();
            bw.write("\t\t </foreach>");
            bw.newLine();
            bw.write("\t</insert>");
            bw.newLine();
            bw.newLine();
            bw.write("\t<!-- 批量新增修改 （批量插入）-->");
            bw.newLine();
            bw.write("\t<insert id=\"insertOrUpdateBatch\" parameterType=\"" + Constants.PACKAGE_BEAN + "." + tableInfo.getBeanName() + "\">");
            bw.newLine();
            bw.write("\t\t INSERT INTO " + tableInfo.getTableName());
            bw.write("(");
            for (int i = 0, len = fieldInfoList.size(); i < len; i++) {
                if (null != fieldInfoList.get(i).getAutoIncrement() && fieldInfoList.get(i).getAutoIncrement()) {
                    continue;
                }
                bw.newLine();
                bw.write("\t\t\t " + fieldInfoList.get(i).getFieldName());
                if (i != len - 1) {
                    bw.write(",");
                }
            }
            bw.newLine();
            bw.write("\t\t )values");
            bw.newLine();
            bw.write("\t\t <foreach collection=\"list\" item=\"item\" separator=\",\">");
            bw.newLine();
            bw.write("\t\t\t (");
            for (int i = 0, len = fieldInfoList.size(); i < len; i++) {
                if (null != fieldInfoList.get(i).getAutoIncrement() && fieldInfoList.get(i).getAutoIncrement()) {
                    continue;
                }
                bw.newLine();
                bw.write("\t\t\t\t #{item." + fieldInfoList.get(i).getPropertyName() + "}");
                if (i != len - 1) {
                    bw.write(",");
                }
            }
            bw.newLine();
            bw.write("\t\t\t )");
            bw.newLine();
            bw.write("\t\t </foreach>");
            bw.newLine();
            bw.write("\t\t\ton DUPLICATE key update ");
            for (int i = 0, len = fieldInfoList.size(); i < len; i++) {
                FieldInfo FieldInfo = fieldInfoList.get(i);
                if (FieldInfo.getAutoIncrement()) {
                    continue;
                }
                bw.newLine();
                bw.write("\t\t\t" + FieldInfo.getFieldName() + " = VALUES(" + FieldInfo.getFieldName() + ")");
                if (i != len - 1) {
                    bw.write(",");
                }
            }
            bw.newLine();
            bw.write("\t</insert>");
            bw.newLine();
            bw.newLine();

            //多条件更新
            bw.write("\t<!--多条件修改-->");
            bw.newLine();
            bw.write("\t<update id=\"updateByParam\" parameterType=\"" + Constants.PACKAGE_PARAM + "." + tableInfo.getBeanParamName() + "\">");
            bw.newLine();
            bw.write("\t\t UPDATE " + tableInfo.getTableName());
            bw.newLine();
            bw.write(" \t\t <set> ");
            bw.newLine();
            for (FieldInfo FieldInfo : fieldInfoList) {
                if ((null != FieldInfo.getAutoIncrement() && FieldInfo.getAutoIncrement())) {
                    continue;
                }
                bw.write("\t\t\t<if test=\"bean." + FieldInfo.getPropertyName() + " != null\">");
                bw.newLine();
                bw.write("\t\t\t\t " + FieldInfo.getFieldName() + " = #{bean." + FieldInfo.getPropertyName() + "},");
                bw.newLine();
                bw.write("\t\t\t</if>");
                bw.newLine();
            }
            bw.write(" \t\t </set>");
            bw.newLine();
            bw.write(" \t\t <include refid=\"query_condition\" />");
            bw.newLine();
            bw.write("\t</update>");
            bw.newLine();
            bw.newLine();

            //多条件删除
            bw.write("\t<!--多条件删除-->");
            bw.newLine();
            bw.write("\t<delete id=\"deleteByParam\">");
            bw.newLine();
            bw.write("\t\t delete from " + tableInfo.getTableName());
            bw.newLine();
            bw.write(" \t\t <include refid=\"query_condition\" />");
            bw.newLine();
            bw.write("\t</delete>");
            bw.newLine();
            bw.newLine();
            Map<String, List<FieldInfo>> keyMap = tableInfo.getKeyIndexMap();
            for (Map.Entry<String, List<FieldInfo>> entry : keyMap.entrySet()) {
                List<FieldInfo> keyfieldInfoList = entry.getValue();
                StringBuffer whereStr = new StringBuffer();
                StringBuffer methodName = new StringBuffer();
                int index = 0;
                for (FieldInfo column : keyfieldInfoList) {
                    if (index > 0) {
                        methodName.append("And");
                        whereStr.append(" and ");
                    }
                    whereStr.append(column.getFieldName() + "=#{" + column.getPropertyName() + "}");
                    methodName.append(StringTools.upperCaseFirstLetter(column.getPropertyName()));
                    index++;
                }
                if (whereStr.length() > 0) {
                    bw.write("\t<!-- 根据" + methodName + "修改-->");
                    bw.newLine();
                    bw.write("\t<update id=\"updateBy" + methodName + "\" parameterType=\"" + Constants.PACKAGE_BEAN + "." + tableInfo
                            .getBeanName() + "\">");
                    bw.newLine();
                    bw.write("\t\t UPDATE " + tableInfo.getTableName());
                    bw.newLine();
                    bw.write(" \t\t <set> ");
                    bw.newLine();
                    for (FieldInfo FieldInfo : fieldInfoList) {
                        if ((null != FieldInfo.getAutoIncrement() && FieldInfo.getAutoIncrement())) {
                            continue;
                        }

                        boolean isKey = false;
                        for (FieldInfo keycolumn : keyfieldInfoList) {
                            if (keycolumn.getFieldName().equals(FieldInfo.getFieldName())) {
                                isKey = true;
                                break;
                            }
                        }
                        if (isKey) {
                            continue;
                        }

                        bw.write("\t\t\t<if test=\"bean." + FieldInfo.getPropertyName() + " != null\">");
                        bw.newLine();
                        bw.write("\t\t\t\t " + FieldInfo.getFieldName() + " = #{bean." + FieldInfo.getPropertyName() + "},");
                        bw.newLine();
                        bw.write("\t\t\t</if>");
                        bw.newLine();
                    }
                    bw.write(" \t\t </set>");
                    bw.newLine();
                    bw.write(" \t\t where " + whereStr);
                    bw.newLine();
                    bw.write("\t</update>");
                    bw.newLine();
                    bw.newLine();


                    bw.write("\t<!-- 根据" + methodName + "删除-->");
                    bw.newLine();
                    bw.write("\t<delete id=\"deleteBy" + methodName + "\">");
                    bw.newLine();
                    bw.write("\t\tdelete from " + tableInfo.getTableName() + " where " + whereStr);
                    bw.newLine();
                    bw.write("\t</delete>");
                    bw.newLine();
                    bw.newLine();

                    bw.write("\t<!-- 根据PrimaryKey获取对象-->");
                    bw.newLine();
                    bw.write("\t<select id=\"selectBy" + methodName + "\" resultMap=\"" + BASE_RESULT_MAP + "\" >");
                    bw.newLine();
                    bw.write("\t\tselect <include refid=\"" + BASE_COLUMN_LIST + "\" /> from " + tableInfo.getTableName() + " where " +
                            whereStr);
                    bw.newLine();
                    bw.write("\t</select>");
                    bw.newLine();
                    bw.newLine();
                }
            }
            bw.write("</mapper>");
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
