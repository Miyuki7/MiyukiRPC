package com.miyuki.mrpc.core.common.enums;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.ToString;

/**
 * Created with IntelliJ IDEA.
 *
 * @Author: miyuki
 * @Date: 2022/11
 * @Description: response相应码
 */
@AllArgsConstructor
@Getter
@ToString
public enum RpcResponseCodeEnum {
    SUCCESS(200,"Remote call is success"),
    FAIL(500,"Remote call is failed"),
    RPC_FAIL(400,"Remote call is failed, caused by rpc"),
    //token不对
    FAIL_TOKEN_ILLEGAL(300,"service token is illegal");

    private final int code;
    private final String msg;

}
