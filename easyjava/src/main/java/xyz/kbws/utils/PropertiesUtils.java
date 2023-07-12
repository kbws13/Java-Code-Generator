package xyz.kbws.utils;

import java.io.IOException;
import java.io.InputStream;
import java.util.Iterator;
import java.util.Map;
import java.util.Properties;
import java.util.concurrent.ConcurrentHashMap;

/**
 * @author hsy
 * @date 2023/6/25
 */
public class PropertiesUtils {
    private static Properties prop = new Properties();
    private static Map<String, String> PROPER_MAP = new ConcurrentHashMap<String, String>();

    static {
        InputStream is = null;
        try {
            is = PropertiesUtils.class.getClassLoader().getResourceAsStream("application.properties");
            prop.load(is);

            Iterator<Object> iterator = prop.keySet().iterator();
            while (iterator.hasNext()){
                String key = (String) iterator.next();
                PROPER_MAP.put(key, prop.getProperty(key));
            }

        }catch (Exception e){

        }finally {
            if (is!=null){
                try {
                    is.close();
                } catch (IOException e) {
                    throw new RuntimeException(e);
                }
            }
        }
    }
    public static String getString(String key){
        return PROPER_MAP.get(key);
    }

}
