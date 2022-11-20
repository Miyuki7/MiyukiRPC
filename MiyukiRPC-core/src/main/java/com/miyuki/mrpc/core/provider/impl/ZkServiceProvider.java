package com.miyuki.mrpc.core.provider.impl;

import com.miyuki.mrpc.core.common.enums.RpcErrorMessageEnum;
import com.miyuki.mrpc.core.common.exception.RpcException;
import com.miyuki.mrpc.core.common.extension.ExtensionLoader;
import com.miyuki.mrpc.core.config.RpcServiceConfig;
import com.miyuki.mrpc.core.provider.ServiceProvider;
import com.miyuki.mrpc.core.register.ServiceRegistry;
import com.miyuki.mrpc.core.register.zk.ServiceRegisterImpl;
import lombok.extern.slf4j.Slf4j;

import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.net.UnknownHostException;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

import static com.miyuki.mrpc.core.common.cache.CommonServerCache.SERVER_CONFIG;

/**
 * Created with IntelliJ IDEA.
 *
 * @Author: miyuki
 * @Date: 2022/11
 * @Description: 基于zookeeper实现的provider
 */
@Slf4j
public class ZkServiceProvider implements ServiceProvider {
    private final Map<String,Object> serviceMap;
    private final Set<String> registeredService;
    private final ServiceRegistry serviceRegistry;

    private ZkServiceProvider(){
        serviceMap = new ConcurrentHashMap<>();
        registeredService = ConcurrentHashMap.newKeySet();
        serviceRegistry = ExtensionLoader.getExtensionLoader(ServiceRegistry.class).getExtension(SERVER_CONFIG.getRegisterType());
    }

    @Override
    public void addService(RpcServiceConfig rpcServiceConfig) {
        String rpcServiceName = rpcServiceConfig.getRpcServiceName();
        if (registeredService.contains(rpcServiceName)) return;
        registeredService.add(rpcServiceName);
        System.out.println(Thread.currentThread().getName());
        serviceMap.put(rpcServiceName,rpcServiceConfig.getService());
        log.info("Add service: {} and interfaces:{}", rpcServiceName, rpcServiceConfig.getService().getClass().getInterfaces());

    }

    @Override
    public Object getService(String rpcServiceName) {
        System.out.println(Thread.currentThread().getName());
        Object service = serviceMap.get(rpcServiceName);
        if (null == service){
            throw new RpcException(RpcErrorMessageEnum.SERVICE_CAN_NOT_BE_FOUND);
        }
        return service;
    }

    @Override
    public void publishService(RpcServiceConfig rpcServiceConfig) {
        try {
            String host = InetAddress.getLocalHost().getHostAddress();
            this.addService(rpcServiceConfig);
            serviceRegistry.registerService(new InetSocketAddress(host, SERVER_CONFIG.getServerPort()),rpcServiceConfig);

        } catch (UnknownHostException e) {
            log.error("occur exception when getHostAddress", e);
        }

    }
}
