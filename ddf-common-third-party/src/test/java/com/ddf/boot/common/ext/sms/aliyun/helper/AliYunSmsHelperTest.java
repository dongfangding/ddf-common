package com.ddf.boot.common.ext.sms.aliyun.helper;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import com.ddf.boot.common.ext.sms.aliyun.config.AliYunSmsProperties;
import com.ddf.boot.common.ext.sms.aliyun.domain.TemplateParamObj;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

/**
 * AliYunSmsHelper 测试
 *
 * @author Codex
 * @since 2026/04/20
 */
class AliYunSmsHelperTest {

    private final AliYunSmsHelper helper = new AliYunSmsHelper(new AliYunSmsProperties());

    @Test
    @DisplayName("应生成六位随机验证码模板参数")
    void shouldGenerateRandomCodeTemplateParam() {
        TemplateParamObj templateParamObj = helper.randomCodeTemplateParam();

        assertNotNull(templateParamObj);
        assertNotNull(templateParamObj.getCode());
        assertEquals(6, templateParamObj.getCode().length());
        assertTrue(templateParamObj.getCode().chars().allMatch(Character::isDigit));
        assertTrue(templateParamObj.getTemplateParam().contains(templateParamObj.getCode()));
        assertTrue(templateParamObj.getTemplateParam().contains("\"code\""));
    }
}
