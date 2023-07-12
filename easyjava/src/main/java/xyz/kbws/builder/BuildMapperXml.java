package xyz.kbws.builder;

import org.apache.commons.lang3.ArrayUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import xyz.kbws.bean.Constants;
import xyz.kbws.bean.FieldInfo;
import xyz.kbws.bean.TableInfo;
import xyz.kbws.utils.StringUtils;

import java.io.*;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * @author hsy
 * @date 2023/6/29
 */
public class BuildMapperXml {
    private static final Logger logger = LoggerFactory.getLogger(BuildMapperXml.class);

    private static final String BASE_COLUMN_LIST="base_column_list";

    private static final String BASE_QUERY_CONDITION="base_query_condition";

    private static final String BASE_QUERY_CONDITION_EXTEND="base_query_condition_extend";

    private static final String QUERY_CONDITION="query_condition";

    public static void execute(TableInfo tableInfo) {
        File folder = new File(Constants.PATH_MAPPER_XML);
        if (!folder.exists()) {
            folder.mkdirs();
        }

        String className = tableInfo.getBeanName() + Constants.SUFFIX_MAPPER;
        File poFile = new File(folder, className + ".xml");

        OutputStream out = null;
        OutputStreamWriter outWrite = null;
        BufferedWriter bw = null;
        try {
            out = new FileOutputStream(poFile);
            outWrite = new OutputStreamWriter(out, "utf8");
            bw = new BufferedWriter(outWrite);
            bw.write("<?xml version=\"1.0\" encoding=\"utf-8\" ?>");
            bw.newLine();
            bw.write("<!DOCTYPE mapper PUBLIC \"-//mybatis.org//DTD Mapper 3.0/EN\"");
            bw.newLine();
            bw.write("\t\t\"http://mybatis.org/dtd/mybatis-3-mapper.dtd\">");
            bw.newLine();
            bw.write("<mapper namespace=\""+Constants.PACKAGE_MAPPER+"."+className+"\">");
            bw.newLine();

            bw.write("\t<!--实体映射-->");
            bw.newLine();
            String poClass = Constants.PACKAGE_PO + "." + tableInfo.getBeanName();
            bw.write("\t<resultMap id=\"base_result_map\" type=\"" + poClass +"\">");
            bw.newLine();

            FieldInfo idField = null;
            Map<String, List<FieldInfo>> keyIndexInfo = tableInfo.getKeyIndexMap();
            for (Map.Entry<String, List<FieldInfo>> entry : keyIndexInfo.entrySet()){
                if ("PRIMARY".equals(entry.getKey())){
                    List<FieldInfo> fieldInfoList = entry.getValue();
                    if (fieldInfoList.size()==1){
                        idField = fieldInfoList.get(0);
                        break;
                    }
                }
            }

            for (FieldInfo fieldInfo : tableInfo.getFieldList()){
                bw.write("\t\t<!--"+ fieldInfo.getComment() +"-->");
                bw.newLine();
                String key;
                if (idField!=null && fieldInfo.getPropertyName().equals(idField.getPropertyName())){
                    key = "id";
                }else {
                    key = "result";
                }
                bw.write("\t\t<"+key+" column=\""+fieldInfo.getFieldName()+"\" property=\""+fieldInfo.getPropertyName()+"\"/>");
                bw.newLine();
            }


            bw.write("\t</resultMap>");
            bw.newLine();
            bw.newLine();

            //通用查询列
            bw.write("\t<!--通用查询结果列-->");
            bw.newLine();

            bw.write("\t<sql id=\""+BASE_COLUMN_LIST+"\">");
            bw.newLine();
            StringBuffer columnBuilder = new StringBuffer();
            for (FieldInfo fieldInfo : tableInfo.getFieldList()){
                columnBuilder.append(fieldInfo.getFieldName()).append(",");
            }
            String columnBuilderStr = columnBuilder.substring(0, columnBuilder.lastIndexOf(","));
            bw.write("\t\t"+columnBuilderStr);
            bw.newLine();
            bw.write("\t</sql>");
            bw.newLine();
            bw.newLine();

            //基础查询条件
            bw.write("\t<!--基础查询条件-->");
            bw.newLine();
            bw.write("\t<sql id=\""+BASE_QUERY_CONDITION+"\">");
            bw.newLine();

            for (FieldInfo fieldInfo : tableInfo.getFieldList()){
                String stringQuery = "";
                if (ArrayUtils.contains(Constants.SQL_STRING_TYPES, fieldInfo.getSqlType())){
                    stringQuery = " and query." + fieldInfo.getPropertyName() + "!=''";
                }
                bw.write("\t\t<if test=\"query."+fieldInfo.getPropertyName()+" != null"+stringQuery+"\">");
                bw.newLine();
                bw.write("\t\t\t and id = ${query."+fieldInfo.getPropertyName()+"}");
                bw.newLine();
                bw.write("\t\t</if>");
                bw.newLine();
            }

            bw.write("\t</sql>");
            bw.newLine();

            bw.newLine();
            bw.write("\t<!--扩展的查询条件-->");
            bw.newLine();
            bw.write("\t<sql id=\""+BASE_QUERY_CONDITION_EXTEND+"\">");
            bw.newLine();

            for (FieldInfo fieldInfo : tableInfo.getFieldExtendList()){
                String andWhere = "";
                if (ArrayUtils.contains(Constants.SQL_STRING_TYPES, fieldInfo.getSqlType())){
                    andWhere = "and "+fieldInfo.getFieldName()+
                            " like concat('%', #{query."+fieldInfo.getPropertyName()+"}, '%')";
                } else if (ArrayUtils.contains(Constants.SQL_DATE_TIME_TYPES, fieldInfo.getSqlType()) ||
                        ArrayUtils.contains(Constants.SQL_DATE_TYPES, fieldInfo.getSqlType())) {
                    if (fieldInfo.getPropertyName().endsWith(Constants.SUFFIX_BEAN_QUERY_FUZZY_TIME_START)){
                        andWhere = "<![CDATA[ and "+fieldInfo.getFieldName() +
                                " >= str_to_date(#{query."+fieldInfo.getPropertyName()+"}, '%Y-%m-%d')]]>";
                    } else if (fieldInfo.getPropertyName().endsWith(Constants.SUFFIX_BEAN_QUERY_FUZZY_TIME_END)) {
                        andWhere = "<![CDATA[ and "+fieldInfo.getFieldName() +
                                " < date_sub(str_to_date(#{query."+fieldInfo.getPropertyName()+"}, '%Y-%m-%d'), interval -1 day)]]>";
                    }
                }
                bw.write("\t\t<if test=\"query."+fieldInfo.getPropertyName()+" != null and query."+
                        fieldInfo.getPropertyName()+" !=''\">");
                bw.newLine();
                bw.write("\t\t\t"+andWhere);
                bw.newLine();
                bw.write("\t\t</if>");
                bw.newLine();
            }

            bw.write("\t</sql>");
            bw.newLine();

            //通用查询条件
            bw.newLine();
            bw.write("\t<!--通用查询条件-->");
            bw.newLine();
            bw.write("\t<sql id=\""+QUERY_CONDITION+"\">");
            bw.newLine();
            bw.write("\t\t<where>");
            bw.newLine();
            bw.write("\t\t\t<include refid=\""+BASE_QUERY_CONDITION+"\"/>");
            bw.newLine();
            bw.write("\t\t\t<include refid=\""+BASE_QUERY_CONDITION_EXTEND+"\"/>");
            bw.newLine();
            bw.write("\t\t</where>");
            bw.newLine();
            bw.write("\t</sql>");
            bw.newLine();

            //查询列表
            bw.newLine();
            bw.write("\t<!--查询列表-->");
            bw.newLine();
            bw.write("\t<select id=\"selectList\" resultMap=\"base_result_map\">");
            bw.newLine();
            bw.write("\t\tSELECT <include refid=\""+BASE_COLUMN_LIST+"\"/> FROM "+tableInfo.getTableName()+
                    " <include refid=\""+QUERY_CONDITION+"\"/>");
            bw.newLine();
            bw.write("\t\t<if test=\"query.orderBy!=null\">order by ${query.orderBy}</if>");
            bw.newLine();
            bw.write("\t\t<if test=\"query.simplePage!=null\">limit #{query.simplePage.start},#{query.simplePage.end}</if>");
            bw.newLine();
            bw.write("\t</select>");
            bw.newLine();
            bw.newLine();

            //查询数量
            bw.newLine();
            bw.write("\t<!--查询数量-->");
            bw.newLine();
            bw.write("\t<select id=\"selectCount\" resultType=\"java.lang.Integer\">");
            bw.newLine();
            bw.write("\t\tSELECT count(1) FROM "+tableInfo.getTableName() + " <include refid=\""+QUERY_CONDITION+"\"/>");
            bw.newLine();
            bw.write("\t</select>");
            bw.newLine();

            //单条插入
            bw.newLine();
            bw.write("\t<!--插入(匹配有值的字段)-->");
            bw.newLine();
            bw.write("\t<insert id=\"insert\" parameterType=\""+poClass+"\">");
            bw.newLine();

            FieldInfo autoIncrementField = null;
            for (FieldInfo fieldInfo: tableInfo.getFieldList()){
                if (fieldInfo.getAutoIncrement()!=null&&fieldInfo.getAutoIncrement()){
                    autoIncrementField = fieldInfo;
                    break;
                }
            }
            if (autoIncrementField!=null){
                bw.write("\t\t<selectKey keyProperty=\"bean."+autoIncrementField.getFieldName()+
                        "\" resultType=\""+autoIncrementField.getJavaType()+"\" order=\"AFTER\">");
                bw.newLine();
                bw.write("\t\t\tSELECT LAST_INSERT_ID()");
                bw.newLine();
                bw.write("\t\t</selectKey>");
                bw.newLine();
            }
            bw.write("\t\tINSERT INTO "+tableInfo.getTableName());
            bw.newLine();
            bw.write("\t\t<trim prefix=\"(\" suffix=\")\" suffixOverrides=\",\">");
            bw.newLine();
            for (FieldInfo fieldInfo: tableInfo.getFieldList()){
                bw.write("\t\t\t<if test=\"bean."+fieldInfo.getPropertyName()+" !=null\">");
                bw.newLine();
                bw.write("\t\t\t\t"+fieldInfo.getFieldName()+",");
                bw.newLine();
                bw.write("\t\t\t</if>");
                bw.newLine();
            }
            bw.write("\t\t</trim>");
            bw.newLine();
            bw.write("\t\t<trim prefix=\"values (\" suffix=\")\" suffixOverrides=\",\">");
            bw.newLine();

            for (FieldInfo fieldInfo: tableInfo.getFieldList()){
                bw.write("\t\t\t<if test=\"bean."+fieldInfo.getPropertyName()+" !=null\">");
                bw.newLine();
                bw.write("\t\t\t\t#{bean."+fieldInfo.getPropertyName()+"},");
                bw.newLine();
                bw.write("\t\t\t</if>");
                bw.newLine();
            }


            bw.write("\t\t</trim>");
            bw.newLine();
            bw.write("\t</insert>");
            bw.newLine();
            bw.newLine();

            //插入或者更新
            bw.write("\t<!--插入或更新(匹配有值的字段)-->");
            bw.newLine();
            bw.write("\t<insert id=\"insertOrUpdate\" parameterType=\""+poClass+"\">");
            bw.newLine();

            bw.write("\t\tINSERT INTO "+tableInfo.getTableName());
            bw.newLine();
            bw.write("\t\t<trim prefix=\"(\" suffix=\")\" suffixOverrides=\",\">");
            bw.newLine();
            for (FieldInfo fieldInfo: tableInfo.getFieldList()){
                bw.write("\t\t\t<if test=\"bean."+fieldInfo.getPropertyName()+" !=null\">");
                bw.newLine();
                bw.write("\t\t\t\t"+fieldInfo.getFieldName()+",");
                bw.newLine();
                bw.write("\t\t\t</if>");
                bw.newLine();
            }
            bw.write("\t\t</trim>");
            bw.newLine();
            bw.write("\t\t<trim prefix=\"values (\" suffix=\")\" suffixOverrides=\",\">");
            bw.newLine();

            for (FieldInfo fieldInfo: tableInfo.getFieldList()){
                bw.write("\t\t\t<if test=\"bean."+fieldInfo.getPropertyName()+" !=null\">");
                bw.newLine();
                bw.write("\t\t\t\t#{bean."+fieldInfo.getPropertyName()+"},");
                bw.newLine();
                bw.write("\t\t\t</if>");
                bw.newLine();
            }
            bw.write("\t\t</trim>");
            bw.newLine();

            bw.write("\t\ton DUPLICATE key update");
            bw.newLine();

            Map<String, String> tempMap = new HashMap<String, String>();
            for (Map.Entry<String, List<FieldInfo>> entry : keyIndexInfo.entrySet()){
                List<FieldInfo> fieldInfoList = entry.getValue();
                for (FieldInfo item : fieldInfoList){
                    tempMap.put(item.getFieldName(), item.getFieldName());
                }
            }

            bw.write("\t\t<trim prefix=\"\" suffix=\"\" suffixOverrides=\",\">");
            bw.newLine();
            for (FieldInfo fieldInfo: tableInfo.getFieldList()){
                if (tempMap.get(fieldInfo.getFieldName())!=null){
                    continue;
                }
                bw.write("\t\t\t<if test=\"bean."+fieldInfo.getPropertyName()+" !=null\">");
                bw.newLine();
                bw.write("\t\t\t\t"+fieldInfo.getFieldName()+" = VALUES("+fieldInfo.getFieldName()+"),");
                bw.newLine();
                bw.write("\t\t\t</if>");
                bw.newLine();
            }
            bw.write("\t\t</trim>");
            bw.newLine();

            bw.write("\t</insert>");
            bw.newLine();

            //批量插入
            bw.write("\t<!--批量插入-->");
            bw.newLine();
            bw.write("\t<insert id=\"insertBatch\" parameterType=\""+poClass+"\">");
            bw.newLine();
            StringBuffer insertFieldBuffer = new StringBuffer();
            StringBuffer insertPropertyBuffer = new StringBuffer();
            for (FieldInfo fieldInfo : tableInfo.getFieldList()){
                if (fieldInfo.getAutoIncrement()){
                    continue;
                }
                insertFieldBuffer.append(fieldInfo.getFieldName()).append(",");
                insertPropertyBuffer.append("#{item."+fieldInfo.getPropertyName()+"}").append(",");
            }
            String insertFieldBufferStr = insertFieldBuffer.substring(0, insertFieldBuffer.lastIndexOf(","));
            bw.write("\t\tINSERT INTO "+tableInfo.getTableName()+"("+insertFieldBufferStr+")values ");
            bw.newLine();
            bw.write("\t\t<foreach collection=\"list\" item=\"item\" separator=\",\" open=\"(\" close=\")\">");
            bw.newLine();
            String insertPropertyBufferStr = insertPropertyBuffer.substring(0, insertPropertyBuffer.lastIndexOf(","));
            bw.write("\t\t\t("+insertPropertyBufferStr+")");
            bw.newLine();
            bw.write("\t\t</foreach>");
            bw.newLine();
            bw.write("\t</insert>");
            bw.newLine();

            //批量插入或更新
            bw.write("\t<!--批量插入或更新-->");
            bw.newLine();
            bw.write("\t<insert id=\"insertOrUpdateBatch\" parameterType=\""+poClass+"\">");
            bw.newLine();
            insertFieldBufferStr = insertFieldBuffer.substring(0, insertFieldBuffer.lastIndexOf(","));
            bw.write("\t\tINSERT INTO "+tableInfo.getTableName()+"("+insertFieldBufferStr+")values ");
            bw.newLine();
            bw.write("\t\t<foreach collection=\"list\" item=\"item\" separator=\",\">");
            bw.newLine();
            insertPropertyBufferStr = insertPropertyBuffer.substring(0, insertPropertyBuffer.lastIndexOf(","));
            bw.write("\t\t\t("+insertPropertyBufferStr+")");
            bw.newLine();
            bw.write("\t\t</foreach>");
            bw.newLine();

            bw.write("\t\ton DUPLICATE key update");
            bw.newLine();
            StringBuffer insertBatchUpdateBuffer = new StringBuffer();
            for (FieldInfo fieldInfo : tableInfo.getFieldList()){
                insertBatchUpdateBuffer.append(fieldInfo.getFieldName()+" = VALUES("+fieldInfo.getFieldName()+"),");
            }
            String insertBatchUpdateBufferStr = insertBatchUpdateBuffer.substring(0, insertBatchUpdateBuffer.lastIndexOf(","));
            bw.write("\t\t"+insertBatchUpdateBufferStr);
            bw.newLine();
            bw.write("\t</insert>");
            bw.newLine();
            bw.newLine();

            //根据主键更新
            Map<String, List<FieldInfo>> keyIndexMap = tableInfo.getKeyIndexMap();
            for (Map.Entry<String, List<FieldInfo>> entry : keyIndexMap.entrySet()){
                List<FieldInfo> keyFieldInfoList = entry.getValue();
                Integer index = 0;
                StringBuffer methodName = new StringBuffer();
                StringBuffer paramNames = new StringBuffer();
                for (FieldInfo fieldInfo : keyFieldInfoList){
                    index++;
                    methodName.append(StringUtils.upCaseFirstLetter(fieldInfo.getPropertyName()));
                    paramNames.append(fieldInfo.getFieldName()+"=#{"+fieldInfo.getPropertyName()+"}");
                    if (index < keyFieldInfoList.size()){
                        methodName.append("And");
                        paramNames.append(" and ");
                    }
                }
                //查询
                bw.write("\t<!--根据"+methodName+"查询-->");
                bw.newLine();
                bw.write("\t<select id=\"selectBy" + methodName + "\" resultMap=\"base_result_map\">");
                bw.newLine();
                bw.write("\t\tselect <include refid=\""+BASE_COLUMN_LIST+"\"/> from "+tableInfo.getTableName()
                        +" where "+paramNames);
                bw.newLine();
                bw.write("\t</select>");
                bw.newLine();
                bw.newLine();

                //更新
                bw.write("\t<!--根据"+methodName+"更新-->");
                bw.newLine();
                bw.write("\t<update id=\"updateBy"+methodName+"\" parameterType=\""+poClass+"\">");
                bw.newLine();
                bw.write("\t\tupdate "+tableInfo.getTableName());
                bw.newLine();
                bw.write("\t\t<set>");
                bw.newLine();
                for (FieldInfo fieldInfo : tableInfo.getFieldList()){
                    bw.write("\t\t\t<if test=\"bean."+fieldInfo.getPropertyName()+"!=null\">");
                    bw.newLine();
                    bw.write("\t\t\t\t"+fieldInfo.getFieldName()+" =#{bean."+fieldInfo.getPropertyName()+"},");
                    bw.newLine();
                    bw.write("\t\t\t</if>");
                    bw.newLine();
                }
                bw.write("\t\t</set>");
                bw.newLine();
                bw.write("\t\twhere "+paramNames);
                bw.newLine();
                bw.write("\t</update>");
                bw.newLine();
                bw.newLine();
                //删除
                bw.write("\t<!--根据"+methodName+"删除-->");
                bw.newLine();
                bw.write("\t<delete id=\"deleteBy"+methodName+"\">");
                bw.newLine();
                bw.write("\t\tdelete from "+tableInfo.getTableName()+" where "+paramNames);
                bw.newLine();
                bw.write("\t</delete>");
                bw.newLine();
                bw.newLine();
            }


            bw.write("</mapper>");

            bw.flush();
        } catch (Exception e) {
            logger.error("创建mapper XML失败", e);
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
