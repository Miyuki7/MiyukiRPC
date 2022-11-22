package com.miyuki.mrpc.core.filter.server;

import com.miyuki.mrpc.core.common.enums.RpcErrorMessageEnum;
import com.miyuki.mrpc.core.common.exception.RpcException;
import com.miyuki.mrpc.core.common.factory.SpringBeanFactory;
import com.miyuki.mrpc.core.common.semaphore.SemaphoreHolder;
import com.miyuki.mrpc.core.filter.ServerFilter;
import com.miyuki.mrpc.core.remoting.dto.RpcRequest;
import com.miyuki.mrpc.core.remoting.transport.netty.server.NettyRpcServer;
import lombok.extern.slf4j.Slf4j;

import java.util.Map;
import java.util.concurrent.Semaphore;

/**
 * Created with IntelliJ IDEA.
 *
 * @Author: miyuki
 * @Date: 2022/11
 * @Description: 在真正调用方法前先检查是否可以调用，检查信号量是否足够
 */
@Slf4j
public class ServerServiceBeforeLimitFilterImpl implements ServerFilter {

    private NettyRpcServer nettyRpcServer;

    @Override
    public void doFilter(RpcRequest rpcRequest) {
        if (nettyRpcServer == null){
            nettyRpcServer = SpringBeanFactory.getBean(NettyRpcServer.class);
        }
        String serviceName = rpcRequest.getRpcServiceName();

        Map<String, SemaphoreHolder> semaphoreHolderMap = nettyRpcServer.getSemaphoreHolderMap();
        SemaphoreHolder semaphoreHolder = semaphoreHolderMap.get(serviceName);
        Semaphore semaphore = semaphoreHolder.getSemaphore();

//        System.out.println("get semaphore:" + semaphore);
        if (!semaphore.tryAcquire()){
            log.error("[ServerServiceBeforeLimitFilterImpl] {}'s max request is {},reject now", rpcRequest.getRpcServiceName(), semaphoreHolder.getMaxNums());
            throw new RpcException(RpcErrorMessageEnum.MaxServiceLimitRequestException);
        }
    }
}
