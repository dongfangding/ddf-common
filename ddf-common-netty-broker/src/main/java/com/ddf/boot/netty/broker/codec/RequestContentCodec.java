package com.ddf.boot.netty.broker.codec;

import com.ddf.boot.netty.broker.message.RequestContent;
import com.fasterxml.jackson.databind.ObjectMapper;
import io.netty.buffer.ByteBuf;
import io.netty.buffer.Unpooled;
import io.netty.channel.ChannelHandlerContext;
import io.netty.handler.codec.ByteToMessageCodec;
import io.netty.util.CharsetUtil;
import lombok.extern.slf4j.Slf4j;

import java.nio.charset.Charset;
import java.util.List;

/**
 * 编解码器，用于解析和传输对象{@link RequestContent}
 * @author dongfang.ding
 * @date 2019/7/5 15:01
 */
@Slf4j
public class RequestContentCodec extends ByteToMessageCodec<Object> {

    private final Charset charset;

    public RequestContentCodec(Charset charset) {
        if (charset == null) {
            charset = CharsetUtil.UTF_8;
        }
        this.charset = charset;
    }

    public RequestContentCodec() {
        this.charset = CharsetUtil.UTF_8;
    }

    /**
     * 服务端操作数据使用对象{@link RequestContent}，最终写入到客户端的时候编码成字节
     * @param ctx
     * @param msg
     * @param out
     * @throws Exception
     */
    @Override
    protected void encode(ChannelHandlerContext ctx, Object msg, ByteBuf out) throws Exception {
        if (msg instanceof RequestContent) {
            out.writeBytes(Unpooled.copiedBuffer(new ObjectMapper().writeValueAsString(msg).getBytes(charset)));
            out.writeBytes("\r\n".getBytes());
        }
    }


    /**
     * 将客户端传入的解码成服务端使用的{@link RequestContent}
     * 注意TCP的粘包和拆包问题，这里已经使用了{@link io.netty.handler.codec.LineBasedFrameDecoder}解码器来解决，要求客户端比如以换行符结尾
     * @param ctx
     * @param in
     * @param out
     * @throws Exception
     */
    @Override
    protected void decode(ChannelHandlerContext ctx, ByteBuf in, List<Object> out) throws Exception {
        try {
            log.debug(">>>>>>>>>>>>>> 开始解码, 内容为: {}", in.toString(charset));
            ObjectMapper objectMapper = new ObjectMapper();
            byte[] content = new byte[in.readableBytes()];
            in.readBytes(content);
            RequestContent<?> requestContent = objectMapper.readValue(content, RequestContent.class);
            out.add(requestContent);
            // TODO 对RequestContent参数进行校验
        } catch (Exception e) {
            log.error("解码失败", e);
        }
    }
}
