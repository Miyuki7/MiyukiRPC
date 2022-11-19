package com.miyuki.mrpc.core.config;

import com.miyuki.mrpc.core.common.utils.IpUtil;
import lombok.*;

import java.net.InetSocketAddress;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

/**
 * Created with IntelliJ IDEA.
 *
 * @Author: miyuki
 * @Date: 2022/11
 * @Description:
 */
@AllArgsConstructor
@NoArgsConstructor
@Getter
@Setter
@Builder
@ToString
public class RpcServiceConfig {
    /**
     * service version
     */
    private String version = "";
    /**
     * when the interface has multiple implementation classes, distinguish by group
     */
    private String group = "";

    /**
     * target service
     */
    private Object service;
    /**
     * 服务token
     */
    private String token;

    /**
     * 灰度发布
     */
    private Set<String> permitIps = new HashSet<>();

    //直连ip
    private List<String> directIp;

    public void addPermit(String inetAddress) {
        if (IpUtil.isValidIp(inetAddress)) {
            permitIps.add(inetAddress);
        } else {
            throw new IllegalArgumentException("invalid ip address.");
        }
    }

    public void removePermit(InetSocketAddress inetSocketAddress){
        if (inetSocketAddress != null){
            permitIps.remove(inetSocketAddress);
        }
    }

    public void clearPermit(){
        permitIps.clear();
    }

    public String getRpcServiceName() {
        return this.getServiceName() + this.getGroup() + this.getVersion();
    }

    public String getServiceName() {
        return this.service.getClass().getInterfaces()[0].getCanonicalName();
    }
}
