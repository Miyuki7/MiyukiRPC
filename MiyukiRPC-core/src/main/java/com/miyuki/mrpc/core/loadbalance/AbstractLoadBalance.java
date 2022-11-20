package com.miyuki.mrpc.core.loadbalance;

import com.miyuki.mrpc.core.register.zk.ProviderNodeInfo;
import com.miyuki.mrpc.core.remoting.dto.RpcRequest;

import java.util.List;

/**
 * Created with IntelliJ IDEA.
 *
 * @Author: miyuki
 * @Date: 2022/11
 * @Description:
 */
public abstract class AbstractLoadBalance implements LoadBalance{

    @Override
    public String selectServiceAddress(List<ProviderNodeInfo> serviceUrlList, RpcRequest rpcRequest) {
        // 进行一个简单的初筛
        if (serviceUrlList == null || serviceUrlList.isEmpty()){
            return null;
        }
        if (serviceUrlList.size() == 1){
            return serviceUrlList.get(0).getServiceAddr();
        }
        return doSelect(serviceUrlList,rpcRequest);
    }

    protected abstract String doSelect(List<ProviderNodeInfo> serviceUrlList, RpcRequest rpcRequest);

}
