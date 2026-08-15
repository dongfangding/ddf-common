package com.ddf.boot.common.api.util;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.util.HexFormat;
import lombok.SneakyThrows;
import org.msgpack.jackson.dataformat.MessagePackFactory;

/**
 * <p>message pack协议工具类</p >
 *
 * @author Snowball
 * @version 1.0
 * @since 2026/01/06 15:58
 */
public class MessagePackUtil {

    private static final ObjectMapper OBJECT_MAPPER = newInstance();

    /**
     * @param value 参数值
     */
    @SneakyThrows
    public static byte[] writeValueAsBytes(Object value) {
        return OBJECT_MAPPER.writeValueAsBytes(value);
    }

    /**
     * @param value 参数值
     */
    @SneakyThrows
    public static String writeValueAsHex(Object value) {
        return HexFormat.of().formatHex(writeValueAsBytes(value));
    }

    /**
     * @param src 参数
     * @param valueType 参数
     */
    @SneakyThrows
    public static <T> T readValue(byte[] src, Class<T> valueType) {
        return OBJECT_MAPPER.readValue(src, valueType);
    }


    /**
     * 将16进制数据，转换为msgpack协议，由于原生msgpack协议无法用字符展示，调试的时候，比如使用浏览器测试，
     * 接收到的数据是二进制，可以复制为16进制，这样可以复制然后反序列化看是否正确。
     *
     * @param content 内容
     * @param valueType 值类型
     * @param <T> 泛型类型
     */
    @SneakyThrows
    public static <T> T readHexValue(String content, Class<T> valueType) {
        return OBJECT_MAPPER.readValue(hexToBinary(content), valueType);
    }

    /**
     * @param hexString 参数
     */
    public static byte[] hexToBinary(String hexString) {
        if (hexString == null || hexString.isEmpty()) {
            return new byte[0];
        }
        return HexFormat.of().parseHex(hexString);
    }

    /**
     * 返回新实例,同时设置默认值
     *
     * @return 配置好的 ObjectMapper 实例
     */
    public static ObjectMapper newInstance() {
        ObjectMapper mapper = new ObjectMapper(new MessagePackFactory());
        // 忽略未知属性，增强健壮性
        mapper.configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false);
        // 忽略空字段，压缩体积
        mapper.setSerializationInclusion(JsonInclude.Include.NON_NULL);
        return mapper;
    }
}
