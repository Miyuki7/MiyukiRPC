package com.miyuki.mrpc.core.common;

import lombok.*;

/**
 * Created with IntelliJ IDEA.
 *
 * @Author: miyuki
 * @Date: 2022/11
 * @Description:
 */
@AllArgsConstructor
@NoArgsConstructor
@Getter
@Setter
@Builder
@ToString
public class RpcResponse<T> {

    private String requestId;
    private Integer code;
    private String message;
    private T data;

    public static <T> RpcResponse<T> success(T data, String requestId){
        RpcResponse<T> response = (RpcResponse<T>) RpcResponse.builder()
                .code(RpcResponseCodeEnum.SUCCESS.getCode())
                .message(RpcResponseCodeEnum.SUCCESS.getMsg())
                .requestId(requestId)
                .data(data)
                .build();
        return response;
    }

    public static <T> RpcResponse<T> fail(RpcResponseCodeEnum rpcResponseCodeEnum){
        RpcResponse<T> response = (RpcResponse<T>) RpcResponse.builder()
                .code(RpcResponseCodeEnum.FAIL.getCode())
                .message(RpcResponseCodeEnum.FAIL.getMsg())
                .build();
        return response;
    }
