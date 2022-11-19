package com.miyuki.mrpc.core.compress;

import com.miyuki.mrpc.core.common.extension.SPI;

import java.io.IOException;

/**
 * Created with IntelliJ IDEA.
 *
 * @Author: miyuki
 * @Date: 2022/11
 * @Description:
 */
@SPI
public interface Compress {

    byte[] compress(byte[] bytes) throws IOException;

    byte[] decompress(byte[] bytes);
}
