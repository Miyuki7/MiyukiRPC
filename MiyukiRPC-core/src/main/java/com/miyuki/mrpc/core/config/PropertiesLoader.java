package com.miyuki.mrpc.core.config;

import com.miyuki.mrpc.core.common.utils.StringUtil;

import java.io.IOException;
import java.io.InputStream;
import java.util.HashMap;
import java.util.Map;
import java.util.Properties;

/**
 * Created with IntelliJ IDEA.
 *
 * @Author: miyuki
 * @Date: 2022/11
 * @Description: 配置加载器
 */
public class PropertiesLoader {

    private static Properties properties;

    private static Map<String, String> propertiesMap = new HashMap<>();

    private static String DEFAULT_PROPERTIES_FILE = "mrpc.properties";

    public static void loadConfiguration() throws IOException {
        if (properties != null) {
            return;
        }
        properties = new Properties();
//        FileInputStream in = null;
//        in = new FileInputStream(new File(DEFAULT_PROPERTIES_FILE));
        InputStream in = PropertiesLoader.class.getClassLoader().getResourceAsStream(DEFAULT_PROPERTIES_FILE);
        properties.load(in);
    }

    /**
     * 根据键值获取配置属性
     *
     * @param key
     * @return
     */
    public static String getPropertiesStr(String key) {
        if (properties == null) {
            return null;
        }
        if (StringUtil.isEmpty(key)) {
            return null;
        }
        if (!propertiesMap.containsKey(key)) {
            String value = properties.getProperty(key);
            propertiesMap.put(key, value);
        }
        return propertiesMap.get(key) == null ? null : String.valueOf(propertiesMap.get(key));
    }

    public static String getPropertiesNotBlank(String key) {
        String val = getPropertiesStr(key);
        if (val == null || val.equals("")) {
            throw new IllegalArgumentException(key + " 配置为空异常");
        }
        return val;
    }

    public static String getPropertiesStrDefault(String key, String defaultVal) {
        String val = getPropertiesStr(key);
        return val == null || val.equals("") ? defaultVal : val;
    }

    /**
     * 根据键值获取配置属性
     *
     * @param key
     * @return
     */
    public static Integer getPropertiesInteger(String key) {
        if (properties == null) {
            return null;
        }
        if (StringUtil.isEmpty(key)) {
            return null;
        }
        if (!propertiesMap.containsKey(key)) {
            String value = properties.getProperty(key);
            propertiesMap.put(key, value);
        }
        return Integer.valueOf(propertiesMap.get(key));
    }

    /**
     * 根据键值获取配置属性
     *
     * @param key
     * @return
     */
    public static Integer getPropertiesIntegerDefault(String key,Integer defaultVal) {
        if (properties == null) {
            return defaultVal;
        }
        if (StringUtil.isEmpty(key)) {
            return defaultVal;
        }
        String value = properties.getProperty(key);
        if(value==null){
            propertiesMap.put(key, String.valueOf(defaultVal));
            return defaultVal;
        }
        if (!propertiesMap.containsKey(key)) {
            propertiesMap.put(key, value);
        }
        return Integer.valueOf(propertiesMap.get(key));
    }
}
