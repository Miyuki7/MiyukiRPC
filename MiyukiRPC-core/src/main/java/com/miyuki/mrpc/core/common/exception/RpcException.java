package com.miyuki.mrpc.core.common.exception;

import com.miyuki.mrpc.core.common.enums.RpcErrorMessageEnum;

/**
 * Created with IntelliJ IDEA.
 *
 * @Author: miyuki
 * @Date: 2022/11
 * @Description:
 */
public class RpcException extends RuntimeException{
    public RpcException(RpcErrorMessageEnum rpcErrorMessageEnum, String detail) {
        super(rpcErrorMessageEnum.getMessage() + ":" + detail);
    }
    public RpcException(String message, Throwable cause) {
        super(message, cause);
    }

    public RpcException(RpcErrorMessageEnum rpcErrorMessageEnum) {
        super(rpcErrorMessageEnum.getMessage());
    }
}
