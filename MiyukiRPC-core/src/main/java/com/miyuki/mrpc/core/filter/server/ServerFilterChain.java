package com.miyuki.mrpc.core.filter.server;

import com.miyuki.mrpc.core.filter.ServerFilter;
import com.miyuki.mrpc.core.remoting.dto.RpcRequest;

/**
 * Created with IntelliJ IDEA.
 *
 * @Author: miyuki
 * @Date: 2022/11
 * @Description:
 */
public abstract class ServerFilterChain {

    public abstract void addServerFilter(ServerFilter serverFilter);

    public abstract void doFilter(RpcRequest rpcRequest);
}
