package com.miyuki.mrpc.core.common;

import lombok.*;

import java.io.Serializable;
import java.util.Arrays;
import java.util.PriorityQueue;

import static com.miyuki.mrpc.core.common.constants.RpcConstants.MAGIC_NUMBER;

/**
 * Created with IntelliJ IDEA.
 *
 * @Author: miyuki
 * @Date: 2022/11
 * @Description: rpc协议
 */
@AllArgsConstructor
@NoArgsConstructor
@Getter
@Setter
@Builder
@ToString
public class RpcProtocol{
    /**
     * rpc message type
     */
    private byte messageType;
    /**
     * serialization type
     */
    private byte codec;
    /**
     * compress type
     */
    private byte compress;
    /**
     * request id
     */
    private int requestId;
    /**
     * request data
     */
    private Object data;

}
