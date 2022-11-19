package com.miyuki.mrpc.core.provider;

import com.miyuki.mrpc.core.config.RpcServiceConfig;

/**
 * Created with IntelliJ IDEA.
 *
 * @Author: miyuki
 * @Date: 2022/11
 * @Description: 存储并且提供服务
 */
public interface ServiceProvider {

    void addService(RpcServiceConfig rpcServiceConfig);

    Object getService(String rpcServiceName);

    void publishService(RpcServiceConfig rpcServiceConfig);
}
