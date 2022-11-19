package com.miyuki.mrpc.core.common.serialize.fastjson;

import com.alibaba.fastjson.JSON;
import com.miyuki.mrpc.core.common.serialize.Serializer;

/**
 * Created with IntelliJ IDEA.
 *
 * @Author: miyuki
 * @Date: 2022/11
 * @Description:
 */
public class FastJsonSerializer implements Serializer {
    @Override
    public byte[] serialize(Object object) {
        String jsonStr = JSON.toJSONString(object);
        return jsonStr.getBytes();
    }

    @Override
    public <T> T deserialize(byte[] bytes, Class<T> clazz) {
        return JSON.parseObject(new String(bytes),clazz);
    }
}
