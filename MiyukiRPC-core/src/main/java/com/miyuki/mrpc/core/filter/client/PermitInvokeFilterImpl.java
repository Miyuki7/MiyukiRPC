package com.miyuki.mrpc.core.filter.client;

import com.miyuki.mrpc.core.config.RpcServiceConfig;
import com.miyuki.mrpc.core.filter.ClientFilter;
import com.miyuki.mrpc.core.register.zk.CuratorUtils;
import com.miyuki.mrpc.core.register.zk.ProviderNodeInfo;
import org.apache.curator.framework.CuratorFramework;

import java.net.InetAddress;
import java.net.UnknownHostException;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Set;

/**
 * Created with IntelliJ IDEA.
 *
 * @Author: miyuki
 * @Date: 2022/11
 * @Description:
 */
public class PermitInvokeFilterImpl implements ClientFilter {

    @Override
    public void doFilter(List<ProviderNodeInfo> providerNodeInfoList, RpcServiceConfig rpcServiceConfig) {
        CuratorFramework zkClient = CuratorUtils.getZkClient();
        Iterator<ProviderNodeInfo> iterator = providerNodeInfoList.iterator();
        while (iterator.hasNext()){
            ProviderNodeInfo providerNodeInfo = iterator.next();
            Set<String> servicePermitSet = new HashSet<>(CuratorUtils.getPermitList(zkClient, providerNodeInfo.getRpcServiceName(), providerNodeInfo.getServiceAddr()));
            try {
                String hostAddress = InetAddress.getLocalHost().getHostAddress();
                if (!(servicePermitSet == null || servicePermitSet.isEmpty() || servicePermitSet.contains(hostAddress))){
                    iterator.remove();
                }
            } catch (UnknownHostException e) {
                throw new RuntimeException(e);
            }
        }
    }
}
