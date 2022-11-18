package com.miyuki.mrpc.core.common.enums;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.ToString;

/**
 * Created with IntelliJ IDEA.
 *
 * @Author: miyuki
 * @Date: 2022/11
 * @Description:
 */
@AllArgsConstructor
@Getter
@ToString
public enum RpcErrorMessageEnum {
    CLIENT_CONNECT_SERVER_FAILURE("客户端连接服务端失败"),
    SERVICE_INVOCATION_FAILURE("服务调用失败"),
    SERVICE_CAN_NOT_BE_FOUND("没有找到指定的服务"),
    SERVICE_NOT_IMPLEMENT_ANY_INTERFACE("注册的服务没有实现任何接口"),
    REQUEST_NOT_MATCH_RESPONSE("返回结果错误！请求和返回的相应不匹配"),

    MAGIC_NUMBER_NOT_MATCH("MAGIC_NUMBER 不匹配"),

    TOKEN_NOT_MATCH("token 不匹配"),

    MaxServiceLimitRequestException("服务请求数量达到上限");

    private final String message;
}
