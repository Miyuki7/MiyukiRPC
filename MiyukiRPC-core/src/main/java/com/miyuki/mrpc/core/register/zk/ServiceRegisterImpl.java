package com.miyuki.mrpc.core.register.zk;

import com.miyuki.mrpc.core.config.RpcServiceConfig;
import com.miyuki.mrpc.core.register.ServiceRegistry;
import com.miyuki.mrpc.core.remoting.constants.RpcConstants;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.ToString;
import org.apache.curator.framework.CuratorFramework;

import java.net.InetSocketAddress;
import java.util.Set;

/**
 * Created with IntelliJ IDEA.
 *
 * @Author: miyuki
 * @Date: 2022/11
 * @Description:
 */
public class ServiceRegisterImpl implements ServiceRegistry {
    @Override
    public void registerService(InetSocketAddress inetSocketAddress, RpcServiceConfig rpcServiceConfig) {
        String rpcServiceName = rpcServiceConfig.getRpcServiceName();
        String servicePath = CuratorUtils.ZK_REGISTER_ROOT_PATH + "/" + rpcServiceName + inetSocketAddress.toString();
        CuratorFramework zkClient = CuratorUtils.getZkClient();
        CuratorUtils.createPersistentNode(zkClient,servicePath, RpcConstants.SERVICE_NODE);

// 灰度发布，只允许这几个 ip 调用 server 的服务
        Set<String> permitIps = rpcServiceConfig.getPermitIps();
        if (permitIps == null || permitIps.isEmpty()){
            return;
        }
        permitIps.forEach(permitIp -> {
            String permitPath = servicePath + "/permit/" + permitIp;
            CuratorUtils.createPersistentNode(zkClient,permitPath,RpcConstants.PERMIT_NODE);
        });
    }
}
