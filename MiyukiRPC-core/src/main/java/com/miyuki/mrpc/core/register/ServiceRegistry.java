package com.miyuki.mrpc.core.register;

import com.miyuki.mrpc.core.config.RpcServiceConfig;

import java.net.InetSocketAddress;

/**
 * Created with IntelliJ IDEA.
 *
 * @Author: miyuki
 * @Date: 2022/11
 * @Description: 服务注册
 */
public interface ServiceRegistry {

    /**
     * register service
     *
     * @param rpcServiceConfig    rpcserviceConfig
     * @param inetSocketAddress service address
     */
    void registerService(InetSocketAddress inetSocketAddress, RpcServiceConfig rpcServiceConfig);
}
