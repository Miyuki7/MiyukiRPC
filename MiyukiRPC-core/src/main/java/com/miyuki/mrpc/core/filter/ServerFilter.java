package com.miyuki.mrpc.core.filter;

import com.miyuki.mrpc.core.common.extension.SPI;
import com.miyuki.mrpc.core.remoting.dto.RpcRequest;

/**
 * Created with IntelliJ IDEA.
 *
 * @Author: miyuki
 * @Date: 2022/11
 * @Description:
 */
@SPI
public interface ServerFilter extends Filter{

    void doFilter(RpcRequest rpcRequest);
}
