package com.miyuki.mrpc.core.remoting.constants;


/**
 * Created with IntelliJ IDEA.
 *
 * @Author: miyuki
 * @Date: 2022/11
 * @Description:
 */
public class RpcConstants {
    //消息类型
    public static final byte REQUEST_TYPE = 1;
    public static final byte RESPONSE_TYPE = 2;
    public static final byte HEARTBEAT_REQUEST_TYPE = 3;
    public static final byte HEARTBEAT_RESPONSE_TYPE = 4;
    //协议版本号
    public static final byte VERSION = 1;
    public static final byte[] MAGIC_NUMBER = {(byte) 'm', (byte) 'r', (byte) 'p', (byte) 'c'};

    //协议头部长度
    public static final int HEAD_LENGTH = 16;

    //心跳信息
    public static final String PING = "ping";
    public static final String PONG = "pong";

    //最大帧长度
    public static final int MAX_FRAME_LENGTH = 8 * 1024 * 1024;

    //最大重连次数
    public static final int MAX_RECONNECTION_TIMES = 5;

    //最大储备
    public static final int BACKLOG = 1024;

    //限流凭证
    public static final int MAX_SEMAPHORE_NUMS = 50;

    public static final int SERVICE_NODE = 0;

    public static final int PERMIT_NODE = 1;

    //限流速率
    public static final int RATELIMIT = 500;
}
