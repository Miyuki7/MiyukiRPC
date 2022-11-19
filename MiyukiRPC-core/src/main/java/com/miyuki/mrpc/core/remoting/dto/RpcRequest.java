package com.miyuki.mrpc.core.remoting.dto;

import lombok.*;

/**
 * Created with IntelliJ IDEA.
 *
 * @Author: miyuki
 * @Date: 2022/11
 * @Description: Rpc请求类
 */
@AllArgsConstructor
@NoArgsConstructor
@ToString
@Builder
@Getter
public class RpcRequest {
    private String requestId;
    private String interfaceName;
    private String methodName;
    private Object[] parameters;
    //版本号
    private String version;
    //参数
    private Class<?>[] paramTypes;
    //分组
    private String group;
    //token
    private String token;

    public String getRpcServiceName() {
        return this.getInterfaceName() + this.getGroup() + this.version;
    }
}
