package com.miyuki.mrpc.core.common.extension;

import com.miyuki.mrpc.core.common.utils.StringUtil;
import com.sun.xml.internal.ws.policy.privateutil.PolicyUtils;
import lombok.extern.slf4j.Slf4j;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.util.Enumeration;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Created with IntelliJ IDEA.
 *
 * @Author: miyuki
 * @Date: 2022/11
 * @Description: 很关键的spi加载类
 * 使用方法，外部调用ExtensionLoader.getExtensionLoader(ServiceDiscovery.class).getExtension("zk");
 * 即可加载真正的实现类
 */
@Slf4j
public class ExtensionLoader<T> {
    private static final String SERVICE_DICTORY = "META-INF/extensions/";

    //static 类变量，所有实例都可见
    //缓存某一个类的ExtensionLoader
    //Map<ServiceDiscovery.class,ExtensionLoader<ServiceDiscovery.class>>
    private static final Map<Class<?>, ExtensionLoader<?>> EXTENSION_LOADERS = new ConcurrentHashMap<>();

    //static 类变量，所有实例都可见
    //缓存某一个类的实体，Map<com.yf.serialize.kyro.KyroSerializer.class, 一个真正的实例>
    private static final Map<Class<?>, Object> EXTENSION_INSTANCE =  new ConcurrentHashMap<>();

    //指定该ExtensionLoader的类型
    private final Class<?> type;

    //成员变量，每一个实例对象都有一个cachedInstance
    //缓存类型是，名字对实例对象，和下面的cachedClasses不一样
    // Map<zk,Holder<ZkServiceRegistryImpl>> Holder就是一个保存实例的对象
    //Holder<T>  Holder<ZkServiceRegistryImpl>
    //Holder.set(ZkServiceRegistryImpl)
    private final Map<String, Holder<Object>> cachedInstance = new ConcurrentHashMap<>();

    //成员变量，每一个实例对象都有一个cachedClasses
    //缓存类型是名字对class对象
    //Holder<Map<kyro，com.yf.serialize.kyro.KyroSerializer.class>>>
    private final Holder<Map<String, Class<?>>> cachedClasses = new Holder<>();

    private ExtensionLoader(Class<?> type) {
        this.type = type;
    }

    //得到某一个接口的ExtensionLoader，必须是接口
    //ExtensionLoader.getExtensionLoader(ServiceDiscovery.class).getExtension("zk");
    public static <T> ExtensionLoader<T> getExtensionLoader(Class<T> type) {
        if (type == null) {
            throw new IllegalArgumentException("Extension type should not be null.");
        }
        if (!type.isInterface()) {
            throw new IllegalArgumentException("Extension type must be an interface.");
        }
        if (type.getAnnotation(SPI.class) == null) {
            throw new IllegalArgumentException("Extension type must be annotated by @SPI ");
        }

        ExtensionLoader<T> extensionLoader = (ExtensionLoader<T>) EXTENSION_LOADERS.get(type);
        if (extensionLoader == null) {
            EXTENSION_LOADERS.putIfAbsent(type, new ExtensionLoader<>(type));
            extensionLoader = (ExtensionLoader<T>) EXTENSION_LOADERS.get(type);
        }
        return extensionLoader;
    }

    //ExtensionLoader.getExtensionLoader(ServiceDiscovery.class).getExtension("zk");
    public T getExtension(String name) {
        if (StringUtil.isBlank(name)) {
            throw new IllegalArgumentException("Extension name should not be null or empty.");
        }
        // firstly get from cache, if not hit, create one
        //Holder<zk>, 存储了具体的实现类
        Holder<Object> objectHolder = cachedInstance.get(name);
        if (objectHolder == null) {
            //create a objectHolder
            cachedInstance.putIfAbsent(name, new Holder<>());
            objectHolder = cachedInstance.get(name);
        }
        Object instance = objectHolder.get();
        //DCL单例模式创建
        if (instance == null) {
            synchronized (objectHolder) {
                instance = objectHolder.get();
                if (instance == null) {
                    //create an instance and put it into objectHolder
                    instance = createExtension(name);
                    objectHolder.set(instance);
                }
            }
        }
        return (T)instance;
    }

    //创建出具体的实现类
    //this.serviceDiscovery = ExtensionLoader.getExtensionLoader(ServiceDiscovery.class).getExtension("zk");
    //此时具体的注册中心就是zk注册中心
    private T createExtension(String name) {
        Class<?> clazz = getExtensionClasses().get(name);
        if (clazz == null) {
            throw new RuntimeException("No such extension of name " + name);
        }
        T instance = (T) EXTENSION_INSTANCE.get(clazz);
        if (instance == null) {
            try {
                synchronized (EXTENSION_INSTANCE) {
                    instance = (T) EXTENSION_INSTANCE.get(clazz);
                    if (instance == null) {
                        EXTENSION_INSTANCE.putIfAbsent(clazz, clazz.newInstance());
                        instance = (T) EXTENSION_INSTANCE.get(clazz);
                    }
                }
            } catch (InstantiationException | IllegalAccessException e) {
                throw new RuntimeException(e);
            }
        }
        return instance;
    }

    //辅助方法，获得cachedClasses，如果没有初始化就创建，如果初始化了就直接返回
    private Map<String, Class<?>> getExtensionClasses() {
        Map<String, Class<?>> stringClassMap = cachedClasses.get();
        if (stringClassMap == null) {
            synchronized (cachedClasses) {
                stringClassMap = cachedClasses.get();
                if (stringClassMap == null) {
                    stringClassMap = new ConcurrentHashMap<>();
                    //load classes
                    loadDirectory(stringClassMap);
                    cachedClasses.set(stringClassMap);
                }
            }
        }
        return stringClassMap;
    }

    //辅助方法，在初次加载一个具体的接口时候例如注册中心接口，就会调用该方法，将他们存入cachedClasses中
    //加载一个具体的接口，META-INF/extensions/下面的文件，例如：com.miyuki.serialize.Serializer
    //将他们保存到cachedClasses中，例如kyro，com.yf.serialize.kyro.KyroSerializer
    private void loadDirectory(Map<String, Class<?>> stringClassMap) {

        String fileName = ExtensionLoader.SERVICE_DICTORY + type.getName();

        ClassLoader classLoader = ExtensionLoader.class.getClassLoader();
        Enumeration<URL> resources = null;
        try {
            resources = classLoader.getResources(fileName);
            //加载文件中的每条记录
            if (resources != null) {
                while (resources.hasMoreElements()) {
                    URL url = resources.nextElement();
                    loadResource(stringClassMap, url, classLoader);
                }
            }
        } catch (IOException e) {
            throw  new RuntimeException(e);
        }
    }

    private void loadResource(Map<String, Class<?>> stringClassMap, URL url, ClassLoader classLoader) {
        try {
            BufferedReader bufferedReader = new BufferedReader(new InputStreamReader(url.openStream(), StandardCharsets.UTF_8));
            String line;
            while ((line = bufferedReader.readLine()) != null) {
                //处理字符串格式的每条记录
                final int ci = line.indexOf("#");
                if (ci > 0) {
                    line = line.substring(0, ci);
                }
                line = line.trim();

                if (line.length() > 0) {
                    try {
                        final int ei = line.indexOf('=');
                        String name = line.substring(0, ei).trim();
                        String clazzName = line.substring(ei + 1).trim();
                        Class<?> clazz = classLoader.loadClass(clazzName);
                        stringClassMap.putIfAbsent(name, clazz);
                    } catch (ClassNotFoundException e) {
                        throw new RuntimeException(e);
                    }
                }
            }
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }
}
