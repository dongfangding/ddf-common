package com.ddf.boot.common.core.util;

import com.ddf.boot.common.api.exception.BadRequestException;
import com.ddf.boot.common.api.exception.BaseErrorCallbackCode;
import com.ddf.boot.common.api.exception.BusinessException;
import jakarta.validation.constraints.NotBlank;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

/**
 * PreconditionUtil 测试
 *
 * @author Codex
 * @since 2026/04/20
 */
class PreconditionUtilTest {

    @Test
    @DisplayName("checkArgument 失败时应抛出业务异常")
    void shouldThrowBusinessExceptionWhenCheckArgumentFails() {
        BusinessException messageException = assertThrows(BusinessException.class,
                () -> PreconditionUtil.checkArgument(false, "参数错误"));
        assertEquals("参数错误", messageException.getDescription());

        BusinessException callbackException = assertThrows(BusinessException.class,
                () -> PreconditionUtil.checkArgument(false, BaseErrorCallbackCode.REQUEST_TOO_MANY));
        assertEquals(BaseErrorCallbackCode.REQUEST_TOO_MANY.getCode(), callbackException.getCode());
    }

    @Test
    @DisplayName("checkBadRequest 失败时应抛出 BadRequestException")
    void shouldThrowBadRequestExceptionWhenCheckBadRequestFails() {
        BadRequestException exception = assertThrows(BadRequestException.class,
                () -> PreconditionUtil.checkBadRequest(false, "请求非法"));

        assertEquals("请求非法", exception.getDescription());
    }

    @Test
    @DisplayName("requiredParamCheck 应校验空对象与约束对象")
    void shouldValidateRequiredParameters() {
        BusinessException nullException = assertThrows(BusinessException.class,
                () -> PreconditionUtil.requiredParamCheck(null));
        assertEquals(BaseErrorCallbackCode.BAD_REQUEST.getCode(), nullException.getCode());

        DemoParam invalidParam = new DemoParam();
        invalidParam.setName("");

        BadRequestException invalidException = assertThrows(BadRequestException.class,
                () -> PreconditionUtil.requiredParamCheck(invalidParam));
        assertEquals("name不能为空", invalidException.getDescription());
    }

    @Test
    @DisplayName("requiredParamCheck 在合法对象时应通过")
    void shouldPassWhenRequiredParametersAreValid() {
        DemoParam param = new DemoParam();
        param.setName("codex");

        PreconditionUtil.requiredParamCheck(param);
    }

    /**
     * 校验请求对象
     */
    static class DemoParam {

        /**
         * 名称
         */
        @NotBlank(message = "name不能为空")
        private String name;

        public String getName() {
            return name;
        }

        public void setName(String name) {
            this.name = name;
        }
    }
}
