package xyz.kbws.utils;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.serializer.SerializerFeature;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;



/**
 * @author hsy
 * @date 2023/6/26
 */
public class JsonUtils {
    public static String covertObj2Json(Object obj){
        if (null == obj){
            return null;
        }
        return JSON.toJSONString(obj, SerializerFeature.DisableCircularReferenceDetect);
    }
}
