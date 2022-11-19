package com.miyuki.mrpc.core.register;

import com.miyuki.mrpc.core.config.RpcServiceConfig;
import com.miyuki.mrpc.core.remoting.dto.RpcRequest;

import java.net.InetSocketAddress;

/**
 * Created with IntelliJ IDEA.
 *
 * @Author: miyuki
 * @Date: 2022/11
 * @Description: 服务发现
 */
public interface ServiceDiscovery {
    //通过服务名称来寻找服务
    public InetSocketAddress lookupService(RpcRequest rpcRequest, RpcServiceConfig rpcServiceConfig);
}
