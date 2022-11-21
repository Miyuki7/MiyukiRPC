package com.miyuki.mrpc.core.remoting.transport.netty.client;

import com.miyuki.mrpc.core.common.enums.CompressTypeEnum;
import com.miyuki.mrpc.core.common.enums.SerializationTypeEnum;
import com.miyuki.mrpc.core.common.factory.SingletonFactory;
import com.miyuki.mrpc.core.remoting.constants.RpcConstants;
import com.miyuki.mrpc.core.remoting.dto.RpcProtocol;
import com.miyuki.mrpc.core.remoting.dto.RpcResponse;
import io.netty.channel.*;
import io.netty.handler.timeout.IdleState;
import io.netty.handler.timeout.IdleStateEvent;
import io.netty.util.ReferenceCountUtil;
import lombok.extern.slf4j.Slf4j;

import java.net.InetSocketAddress;

/**
 * Created with IntelliJ IDEA.
 *
 * @Author: miyuki
 * @Date: 2022/11
 * @Description:Customize the client ChannelHandler to process the data sent by the server
 *  *
 *  * <p>
 *  * 如果继承自 SimpleChannelInboundHandler 的话就不要考虑 ByteBuf 的释放 ，{@link SimpleChannelInboundHandler} 内部的
 *  * channelRead 方法会替你释放 ByteBuf ，避免可能导致的内存泄露问题。详见《Netty进阶之路 跟着案例学 Netty》
 */
@Slf4j
public class NettyRpcClientHandler extends ChannelInboundHandlerAdapter {
    private final UnprocessedRequests unprocessedRequests;
    private final NettyRpcClient nettyRpcClient;
    public NettyRpcClientHandler() {
        this.unprocessedRequests = SingletonFactory.getInstance(UnprocessedRequests.class);
        this.nettyRpcClient = SingletonFactory.getInstance(NettyRpcClient.class);
    }
    /**
     * Read the message transmitted by the server
     */
    @Override
    public void channelRead(ChannelHandlerContext ctx, Object msg) throws Exception {
        try {
            log.info("client receive msg: [{}]", msg);
            if (msg instanceof RpcProtocol) {
                RpcProtocol tmp = (RpcProtocol) msg;
                byte messageType = tmp.getMessageType();
                if (messageType == RpcConstants.HEARTBEAT_RESPONSE_TYPE) {
                    log.info("heart [{}]", tmp.getData());
                } else if (messageType == RpcConstants.RESPONSE_TYPE) {
                    RpcResponse<Object> rpcResponse = (RpcResponse<Object>) tmp.getData();
                    unprocessedRequests.complete(rpcResponse);
                }
            }

        } finally {
            ReferenceCountUtil.release(msg);
        }
    }

    //如果写通道处于空闲状态就发送心跳命令
    @Override
    public void userEventTriggered(ChannelHandlerContext ctx, Object evt) throws Exception {
        if (evt instanceof IdleStateEvent) {
            IdleState state = ((IdleStateEvent) evt).state();
            if (state == IdleState.WRITER_IDLE) {
                log.info("write idle happen [{}]", ctx.channel().remoteAddress());
                Channel channel = nettyRpcClient.getChannel((InetSocketAddress) ctx.channel().remoteAddress());

                RpcProtocol rpcMessage = RpcProtocol.builder()
                        .messageType(RpcConstants.HEARTBEAT_REQUEST_TYPE)
                        .codec(SerializationTypeEnum.KYRO.getCode())
                        .compress(CompressTypeEnum.GZIP.getCode())
                        .data(RpcConstants.PING)
                        .build();
                channel.writeAndFlush(rpcMessage).addListener(ChannelFutureListener.CLOSE_ON_FAILURE);

//                channel.writeAndFlush(rpcMessage).addListener((ChannelFutureListener) future -> {
//                    System.out.println("send success!");
//                });

            }
        }
    }

    @Override
    public void channelInactive(ChannelHandlerContext ctx) throws Exception {
        nettyRpcClient.doConnect((InetSocketAddress) ctx.channel().remoteAddress());
        super.channelInactive(ctx);
    }

    /**
     * Called when an exception occurs in processing a client message
     */
    @Override
    public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause) {
        log.error("client catch exception：", cause);
        cause.printStackTrace();
        ctx.close();
    }
}
