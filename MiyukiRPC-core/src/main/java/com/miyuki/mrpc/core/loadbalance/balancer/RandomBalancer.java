package com.miyuki.mrpc.core.loadbalance.balancer;

import com.miyuki.mrpc.core.loadbalance.AbstractLoadBalance;
import com.miyuki.mrpc.core.register.zk.ProviderNodeInfo;
import com.miyuki.mrpc.core.remoting.dto.RpcRequest;

import java.util.List;
import java.util.Random;

/**
 * Created with IntelliJ IDEA.
 *
 * @Author: miyuki
 * @Date: 2022/11
 * @Description:
 */
public class RandomBalancer extends AbstractLoadBalance {
    @Override
    protected String doSelect(List<ProviderNodeInfo> serviceUrlList, RpcRequest rpcRequest) {
        Random random = new Random();
        return serviceUrlList.get(random.nextInt(serviceUrlList.size())).getServiceAddr();
    }
}
