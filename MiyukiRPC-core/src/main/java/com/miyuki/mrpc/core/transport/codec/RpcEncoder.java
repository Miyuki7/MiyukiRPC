package com.miyuki.mrpc.core.transport.codec;

import com.miyuki.mrpc.core.common.enums.CompressTypeEnum;
import com.miyuki.mrpc.core.common.enums.SerializationTypeEnum;
import com.miyuki.mrpc.core.common.extension.ExtensionLoader;
import com.miyuki.mrpc.core.common.serialize.Serializer;
import com.miyuki.mrpc.core.compress.Compress;
import com.miyuki.mrpc.core.remoting.dto.RpcProtocol;
import com.miyuki.mrpc.core.remoting.constants.*;
import io.netty.buffer.ByteBuf;
import io.netty.channel.ChannelHandlerContext;
import io.netty.handler.codec.MessageToByteEncoder;
import lombok.extern.slf4j.Slf4j;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * Created with IntelliJ IDEA.
 *
 * @Author: miyuki
 * @Date: 2022/11
 * @Description:
 */
@Slf4j
public class RpcEncoder extends MessageToByteEncoder<RpcProtocol> {
    private static final AtomicInteger ATOMIC_INTEGER = new AtomicInteger(0);

    @Override
    protected void encode(ChannelHandlerContext ctx, RpcProtocol msg, ByteBuf out) throws Exception {
        try {
            //4B
            out.writeBytes(RpcConstants.MAGIC_NUMBER);

            //1B
            out.writeByte(RpcConstants.VERSION);
            //leave a place to write the value of full length
            out.writerIndex(out.writerIndex() + 4);

            byte messageType = msg.getMessageType();
            out.writeByte(messageType);
            out.writeByte(msg.getCodec());
            out.writeByte(msg.getCompress());
            out.writeInt(ATOMIC_INTEGER.getAndIncrement());

            //full length
            byte[] bodyBytes = null;
            int fullLength = RpcConstants.HEAD_LENGTH;

            // if messageType is not heartbeat message,fullLength = head length + body length
            if (messageType != RpcConstants.HEARTBEAT_REQUEST_TYPE
                    && messageType != RpcConstants.HEARTBEAT_RESPONSE_TYPE) {
                String codecName = SerializationTypeEnum.getName(msg.getCodec());
                log.info("codec name: [{}]", codecName);
                //serialize
                ExtensionLoader<Serializer> extensionLoader = ExtensionLoader.getExtensionLoader(Serializer.class);
                Serializer serializer = extensionLoader.getExtension(codecName);
                bodyBytes = serializer.serialize(msg.getData());
                //compress
                String compressName = CompressTypeEnum.getName(msg.getCompress());
                Compress compress = ExtensionLoader.getExtensionLoader(Compress.class).getExtension(compressName);
                bodyBytes = compress.compress(bodyBytes);
                fullLength += bodyBytes.length;
            }
            if (bodyBytes != null) {
                out.writeBytes(bodyBytes);
            }
            int writeIndex = out.writerIndex();
            out.writerIndex(writeIndex - fullLength + RpcConstants.MAGIC_NUMBER.length + 1);
            out.writeInt(fullLength);
            out.writerIndex(writeIndex);
        } catch (Exception e) {
            log.error("Encode request error!", e);
        }
    }
}
