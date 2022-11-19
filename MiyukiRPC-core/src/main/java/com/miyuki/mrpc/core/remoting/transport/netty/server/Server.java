package com.miyuki.mrpc.core.remoting.transport.netty.server;
import com.miyuki.mrpc.core.provider.ServiceProvider;
import org.springframework.stereotype.Component;
import com.miyuki.mrpc.core.config.ServerConfig;
import io.netty.bootstrap.ServerBootstrap;
import io.netty.channel.ChannelInitializer;
import io.netty.channel.ChannelOption;
import io.netty.channel.EventLoopGroup;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.SocketChannel;
import io.netty.channel.socket.nio.NioServerSocketChannel;
import lombok.extern.slf4j.Slf4j;

/**
 * Created with IntelliJ IDEA.
 *
 * @Author: miyuki
 * @Date: 2022/11
 * @Description: netty客户端
 */
@Slf4j
@Component
public class Server {
    public static final int PORT = 9998;
//    private final ServiceProvider
}
