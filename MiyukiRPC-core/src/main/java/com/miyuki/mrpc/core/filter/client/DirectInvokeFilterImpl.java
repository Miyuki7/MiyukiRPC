package com.miyuki.mrpc.core.filter.client;

import com.miyuki.mrpc.core.common.utils.IpUtil;
import com.miyuki.mrpc.core.config.RpcServiceConfig;
import com.miyuki.mrpc.core.filter.ClientFilter;
import com.miyuki.mrpc.core.register.zk.ProviderNodeInfo;

import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Set;

/**
 * Created with IntelliJ IDEA.
 *
 * @Author: miyuki
 * @Date: 2022/11
 * @Description: 直连过滤器
 */
public class DirectInvokeFilterImpl implements ClientFilter {
    @Override
    public void doFilter(List<ProviderNodeInfo> providerNodeInfoList, RpcServiceConfig rpcServiceConfig) {
        List<String> directIps = rpcServiceConfig.getDirectIp();
        if (directIps == null || directIps.isEmpty()){
            return;
        }

        Set<String> directIpSet = new HashSet<>(directIps);
        Iterator<ProviderNodeInfo> iterator = providerNodeInfoList.iterator();
        while (iterator.hasNext()){
            ProviderNodeInfo providerNodeInfo = iterator.next();
            String serviceAddr = providerNodeInfo.getServiceAddr();
            String serviceIp = serviceAddr.split(":")[0];
            if (!IpUtil.isValidIp(serviceIp)){
                throw new IllegalArgumentException("必须为 Ip 格式");
            }
            if (!directIpSet.contains(serviceIp)){
                iterator.remove();
            }
        }
    }
}
