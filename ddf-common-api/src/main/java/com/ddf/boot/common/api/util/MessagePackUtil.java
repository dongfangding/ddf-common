package com.ddf.boot.common.api.util;

import com.ddf.boot.common.api.model.authentication.UserClaim;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.nio.charset.StandardCharsets;
import java.util.HexFormat;
import java.util.Map;
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

    @SneakyThrows
    public static byte[] writeValueAsBytes(Object value) {
        return OBJECT_MAPPER.writeValueAsBytes(value);
    }

    @SneakyThrows
    public static String writeValueAsHex(Object value) {
        return HexFormat.of().formatHex(writeValueAsBytes(value));
    }

    @SneakyThrows
    public static <T> T readValue(byte[] src, Class<T> valueType) {
        return OBJECT_MAPPER.readValue(src, valueType);
    }


    /**
     * 将16进制数据，转换为msgpack协议，由于原生msgpack协议无法用字符展示，调试的时候，比如使用浏览器测试，
     * 接收到的数据是二进制，可以复制为16进制，这样可以复制然后反序列化看是否正确。
     *
     * @param content
     * @param valueType
     * @return
     * @param <T>
     */
    @SneakyThrows
    public static <T> T readHexValue(String content, Class<T> valueType) {
        return OBJECT_MAPPER.readValue(hexToBinary(content), valueType);
    }

    public static void main(String[] args) {
        final UserClaim claim = new UserClaim();
        claim.setUserId("123");
        claim.setUsername("snowball");
        claim.setCredit("1000");
        claim.setRemarks("测试用户");
        claim.setDetail("测试用户详情");
        claim.setProperties(Map.of("key1", "value1", "key2", "value2"));
        // 1. 获取 JSON 字节
        byte[] jsonBytes = JsonUtil
                .toJson(claim)
                .getBytes(StandardCharsets.UTF_8);
        // 2. 获取 MsgPack 字节
        byte[] msgPackBytes = writeValueAsBytes(claim);
        final String hex = writeValueAsHex(claim);
        System.out.println(hex);

        System.out.println("JSON 实际大小: " + jsonBytes.length + " bytes");
        System.out.println("MsgPack 实际大小: " + msgPackBytes.length + " bytes");
        System.out.println("压缩率: " + (double) msgPackBytes.length / jsonBytes.length * 100 + "%");

        UserClaim userClaim = readValue(msgPackBytes, UserClaim.class);

        System.out.println(readHexValue(hex,
                UserClaim.class
        ));
        System.out.println(userClaim);
    }

    public static byte[] hexToBinary(String hexString) {
        if (hexString == null || hexString.isEmpty()) {
            return new byte[0];
        }
        return HexFormat
                .of()
                .parseHex(hexString);
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
