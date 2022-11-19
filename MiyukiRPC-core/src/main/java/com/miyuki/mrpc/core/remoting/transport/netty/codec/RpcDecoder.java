package com.miyuki.mrpc.core.remoting.transport.netty.codec;

import com.miyuki.mrpc.core.common.enums.CompressTypeEnum;
import com.miyuki.mrpc.core.common.enums.RpcErrorMessageEnum;
import com.miyuki.mrpc.core.common.enums.SerializationTypeEnum;
import com.miyuki.mrpc.core.common.exception.RpcException;
import com.miyuki.mrpc.core.common.extension.ExtensionLoader;
import com.miyuki.mrpc.core.common.serialize.Serializer;
import com.miyuki.mrpc.core.compress.Compress;
import com.miyuki.mrpc.core.remoting.constants.RpcConstants;
import com.miyuki.mrpc.core.remoting.dto.RpcProtocol;
import com.miyuki.mrpc.core.remoting.dto.RpcRequest;
import com.miyuki.mrpc.core.remoting.dto.RpcResponse;
import io.netty.buffer.ByteBuf;
import io.netty.channel.ChannelHandlerContext;
import io.netty.handler.codec.LengthFieldBasedFrameDecoder;
import lombok.extern.slf4j.Slf4j;

/**
 * Created with IntelliJ IDEA.
 *
 * @Author: miyuki
 * @Date: 2022/11
 * @Description: 解码器
 */
@Slf4j
public class RpcDecoder extends LengthFieldBasedFrameDecoder {
    public RpcDecoder(){
        // lengthFieldOffset: magic code is 4B, and version is 1B, and then full length. so value is 5
        // lengthFieldLength: full length is 4B. so value is 4
        // lengthAdjustment: full length include all data and read 9 bytes before, so the left length is (fullLength-9). so values is -9
        // initialBytesToStrip: we will check magic code and version manually, so do not strip any bytes. so values is 0
        this(RpcConstants.MAX_FRAME_LENGTH, 5, 4, -9, 0);
    }

    /**
     * @param maxFrameLength      Maximum frame length. It decide the maximum length of data that can be received.
     *                            If it exceeds, the data will be discarded.
     * @param lengthFieldOffset   Length field offset. The length field is the one that skips the specified length of byte.
     * @param lengthFieldLength   The number of bytes in the length field.
     * @param lengthAdjustment    The compensation value to add to the value of the length field
     * @param initialBytesToStrip Number of bytes skipped.
     *                            If you need to receive all of the header+body data, this value is 0
     *                            if you only want to receive the body data, then you need to skip the number of bytes consumed by the header.
     */
    public RpcDecoder(int maxFrameLength, int lengthFieldOffset, int lengthFieldLength,
                             int lengthAdjustment, int initialBytesToStrip) {
        super(maxFrameLength, lengthFieldOffset, lengthFieldLength, lengthAdjustment, initialBytesToStrip);
    }

    @Override
    protected Object decode(ChannelHandlerContext ctx, ByteBuf in) throws Exception {
        Object decoded =  super.decode(ctx, in);
        if (decoded instanceof ByteBuf){
            ByteBuf frame = (ByteBuf) decoded;
            if (frame.readableBytes() >= RpcConstants.TOTAL_LENGTH) {
                try {
                    return decodeFrame(frame);
                } catch (Exception e) {
                    log.error("Decode frame error!", e);
                    throw e;
                } finally {
                    frame.release();
                }
            }
        }
        return decoded;
    }

    private Object decodeFrame(ByteBuf in) {
        //must read ByteBuf in order
        checkMagicNumber(in);
        checkVersion(in);
        int fullLength = in.readInt();
        //build RpcMessage object
        byte messageType = in.readByte();
        byte codecType = in.readByte();
        byte compressType = in.readByte();
        int requestId = in.readInt();
        RpcProtocol protocol = RpcProtocol.builder()
                .codec(codecType)
                .compress(compressType)
                .requestId(requestId)
                .messageType(messageType).build();
        if (messageType == RpcConstants.HEARTBEAT_REQUEST_TYPE) {
            protocol.setData(RpcConstants.PING);
            return protocol;
        }
        if (messageType == RpcConstants.HEARTBEAT_RESPONSE_TYPE) {
            protocol.setData(RpcConstants.PONG);
            return protocol;
        }
        int bodyLength = fullLength - RpcConstants.HEAD_LENGTH;
        if (bodyLength > 0) {
            byte[] bs = new byte[bodyLength];
            in.readBytes(bs);
            //decompress the bytes
            String compressName = CompressTypeEnum.getName(compressType);
            Compress compress = ExtensionLoader.getExtensionLoader(Compress.class)
                    .getExtension(compressName);
            bs = compress.decompress(bs);
            //deserialize the object
            String codecName = SerializationTypeEnum.getName(protocol.getCodec());
            log.info("codec name: [{}]", codecName);
            Serializer serializer = ExtensionLoader.getExtensionLoader(Serializer.class)
                    .getExtension(codecName);
            if (messageType == RpcConstants.REQUEST_TYPE) {
                RpcRequest tmpValue = serializer.deserialize(bs, RpcRequest.class);
                protocol.setData(tmpValue);
            } else {
                RpcResponse tmpValue = serializer.deserialize(bs, RpcResponse.class);
                protocol.setData(tmpValue);
            }
        }
        return protocol;
    }

    private void checkMagicNumber(ByteBuf in) {
        // read the first 4 bit, which is the magic number, and compare
        int len = RpcConstants.MAGIC_NUMBER.length;
        byte[] tmp = new byte[len];
        in.readBytes(tmp);
        for (int i = 0; i < tmp.length; i++) {
            if (tmp[i] != RpcConstants.MAGIC_NUMBER[i]) {
                throw new RpcException(RpcErrorMessageEnum.MAGIC_NUMBER_NOT_MATCH);
            }
        }
    }

    private void checkVersion(ByteBuf in) {
        //read th version and compare
        byte version = in.readByte();
        if (version != RpcConstants.VERSION) {
            throw new RuntimeException("version isn't compatible" + version);
        }
    }

}
