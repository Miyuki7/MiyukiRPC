package com.miyuki.mrpc.core.config;

import lombok.Getter;
import lombok.Setter;

/**
 * Created with IntelliJ IDEA.
 *
 * @Author: miyuki
 * @Date: 2022/11
 * @Description: 客户端配置，从配置文件加载
 */
@Getter
@Setter
public class ClientConfig {
    private String applicationName;

    private String registerAddr;

    private String registerType;

    /**
     * 代理类型 example: jdk,javassist
     */
    private String proxyType;

    /**
     * 负载均衡策略 example:random,rotate
     */
    private String routerStrategy;

    /**
     * 客户端序列化方式 example: hession2,kryo,jdk,fastjson
     */
    private String clientSerialize;

    /**
     * 客户端发数据的超时时间
     */
    private Integer timeOut;
}
