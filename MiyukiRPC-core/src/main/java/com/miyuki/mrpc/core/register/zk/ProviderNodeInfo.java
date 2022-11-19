package com.miyuki.mrpc.core.register.zk;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.ToString;

import java.util.Objects;

/**
 * Created with IntelliJ IDEA.
 *
 * @Author: miyuki
 * @Date: 2022/11
 * @Description:
 */
@Data
@AllArgsConstructor
@Builder
@ToString
public class ProviderNodeInfo {
    private String rpcServiceName;
    private String serviceAddr;
    private Integer weight;

    @Override
    public boolean equals(Object obj) {
        if (obj instanceof ProviderNodeInfo) {
            ProviderNodeInfo objProvider = (ProviderNodeInfo) obj;
            return this.rpcServiceName.equals(objProvider.getRpcServiceName()) && this.getServiceAddr().equals(objProvider.getServiceAddr());
        }
        return false;
    }

    @Override
    public int hashCode() {
        return rpcServiceName.hashCode() + serviceAddr.hashCode();
    }
}
