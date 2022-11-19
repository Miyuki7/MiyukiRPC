package com.miyuki.mrpc.core.common.serialize.hessian;

import com.caucho.hessian.io.HessianInput;
import com.caucho.hessian.io.HessianOutput;
import com.miyuki.mrpc.core.common.exception.SerializeException;
import com.miyuki.mrpc.core.common.serialize.Serializer;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;

/**
 * Created with IntelliJ IDEA.
 *
 * @Author: miyuki
 * @Date: 2022/11
 * @Description:
 */
public class HessianSerializer implements Serializer {
    @Override
    public byte[] serialize(Object object) {
        try (ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream()){
            HessianOutput hessianOutput = new HessianOutput(byteArrayOutputStream);
            hessianOutput.writeObject(object);
            return byteArrayOutputStream.toByteArray();
        } catch (Exception e) {
            throw new SerializeException("Serialization failed");
        }
    }

    @Override
    public <T> T deserialize(byte[] bytes, Class<T> clazz) {
        try (ByteArrayInputStream byteArrayInputStream = new ByteArrayInputStream(bytes)) {
            HessianInput hessianInput = new HessianInput(byteArrayInputStream);
            Object o = hessianInput.readObject();

            return clazz.cast(o);

        } catch (Exception e) {
            throw new SerializeException("Deserialization failed");
        }
    }
}
