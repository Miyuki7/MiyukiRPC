package com.miyuki.mrpc.core.filter;

import com.miyuki.mrpc.core.common.extension.SPI;
import com.miyuki.mrpc.core.config.RpcServiceConfig;
import com.miyuki.mrpc.core.register.zk.ProviderNodeInfo;

import java.util.List;

/**
 * Created with IntelliJ IDEA.
 *
 * @Author: miyuki
 * @Date: 2022/11
 * @Description:
 */
@SPI
public interface ClientFilter extends Filter{

    void doFilter(List<ProviderNodeInfo> providerNodeInfoList, RpcServiceConfig rpcServiceConfig);
}
