package com.miyuki.mrpc.core.common.enums;

import lombok.AllArgsConstructor;
import lombok.Getter;

/**
 * Created with IntelliJ IDEA.
 *
 * @Author: miyuki
 * @Date: 2022/11
 * @Description: 压缩方式枚举
 */
@AllArgsConstructor
@Getter
public enum CompressTypeEnum {
    GZIP((byte)0X01,"gzip");

    private final byte code;
    private final String name;

    public static String getName(byte code){
        for (CompressTypeEnum value : CompressTypeEnum.values()) {
            if (value.getCode() == code){
                return value.name;
            }
        }
        return null;
    }
}
