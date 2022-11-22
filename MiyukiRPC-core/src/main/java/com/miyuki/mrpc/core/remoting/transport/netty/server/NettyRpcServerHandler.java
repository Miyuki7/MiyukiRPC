package com.miyuki.mrpc.core.remoting.transport.netty.server;

import com.miyuki.mrpc.core.common.enums.RpcErrorMessageEnum;
import com.miyuki.mrpc.core.common.enums.RpcResponseCodeEnum;
import com.miyuki.mrpc.core.common.exception.RpcException;
import com.miyuki.mrpc.core.common.extension.ExtensionLoader;
import com.miyuki.mrpc.core.common.factory.SingletonFactory;
import com.miyuki.mrpc.core.filter.ServerFilter;
import com.miyuki.mrpc.core.filter.server.ServerAfterFilterChain;
import com.miyuki.mrpc.core.filter.server.ServerBeforeFilterChain;
import com.miyuki.mrpc.core.filter.server.ServerFilterChain;
import com.miyuki.mrpc.core.filter.server.ServerTokenFilterImpl;
import com.miyuki.mrpc.core.remoting.constants.RpcConstants;
import com.miyuki.mrpc.core.remoting.dto.RpcProtocol;
import com.miyuki.mrpc.core.remoting.dto.RpcRequest;
import com.miyuki.mrpc.core.remoting.dto.RpcResponse;
import com.miyuki.mrpc.core.remoting.handler.RpcRequestHandler;
import io.netty.channel.ChannelFutureListener;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelInboundHandlerAdapter;
import io.netty.handler.timeout.IdleState;
import io.netty.handler.timeout.IdleStateEvent;
import io.netty.util.ReferenceCountUtil;
import lombok.extern.slf4j.Slf4j;

/**
 * Created with IntelliJ IDEA.
 *
 * @Author: miyuki
 * @Date: 2022/11
 * @Description:
 */
@Slf4j
public class NettyRpcServerHandler extends ChannelInboundHandlerAdapter {
    private final RpcRequestHandler rpcRequestHandler; // 负责在 server 端执行方法

    private final ServerFilterChain serverBeforeFilterChain;
    private final ServerFilterChain serverAfterFilterChain;
    public NettyRpcServerHandler() {
        this.rpcRequestHandler = SingletonFactory.getInstance(RpcRequestHandler.class);

        serverBeforeFilterChain = SingletonFactory.getInstance(ServerBeforeFilterChain.class);
        serverAfterFilterChain = SingletonFactory.getInstance(ServerAfterFilterChain.class);

        ServerTokenFilterImpl serverTokenFilter = (ServerTokenFilterImpl) ExtensionLoader.getExtensionLoader(ServerFilter.class).getExtension("serverTokenFilter");
        ServerFilter serverServiceBeforeLimitFilterImpl = ExtensionLoader.getExtensionLoader(ServerFilter.class).getExtension("serverServiceBeforeLimitFilterImpl");
        serverBeforeFilterChain.addServerFilter(serverTokenFilter);
        serverBeforeFilterChain.addServerFilter(serverServiceBeforeLimitFilterImpl);

        ServerFilter serverServiceAfterLimitFilterImpl = ExtensionLoader.getExtensionLoader(ServerFilter.class).getExtension("serverServiceAfterLimitFilterImpl");
        serverAfterFilterChain.addServerFilter(serverServiceAfterLimitFilterImpl);
    }

    @Override
    public void channelRead(ChannelHandlerContext ctx, Object msg) {
        RpcRequest rpcRequest = null;
        try {
            if (msg instanceof RpcProtocol){
                log.info("server receive msg: [{}] ", msg);

                RpcProtocol recivedMessage = (RpcProtocol) msg;
                RpcProtocol rpcMessage = new RpcProtocol();

                rpcMessage.setCodec(recivedMessage.getCodec());
                rpcMessage.setCompress(recivedMessage.getCompress());
                rpcMessage.setRequestId(recivedMessage.getRequestId());

                byte messageType = recivedMessage.getMessageType();
                if (messageType == RpcConstants.HEARTBEAT_REQUEST_TYPE){
                    System.out.println("receive heart request !");
                    rpcMessage.setMessageType(RpcConstants.HEARTBEAT_RESPONSE_TYPE);
                    rpcMessage.setData(RpcConstants.PONG);
                }else {
                    rpcRequest = (RpcRequest) recivedMessage.getData();
                    // 前置过滤链,获取锁，必须要接释放锁
                    try {
                        serverBeforeFilterChain.doFilter(rpcRequest);
                    }catch (Exception cause){
                        if (cause instanceof RpcException){
                            // 构造异常信息
                            RpcResponse<Object> rpcResponse = RpcResponse.fail(RpcResponseCodeEnum.FAIL_TOKEN_ILLEGAL);
                            rpcMessage.setData(rpcResponse);
                            ctx.writeAndFlush(rpcMessage).addListener(ChannelFutureListener.CLOSE_ON_FAILURE);
                        }
                        throw new RpcException(RpcErrorMessageEnum.TOKEN_NOT_MATCH);
                    }

                    Object result = rpcRequestHandler.handle(rpcRequest);
                    if (null != result)
                    {
                        log.info(String.format("server get result: %s", result.toString()));
                    }else {
                        log.info("server get result !");
                    }
                    rpcMessage.setMessageType(RpcConstants.RESPONSE_TYPE);

                    if (ctx.channel().isActive() && ctx.channel().isWritable()){
                        RpcResponse<Object> rpcResponse = RpcResponse.success(result,rpcRequest.getRequestId());
                        rpcMessage.setData(rpcResponse);
                    }else {
                        RpcResponse<Object> rpcResponse = RpcResponse.fail(RpcResponseCodeEnum.FAIL);
                        rpcMessage.setData(rpcResponse);
                    }
                }
                ctx.writeAndFlush(rpcMessage).addListener(ChannelFutureListener.CLOSE_ON_FAILURE);
            }
        } finally {
            serverAfterFilterChain.doFilter(rpcRequest);
            ReferenceCountUtil.release(msg);
        }

    }


    @Override
    public void userEventTriggered(ChannelHandlerContext ctx, Object evt) throws Exception {
        try {
            if (evt instanceof IdleStateEvent){
                IdleState state = ((IdleStateEvent) evt).state();
                if (state == IdleState.READER_IDLE){
                    log.info("idle check happen, so close the connection");
                    ctx.close();
                }
            }else {
                super.userEventTriggered(ctx, evt);
            }
        } finally {
            super.userEventTriggered(ctx, evt);
        }
    }
}
