package com.miyuki.mrpc.core.common.utils;

import com.google.common.net.InetAddresses;

/**
 * Created with IntelliJ IDEA.
 *
 * @Author: miyuki
 * @Date: 2022/11
 * @Description:
 */
public class IpUtil {

    public static boolean isValidIp(String ip) {
        return InetAddresses.isInetAddress(ip);
    }
}
