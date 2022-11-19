package com.miyuki.mrpc.core.provider.impl;

import com.miyuki.mrpc.core.config.RpcServiceConfig;
import com.miyuki.mrpc.core.provider.ServiceProvider;
import lombok.extern.slf4j.Slf4j;

import javax.imageio.spi.ServiceRegistry;
import java.util.Map;
import java.util.Set;

/**
 * Created with IntelliJ IDEA.
 *
 * @Author: miyuki
 * @Date: 2022/11
 * @Description: 基于zookeeper实现的provider
 */
@Slf4j
public class ZkServiceProvider implements ServiceProvider {

    private final Map<String, Object> serviceMap;

    private final Set<String> registeredService;

    private final ServiceRegistry serviceRegistry;

    @Override
    public void addService(RpcServiceConfig rpcServiceConfig) {

    }

    @Override
    public Object getService(String rpcServiceName) {
        return null;
    }

    @Override
    public void publishService(RpcServiceConfig rpcServiceConfig) {

    }
}
