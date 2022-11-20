package com.miyuki.mrpc.core.common.factory;

import java.lang.reflect.Constructor;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Created with IntelliJ IDEA.
 *
 * @Author: miyuki
 * @Date: 2022/11
 * @Description: 单例工厂
 */
public class SingletonFactory {
    private static final Map<String,Object> OBJECT_MAP;

    static {
        OBJECT_MAP = new ConcurrentHashMap<>();
    }

    private SingletonFactory(){

    }

    public static <T>T getInstance(Class<T> clazz){
        if (clazz == null){
            throw new IllegalArgumentException();
        }

        String key = clazz.toString();
        if (OBJECT_MAP.containsKey(key)){
            return clazz.cast(OBJECT_MAP.get(key));
        } else {
            return clazz.cast(OBJECT_MAP.computeIfAbsent(key, (k) -> {
                try{
                    Constructor<T> declaredConstructor = clazz.getDeclaredConstructor();
                    declaredConstructor.setAccessible(true);
                    return declaredConstructor.newInstance();
                }catch (Exception e){
                    throw new RuntimeException(e.getMessage(),e);
                }
            }));
        }
    }
}

