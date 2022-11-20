package com.miyuki.mrpc.core.filter.server;

import com.miyuki.mrpc.core.common.enums.RpcErrorMessageEnum;
import com.miyuki.mrpc.core.common.exception.RpcException;
import com.miyuki.mrpc.core.common.factory.SpringBeanFactory;
import com.miyuki.mrpc.core.common.utils.StringUtil;
import com.miyuki.mrpc.core.filter.ServerFilter;
import com.miyuki.mrpc.core.remoting.dto.RpcRequest;
import com.miyuki.mrpc.core.remoting.transport.netty.server.Server;

/**
 * Created with IntelliJ IDEA.
 *
 * @Author: miyuki
 * @Date: 2022/11
 * @Description: token检查
 */
public class ServerTokenFilterImpl implements ServerFilter {

    private Server nettyRpcServer;

    @Override
    public void doFilter(RpcRequest rpcRequest) {
        if (nettyRpcServer == null){
            nettyRpcServer = SpringBeanFactory.getBean(Server.class);
        }


        String token = rpcRequest.getToken();
        String matchToken = nettyRpcServer.getServiceToken(rpcRequest.getRpcServiceName());
//        System.out.println("token:" + token);
        if (StringUtil.isEmpty(matchToken)){
            return;
        }

        if (!StringUtil.isEmpty(token) && token.equals(matchToken)){
            return;
        }else {
            throw new RpcException(RpcErrorMessageEnum.TOKEN_NOT_MATCH);
        }
    }
}
