package com.ddf.boot.common.api.util;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;

/**
 * PretendUtils 测试
 *
 * @author Codex
 * @since 2026/04/20
 */
class PretendUtilsTest {

    @Test
    @DisplayName("应脱敏中文姓名")
    void shouldMaskChineseName() {
        assertEquals("", PretendUtils.chineseName(null));
        assertEquals("*三", PretendUtils.chineseName("张三"));
        assertEquals("张*丰", PretendUtils.chineseName("张三丰"));
    }

    @Test
    @DisplayName("应脱敏身份证和固定展示身份证")
    void shouldMaskIdCard() {
        assertEquals("", PretendUtils.idCardNum(""));
        assertEquals("**************1234", PretendUtils.idCardNum("110101199001011234"));
        assertEquals("1101**********1234", PretendUtils.fixIdCardNum("110101199001011234"));
        assertEquals("1234567", PretendUtils.fixIdCardNum("1234567"));
    }

    @Test
    @DisplayName("应脱敏固定电话和手机号")
    void shouldMaskPhoneNumbers() {
        assertEquals("", PretendUtils.fixedPhone(null));
        assertEquals("*******1234", PretendUtils.fixedPhone("01088881234"));
        assertEquals("135****6810", PretendUtils.mobilePhone("13512346810"));
    }

    @Test
    @DisplayName("应脱敏地址、邮箱、银行卡和密码")
    void shouldMaskAddressEmailBankCardAndPassword() {
        assertEquals("北京市海淀****", PretendUtils.address("北京市海淀区知春路", 4));
        assertEquals("d*@126.com", PretendUtils.email("dd@126.com"));
        assertEquals("a@b.com", PretendUtils.email("a@b.com"));
        assertEquals("6222**********1234", PretendUtils.bankCard("62220212345678901234"));
        assertEquals("******", PretendUtils.password("123456"));
        assertEquals("***", PretendUtils.password("123"));
        assertEquals("", PretendUtils.password(""));
    }
}
