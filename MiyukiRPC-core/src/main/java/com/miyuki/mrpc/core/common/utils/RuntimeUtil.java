package com.miyuki.mrpc.core.common.utils;

/**
 * Created with IntelliJ IDEA.
 *
 * @Author: miyuki
 * @Date: 2022/11
 * @Description:
 */
public class RuntimeUtil {
    public static int cpus(){
        return Runtime.getRuntime().availableProcessors();
    }

}
