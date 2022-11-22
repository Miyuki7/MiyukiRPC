package com.miyuki.mrpc.core.filter.server;

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
 * @Description: 在执行方法后释放semaphore
 */
@Slf4j
public class ServerServiceAfterLimitFilterImpl implements ServerFilter {


    private NettyRpcServer nettyRpcServer;

    @Override
    public void doFilter(RpcRequest rpcRequest) {
        if (null == rpcRequest){
            return;
        }
        if (nettyRpcServer == null){
            nettyRpcServer = SpringBeanFactory.getBean(NettyRpcServer.class);
        }

        try {
            String serviceName = rpcRequest.getRpcServiceName();
            Map<String, SemaphoreHolder> semaphoreHolderMap = nettyRpcServer.getSemaphoreHolderMap();
            SemaphoreHolder semaphoreHolder = semaphoreHolderMap.get(serviceName);
            Semaphore semaphore = semaphoreHolder.getSemaphore();
            semaphore.release();
//            System.out.println("release semaphore:" + semaphore);
        } catch (Exception e){
            log.error(e.getMessage());
            throw new IllegalStateException();
        }
    }
}
