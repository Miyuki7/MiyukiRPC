package com.miyuki.mrpc.core.remoting.transport.netty.client;

import io.netty.channel.Channel;
import lombok.extern.slf4j.Slf4j;

import java.net.InetSocketAddress;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Created with IntelliJ IDEA.
 *
 * @Author: miyuki
 * @Date: 2022/11
 * @Description: store and get Channel object
 */
@Slf4j
public class ChannelProvider {
    private Map<InetSocketAddress, Channel> channelMap;

    private ChannelProvider(){
        channelMap = new ConcurrentHashMap<>();
    }

    public Channel getChannel(InetSocketAddress inetSocketAddress){
        if (channelMap.containsKey(inetSocketAddress)){
            Channel channel = channelMap.get(inetSocketAddress);
            if (channel != null && channel.isActive()){
                return channel;
            }else {
                channelMap.remove(inetSocketAddress);
            }
        }
        return null;
    }

    public void setChannel(InetSocketAddress inetSocketAddress,Channel channel){
        channelMap.put(inetSocketAddress,channel);
    }

    public void remove(InetSocketAddress inetSocketAddress){
        channelMap.remove(inetSocketAddress);
        log.info("Channel map size :[{}]",channelMap.size());
    }
}
