package xyz.kbws.bean;

import xyz.kbws.utils.PropertiesUtils;

public class Constants {

    /**
     * 包路径
     */
    public static String PACKAGE_BASE = null;
    public static String PACKAGE_BEAN = null;
    public static String PACKAGE_PARAM = null;
    public static String PACKAGE_ENUMS = null;
    public static String PACKAGE_VO = null;
    public static String PACKAGE_MAPPER = null;
    public static String PACKAGE_SERVICE = null;
    public static String PACKAGE_SERVICE_IMPL = null;
    public static String PACKAGE_CONTROLLER = null;
    public static String PACKAGE_EXCEPTION = null;
    public static String PACKAGE_UTILS = null;
    /**
     * 文件存储路径
     */
    public static String PATH_BASE = null;

    public static String PATH_BEAN = null;
    public static String PATH_PARAM = null;
    public static String PATH_ENUMS = null;
    public static String PATH_VO = null;
    public static String PATH_MAPPER = null;
    public static String PATH_MAPPER_XML = null;
    public static String PATH_SERVICE = null;
    public static String PATH_SERVICE_IMPL = null;
    public static String PATH_CONTROLLER = null;
    public static String PATH_EXCEPTION = null;
    public static String PATH_UTILS = null;
    /**
     * 后缀
     */
    public static String SUFFIX_MAPPER = null;
    public static String SUFFIX_MAPPER_XML = null;
    public static String SUFFIX_SERVICE = null;
    public static String SUFFIX_SERVICE_IMPL = null;
    public static String SUFFIX_CONTROLLER = null;
    public static String SUFFIX_PROPERTY_FUZZY = null;
    public static String SUFFIX_BEAN_PARAM = null;
    public static String SUFFIX_BEAN_PARAM_TIME_START = null;
    public static String SUFFIX_BEAN_PARAM_TIME_END = null;


    /**
     * 是否忽略表前缀
     */

    public static Boolean IGNORE_TABLE_PREFIX;

    public static String TABLE_SPLIT_PREFIX;


    public static String[] IGNORE_BEAN_TOSTRING_COLUMN;
    public static String[] IGNORE_BEAN_TOJSON_COLUMN;
    public static String IGNORE_BEAN_TOJSON_EXPRESSION;
    public static String IGNORE_BEAN_TOJSON_CLASS;

    /**
     * 格式化日期配置
     */
    public static String BEAN_DATE_EXPRESSION;
    public static String BEAN_DATE_EXPRESSION_CLASS;
    public static String BEAN_DATE_FORMAT;
    public static String BEAN_DATE_FORMAT_CLASS;

    private static String MAVEN_PATH = "/src/main/";

    private static String PATH_JAVA = MAVEN_PATH + "java/";

    private static String PATH_RESOURCES = MAVEN_PATH + "resources/";

    static {
        PACKAGE_BASE = PropertiesUtils.getString("package.base");
        PACKAGE_BEAN = PACKAGE_BASE + "." + PropertiesUtils.getString("package.bean");
        PACKAGE_PARAM = PACKAGE_BASE + "." + PropertiesUtils.getString("package.param");
        PACKAGE_ENUMS = PACKAGE_BASE + "." + PropertiesUtils.getString("package.enums");
        PACKAGE_VO = PACKAGE_BASE + "." + PropertiesUtils.getString("package.vo");
        PACKAGE_MAPPER = PACKAGE_BASE + "." + PropertiesUtils.getString("package.mapper");
        PACKAGE_SERVICE = PACKAGE_BASE + "." + PropertiesUtils.getString("package.service");
        PACKAGE_SERVICE_IMPL = PACKAGE_BASE + "." + PropertiesUtils.getString("package.service.impl");
        PACKAGE_CONTROLLER = PACKAGE_BASE + "." + PropertiesUtils.getString("package.controller");
        PACKAGE_EXCEPTION = PACKAGE_BASE + ".exception";
        PACKAGE_UTILS = PACKAGE_BASE + ".utils";
        PATH_BASE = PropertiesUtils.getString("path.base");
        PATH_BASE = PATH_BASE + PATH_JAVA;
        PATH_BEAN = PATH_BASE + PACKAGE_BEAN.replace(".", "/");
        PATH_PARAM = PATH_BASE + PACKAGE_PARAM.replace(".", "/");
        PATH_ENUMS = PATH_BASE + PACKAGE_ENUMS.replace(".", "/");
        PATH_VO = PATH_BASE + PACKAGE_VO.replace(".", "/");
        PATH_MAPPER = PATH_BASE + PACKAGE_MAPPER.replace(".", "/");
        PATH_MAPPER_XML = PropertiesUtils.getString("path.base") + PATH_RESOURCES + PACKAGE_MAPPER.replace(".", "/");
        PATH_SERVICE = PATH_BASE + PACKAGE_SERVICE.replace(".", "/");
        PATH_SERVICE_IMPL = PATH_BASE + PACKAGE_SERVICE_IMPL.replace(".", "/");
        PATH_CONTROLLER = PATH_BASE + PACKAGE_CONTROLLER.replace(".", "/");
        PATH_EXCEPTION = PATH_BASE + PACKAGE_EXCEPTION.replace(".", "/");
        PATH_UTILS = PATH_BASE + PACKAGE_UTILS.replace(".", "/");

        SUFFIX_BEAN_PARAM = PropertiesUtils.getString("suffix.bean.param");
        SUFFIX_PROPERTY_FUZZY = PropertiesUtils.getString("suffix.property.fuzzy");
        SUFFIX_MAPPER = PropertiesUtils.getString("suffix.mapper");
        SUFFIX_MAPPER_XML = PropertiesUtils.getString("suffix.mapper.xml");
        SUFFIX_SERVICE = PropertiesUtils.getString("suffix.service");
        SUFFIX_SERVICE_IMPL = PropertiesUtils.getString("suffix.service.impl");
        SUFFIX_CONTROLLER = PropertiesUtils.getString("suffix.controller");
        SUFFIX_BEAN_PARAM_TIME_START = PropertiesUtils.getString("suffix.bean.param.time.start");
        SUFFIX_BEAN_PARAM_TIME_END = PropertiesUtils.getString("suffix.bean.param.time.end");
        IGNORE_TABLE_PREFIX = PropertiesUtils.getString("ignore.table.prefix") == null ? false : Boolean.parseBoolean(PropertiesUtils.getString("ignore.table.prefix"));
        /**
         * 是否分表
         */
        TABLE_SPLIT_PREFIX = PropertiesUtils.getString("table.split.prefix");

        /**
         * 处理忽略tostring的属性
         */
        String ignore_bean_tostring_columnstr = PropertiesUtils.getString("ignore.bean.tostring.column");
        if (null != ignore_bean_tostring_columnstr) {
            IGNORE_BEAN_TOSTRING_COLUMN = ignore_bean_tostring_columnstr.split(",");
        }

        /**
         * 返回json时不需要返回的属性
         */
        String ignore_bean_tojson_columnstr = PropertiesUtils.getString("ignore.bean.tojson.column");
        if (null != ignore_bean_tojson_columnstr) {
            IGNORE_BEAN_TOJSON_COLUMN = ignore_bean_tojson_columnstr.split(",");
        }
        IGNORE_BEAN_TOJSON_EXPRESSION = PropertiesUtils.getString("ignore.bean.tojson.expression");

        String ignore_bean_tojson_classstr = PropertiesUtils.getString("ignore.bean.tojson.class");
        if (null != ignore_bean_tojson_classstr && !"".equals(ignore_bean_tojson_classstr)) {
            IGNORE_BEAN_TOJSON_CLASS = ignore_bean_tojson_classstr;
        }


        //日期格式化配置
        BEAN_DATE_EXPRESSION = PropertiesUtils.getString("bean.date.expression");
        BEAN_DATE_EXPRESSION_CLASS = PropertiesUtils.getString("bean.date.expression.class");
        BEAN_DATE_FORMAT = PropertiesUtils.getString("bean.data.format");
        BEAN_DATE_FORMAT_CLASS = PropertiesUtils.getString("bean.date.format.class");
    }

    public final static String[] SQL_DATE_TIIME_TYPES = new String[]{"datetime", "timestamp"};

    public final static String[] SQL_DATE_TYPES = new String[]{"date"};

    public static final String[] SQL_DECIMAL_TYPE = new String[]{"decimal", "double", "float"};

    public static final String[] SQL_STRING_TYPE = new String[]{"char", "varchar", "text", "mediumtext", "longtext"};

    //Integer
    public static final String[] SQL_INTEGER_TYPE = new String[]{"int", "tinyint"};

    //Long
    public static final String[] SQL_LONG_TYPE = new String[]{"bigint"};

    /**
     * 常量
     */
    public static String TYPE_STRING = "String";

    public static String TYPE_DATE = "Date";
}
