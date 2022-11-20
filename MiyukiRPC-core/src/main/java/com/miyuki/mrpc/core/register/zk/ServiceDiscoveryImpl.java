package com.miyuki.mrpc.core.register.zk;

import com.miyuki.mrpc.core.common.enums.RpcErrorMessageEnum;
import com.miyuki.mrpc.core.common.exception.RpcException;
import com.miyuki.mrpc.core.common.extension.ExtensionLoader;
import com.miyuki.mrpc.core.common.factory.SingletonFactory;
import com.miyuki.mrpc.core.common.utils.StringUtil;
import com.miyuki.mrpc.core.config.RpcServiceConfig;
import com.miyuki.mrpc.core.filter.ClientFilter;
import com.miyuki.mrpc.core.filter.client.ClientFilterChain;
import com.miyuki.mrpc.core.filter.client.PermitInvokeFilterImpl;
import com.miyuki.mrpc.core.loadbalance.LoadBalance;
import com.miyuki.mrpc.core.register.ServiceDiscovery;
import com.miyuki.mrpc.core.remoting.dto.RpcRequest;
import org.apache.curator.framework.CuratorFramework;

import java.net.InetSocketAddress;
import java.util.ArrayList;
import java.util.List;

import static com.miyuki.mrpc.core.common.cache.CommonClientCache.CLIENT_CONFIG;
import static com.miyuki.mrpc.core.common.cache.CommonServerCache.SERVER_CONFIG;

/**
 * Created with IntelliJ IDEA.
 *
 * @Author: miyuki
 * @Date: 2022/11
 * @Description: 客户端使用的服务发现
 */
public class ServiceDiscoveryImpl implements ServiceDiscovery {
    private final LoadBalance balance;

    public ServiceDiscoveryImpl() {
        ExtensionLoader<LoadBalance> extensionLoader = ExtensionLoader.getExtensionLoader(LoadBalance.class);
        balance = extensionLoader.getExtension(CLIENT_CONFIG.getRouterStrategy());
    }
    @Override
    public InetSocketAddress lookupService(RpcRequest rpcRequest, RpcServiceConfig rpcServiceConfig) {
        String rpcServiceName = rpcRequest.getRpcServiceName();
        CuratorFramework zkClient = CuratorUtils.getZkClient();
        List<ProviderNodeInfo> serviceProviderList = new ArrayList<>(CuratorUtils.getChildrenNodes(zkClient, rpcServiceName));

        if (serviceProviderList.isEmpty()) {
            throw new RpcException(RpcErrorMessageEnum.SERVICE_CAN_NOT_BE_FOUND, rpcServiceName);
        } else {
            //客户端过滤器链
            ClientFilterChain clientFilterChain = SingletonFactory.getInstance(ClientFilterChain.class);
            ExtensionLoader<ClientFilter> extensionLoader = ExtensionLoader.getExtensionLoader(ClientFilter.class);
            PermitInvokeFilterImpl permitInvokeFilterImpl = (PermitInvokeFilterImpl) extensionLoader.getExtension("permitInvokeFilterImpl");

            extensionLoader = ExtensionLoader.getExtensionLoader(ClientFilter.class);
            ClientFilter directInvokeFilterImpl = extensionLoader.getExtension("directInvokeFilterImpl");

            clientFilterChain.addClientFilter(directInvokeFilterImpl);
            clientFilterChain.addClientFilter(permitInvokeFilterImpl);

            clientFilterChain.doFilter(serviceProviderList,rpcServiceConfig);
            //负载均衡，选择具体的路由
            String targetServiceUrl = balance.selectServiceAddress(serviceProviderList,rpcRequest);
            if (StringUtil.isEmpty(targetServiceUrl)){
                throw new RpcException(RpcErrorMessageEnum.SERVICE_CAN_NOT_BE_FOUND,rpcServiceName);
            }
            String[] socketAddressArray = targetServiceUrl.split(":");
            String host = socketAddressArray[0];
            int port = Integer.parseInt(socketAddressArray[1]);
            return new InetSocketAddress(host,port);
        }

    }
}
