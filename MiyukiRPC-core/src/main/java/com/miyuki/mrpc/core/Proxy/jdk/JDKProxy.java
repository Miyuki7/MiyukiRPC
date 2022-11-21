package com.miyuki.mrpc.core.Proxy.jdk;

import com.miyuki.mrpc.core.Proxy.ProxyFactory;
import com.miyuki.mrpc.core.common.enums.RpcErrorMessageEnum;
import com.miyuki.mrpc.core.common.enums.RpcResponseCodeEnum;
import com.miyuki.mrpc.core.common.exception.RpcException;
import com.miyuki.mrpc.core.config.RpcServiceConfig;
import com.miyuki.mrpc.core.remoting.constants.RpcConstants;
import com.miyuki.mrpc.core.remoting.dto.RpcRequest;
import com.miyuki.mrpc.core.remoting.dto.RpcResponse;
import lombok.extern.slf4j.Slf4j;

import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Method;
import java.lang.reflect.Proxy;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * Created with IntelliJ IDEA.
 *
 * @Author: miyuki
 * @Date: 2022/11
 * @Description:
 */
@Slf4j
public class JDKProxy implements InvocationHandler, ProxyFactory {
    private static final String INTERFACE_NAME = "interfaceName";
    private final RpcServiceConfig rpcServiceConfig;
    private final RpcRequestTransport rpcRequestTransport;

    private final AtomicInteger retryTime = new AtomicInteger(RpcConstants.MAX_RECONNECTION_TIMES);

    public RpcClientProxy(RpcServiceConfig rpcServiceConfig, RpcRequestTransport rpcRequestTransport){
        this.rpcRequestTransport = rpcRequestTransport;
        this.rpcServiceConfig = rpcServiceConfig;
    }


    /*
    * 获取代理对象
    **/
    public <T>T getProxy(Class<T> clazz){
        return (T) Proxy.newProxyInstance(clazz.getClassLoader(), new Class<?>[] {clazz}, this);
    }

    /*
    * 重写 invoke
    * 向 zookeeper 拉取服务列表
    * 给查询到的 server 发信息，获取方法执行结果
    **/
    @Override
    public Object invoke(Object proxy, Method method, Object[] args) throws Throwable {
        log.info("invoked method: [{}]", method.getName());
        RpcRequest rpcRequest = RpcRequest.builder().methodName(method.getName())
                .parameters(args)
                .paramTypes(method.getParameterTypes())
                .interfaceName(method.getDeclaringClass().getName())
                .group(rpcServiceConfig.getGroup())
                .version(rpcServiceConfig.getVersion())
                .requestId(UUID.randomUUID().toString())
                .token(rpcServiceConfig.getToken())
                .build();
        RpcResponse<Object> rpcResponse = null;

        CompletableFuture<Object> completableFuture = (CompletableFuture<Object>) rpcRequestTransport.sendRpcRequest(rpcRequest,rpcServiceConfig);
        rpcResponse = (RpcResponse<Object>) completableFuture.get();
        this.check(rpcResponse,rpcRequest);

        // 符合触发重试条件的异常(远程 Rpc 出现问题，并不是方法本身的问题)，触发重试
        int retry = retryTime.decrementAndGet();
        if (rpcResponse.getCode() == RpcResponseCodeEnum.RPC_FAIL.getCode() && retry > 0){
            return invoke(proxy, method, args);
        }else {
            retryTime.set(RpcConstants.MAX_RECONNECTION_TIMES);
        }
        return rpcResponse.getData();
    }

    private void check(RpcResponse<Object> rpcResponse, RpcRequest rpcRequest){
        if (rpcResponse == null){
            throw new RpcException(RpcErrorMessageEnum.SERVICE_INVOCATION_FAILURE);
        }
        if (! rpcRequest.getRequestId().equals(rpcResponse.getRequestId())){
            throw new RpcException(RpcErrorMessageEnum.REQUEST_NOT_MATCH_RESPONSE);
        }
        if (rpcResponse.getCode() == null || !rpcResponse.getCode().equals(RpcResponseCodeEnum.SUCCESS.getCode())){
            throw new RpcException(RpcErrorMessageEnum.SERVICE_INVOCATION_FAILURE, INTERFACE_NAME + ":" + rpcRequest.getInterfaceName());
        }
    }
}
