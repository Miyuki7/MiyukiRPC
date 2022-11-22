package com.miyuki.mrpc.core.remoting.transport.netty.server;
import com.miyuki.mrpc.core.common.factory.SingletonFactory;
import com.miyuki.mrpc.core.common.semaphore.SemaphoreHolder;
import com.miyuki.mrpc.core.common.utils.RuntimeUtil;
import com.miyuki.mrpc.core.common.utils.StringUtil;
import com.miyuki.mrpc.core.common.utils.concurrent.threadpool.ThreadPoolFactoryUtil;
import com.miyuki.mrpc.core.config.RpcServiceConfig;
import com.miyuki.mrpc.core.provider.ServiceProvider;
import com.miyuki.mrpc.core.provider.impl.ZkServiceProvider;
import com.miyuki.mrpc.core.remoting.constants.RpcConstants;
import io.netty.channel.*;
import io.netty.handler.logging.LogLevel;
import io.netty.handler.logging.LoggingHandler;
import io.netty.handler.timeout.IdleStateHandler;
import io.netty.util.concurrent.DefaultEventExecutorGroup;
import org.springframework.stereotype.Component;
import com.miyuki.mrpc.core.config.ServerConfig;
import io.netty.bootstrap.ServerBootstrap;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.SocketChannel;
import io.netty.channel.socket.nio.NioServerSocketChannel;
import lombok.extern.slf4j.Slf4j;
import com.miyuki.mrpc.core.remoting.transport.netty.codec.*;
import java.net.InetAddress;
import java.net.UnknownHostException;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.TimeUnit;

/**
 * Created with IntelliJ IDEA.
 *
 * @Author: miyuki
 * @Date: 2022/11
 * @Description: netty客户端
 */
@Slf4j
@Component
public class NettyRpcServer {
    public static final int PORT = 9998;

    private final ServiceProvider serviceProvider = SingletonFactory.getInstance(ZkServiceProvider.class);

    private final Map<String, SemaphoreHolder> semaphoreHolderMap = new ConcurrentHashMap<>();

    private static Map<String, String> TOKEN_MAP = new ConcurrentHashMap<>();

    private NettyRpcServer(){

    }

    public Map<String, SemaphoreHolder> getSemaphoreHolderMap(){
        return this.semaphoreHolderMap;
    }

    public void registerService(RpcServiceConfig rpcServiceConfig){
        serviceProvider.publishService(rpcServiceConfig);
        String serviceToken = rpcServiceConfig.getToken();
        semaphoreHolderMap.put(rpcServiceConfig.getRpcServiceName(),new SemaphoreHolder(RpcConstants.MAX_SEMAPHORE_NUMS));
        if (StringUtil.isEmpty(serviceToken)){
            return;
        }
        TOKEN_MAP.put(rpcServiceConfig.getRpcServiceName(),serviceToken);
    }

    public String getServiceToken(String serviceName){
        if (TOKEN_MAP.containsKey(serviceName)){
            return TOKEN_MAP.get(serviceName);
        }
        return null;
    }

    public void start() throws UnknownHostException {
        // TODO ： 清空原有服务

        String host = InetAddress.getLocalHost().getHostAddress();
        EventLoopGroup boss = new NioEventLoopGroup(1);
        EventLoopGroup worker = new NioEventLoopGroup(5);
        ServerBootstrap bootstrap = new ServerBootstrap();

        DefaultEventExecutorGroup serviceHandlerGroup = new DefaultEventExecutorGroup(RuntimeUtil.cpus() * 2,
                ThreadPoolFactoryUtil.createThreadFactory("service-handler-group",false));

        try {
            bootstrap.group(boss,worker)
                    .channel(NioServerSocketChannel.class)
                    // TCP默认开启了 Nagle 算法，该算法的作用是尽可能的发送大数据快，减少网络传输。TCP_NODELAY 参数的作用就是控制是否启用 Nagle 算法。
                    .childOption(ChannelOption.TCP_NODELAY,true)
                    // 是否开启 TCP 底层心跳机制
                    .childOption(ChannelOption.SO_KEEPALIVE,true)
                    //表示系统用于临时存放已完成三次握手的请求的队列的最大长度,如果连接建立频繁，服务器处理创建新连接较慢，可以适当调大这个参数
                    .option(ChannelOption.SO_BACKLOG, RpcConstants.BACKLOG) // 服务端限流
                    .handler(new LoggingHandler(LogLevel.INFO))
                    // 当客户端第一次进行请求的时候才会进行初始化
                    .childHandler(new ChannelInitializer<SocketChannel>() {
                        @Override
                        protected void initChannel(SocketChannel socketChannel) throws Exception {
                            ChannelPipeline pipeline = socketChannel.pipeline();
                            // 30 秒之内没有收到客户端请求的话就关闭连接
                            pipeline.addLast(new IdleStateHandler(30,0,0, TimeUnit.SECONDS));
                            pipeline.addLast(new RpcEncoder());
                            pipeline.addLast(new RpcDecoder());
                            pipeline.addLast(serviceHandlerGroup,new NettyRpcServerHandler());

                        }
                    });
            ChannelFuture future = bootstrap.bind(host,PORT).sync();
            future.channel().closeFuture().sync();
        } catch (Exception e){

        } finally {
            log.error("shutdown bossGroup and workerGroup");
            boss.shutdownGracefully();
            worker.shutdownGracefully();
//            serviceHandlerGroup.shutdownGracefully();
        }



    }

}
