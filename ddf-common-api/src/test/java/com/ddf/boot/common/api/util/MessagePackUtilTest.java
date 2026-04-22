package com.ddf.boot.common.api.util;

import static org.junit.jupiter.api.Assertions.assertArrayEquals;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import com.ddf.boot.common.api.model.authentication.UserClaim;
import java.util.Map;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

/**
 * MessagePackUtil 测试
 *
 * @author Codex
 * @since 2026/04/20
 */
class MessagePackUtilTest {

    @Test
    @DisplayName("应支持对象字节与十六进制双向编解码")
    void shouldSerializeAndDeserializeObject() {
        UserClaim claim = new UserClaim();
        claim.setUserId("u-1");
        claim.setUsername("tester");
        claim.setCredit("credit-a");
        claim.setRemarks("备注");
        claim.setProperties(Map.of("k1", "v1"));

        byte[] bytes = MessagePackUtil.writeValueAsBytes(claim);
        String hex = MessagePackUtil.writeValueAsHex(claim);

        UserClaim byBytes = MessagePackUtil.readValue(bytes, UserClaim.class);
        UserClaim byHex = MessagePackUtil.readHexValue(hex, UserClaim.class);

        assertTrue(bytes.length > 0);
        assertFalse(hex.isEmpty());
        assertEquals("u-1", byBytes.getUserId());
        assertEquals("tester", byBytes.getUsername());
        assertEquals("credit-a", byBytes.getCredit());
        assertEquals("备注", byHex.getRemarks());
        assertEquals("v1", byHex.getProperties().get("k1"));
    }

    @Test
    @DisplayName("十六进制转换应支持空串与正常数据")
    void shouldConvertHexToBinary() {
        assertArrayEquals(new byte[0], MessagePackUtil.hexToBinary(null));
        assertArrayEquals(new byte[0], MessagePackUtil.hexToBinary(""));
        assertArrayEquals(new byte[] {0x01, 0x23, (byte) 0xab}, MessagePackUtil.hexToBinary("0123ab"));
    }

    @Test
    @DisplayName("新建 ObjectMapper 应忽略未知字段")
    void shouldIgnoreUnknownProperties() {
        UserClaim claim = MessagePackUtil.readValue(
                MessagePackUtil.writeValueAsBytes(Map.of("userId", "u-2", "unknownField", "ignored")),
                UserClaim.class
        );

        assertEquals("u-2", claim.getUserId());
    }
}
