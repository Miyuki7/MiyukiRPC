package com.miyuki.mrpc.core.loadbalance;

import com.miyuki.mrpc.core.common.extension.SPI;
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
@SPI
public interface LoadBalance {
    String selectServiceAddress(List<ProviderNodeInfo> serviceUrlList, RpcRequest rpcRequest);
}
