package com.miyuki.mrpc.core.filter.client;

import com.miyuki.mrpc.core.config.RpcServiceConfig;
import com.miyuki.mrpc.core.filter.ClientFilter;
import com.miyuki.mrpc.core.register.zk.ProviderNodeInfo;

import java.util.ArrayList;
import java.util.List;

/**
 * Created with IntelliJ IDEA.
 *
 * @Author: miyuki
 * @Date: 2022/11
 * @Description: 客户端过滤器脸
 */
public class ClientFilterChain {
    private static List<ClientFilter> clientFilterList = new ArrayList<>();

    public void addClientFilter(ClientFilter clientFilter) {
        clientFilterList.add(clientFilter);
    }

    public void doFilter(List<ProviderNodeInfo> providerNodeInfoList, RpcServiceConfig rpcServiceConfig) {
        clientFilterList.forEach(clientFilter -> {
            clientFilter.doFilter(providerNodeInfoList,rpcServiceConfig);
        });
    }
}
