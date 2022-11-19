package com.miyuki.mrpc.core.config;
import lombok.*;

/**
 * Created with IntelliJ IDEA.
 *
 * @Author: miyuki
 * @Date: 2022/11
 * @Description:
 */
@Getter
@Setter
public class ServerConfig {
    private Integer serverPort;

    private String registerAddr;

    private String registerType;

    private String applicationName;

    /**
     * 服务端序列化方式 example: hession2,kryo,jdk,fastjson
     */
    private String serverSerialize;

    /**
     * 服务端业务线程数目
     */
    private Integer serverBizThreadNums;

    /**
     * 服务端接收队列的大小
     */
    private Integer serverQueueSize;

    /**
     * 限制服务端最大所能接受的数据包体积
     */
    private Integer maxServerRequestData;

    /**
     * 服务端最大连接数
     */
    private Integer maxConnections;

}
