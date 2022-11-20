package com.miyuki.mrpc.core.filter.server;

import com.google.common.util.concurrent.RateLimiter;
import com.miyuki.mrpc.core.common.enums.RpcErrorMessageEnum;
import com.miyuki.mrpc.core.common.exception.RpcException;
import com.miyuki.mrpc.core.filter.ServerFilter;
import com.miyuki.mrpc.core.remoting.constants.RpcConstants;
import com.miyuki.mrpc.core.remoting.dto.RpcRequest;
import lombok.extern.slf4j.Slf4j;

/**
 * Created with IntelliJ IDEA.
 *
 * @Author: miyuki
 * @Date: 2022/11
 * @Description: 防止服务请求数量达到上限
 */
@Slf4j
public class ServerServiceBeforeRateLimitFilterImpl implements ServerFilter {

    private static final RateLimiter rateLimiter = RateLimiter.create(RpcConstants.RATELIMIT);

    @Override
    public void doFilter(RpcRequest rpcRequest) {
        if (!rateLimiter.tryAcquire()){
            log.error("[ServerServiceBeforeLimitFilterImpl] {}'s max request is {},reject now", rpcRequest.getRpcServiceName(), rateLimiter.getRate());
            throw new RpcException(RpcErrorMessageEnum.MaxServiceLimitRequestException);
        }
    }
}
