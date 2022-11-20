package com.miyuki.mrpc.core.filter.server;

import com.miyuki.mrpc.core.filter.ServerFilter;
import com.miyuki.mrpc.core.remoting.dto.RpcRequest;

import java.util.ArrayList;
import java.util.List;

/**
 * Created with IntelliJ IDEA.
 *
 * @Author: miyuki
 * @Date: 2022/11
 * @Description:
 */
public class ServerAfterFilterChain extends ServerFilterChain{
    private static List<ServerFilter> ServerFilters = new ArrayList<>();
    @Override
    public void addServerFilter(ServerFilter serverFilter) {
        ServerFilters.add(serverFilter);
    }

    @Override
    public void doFilter(RpcRequest rpcRequest) {
        ServerFilters.forEach(serverFilter ->{
            serverFilter.doFilter(rpcRequest);
        });
    }
}
