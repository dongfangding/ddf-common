package com.ddf.boot.common.mvc.exception200;

import com.ddf.boot.common.api.exception.BadRequestException;
import com.ddf.boot.common.api.exception.BaseCallbackCode;
import com.ddf.boot.common.api.exception.BaseErrorCallbackCode;
import jakarta.validation.constraints.NotBlank;
import java.sql.SQLIntegrityConstraintViolationException;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.dao.DuplicateKeyException;
import org.springframework.validation.BeanPropertyBindingResult;
import org.springframework.validation.BindException;
import org.springframework.validation.ObjectError;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

/**
 * AbstractExceptionHandler.resolveExceptionMessage 测试
 *
 * @author Codex
 * @since 2026/04/20
 */
class AbstractExceptionHandlerResolveTest {

    @Test
    @DisplayName("BaseException 应按异常自身规则解析")
    void shouldResolveBaseException() {
        AbstractExceptionHandler.ExceptionResolveResult result = AbstractExceptionHandler.resolveExceptionMessage(
                new BadRequestException("字段不能为空"), null);

        assertEquals(BaseErrorCallbackCode.BAD_REQUEST.getCode(), result.exceptionCode());
        assertEquals(BaseErrorCallbackCode.BAD_REQUEST.getCode(), result.formatCode());
        assertEquals("字段不能为空", result.subMessage());
        assertNotNull(result.formatDefaultMessage());
    }

    @Test
    @DisplayName("IllegalArgumentException 应映射为 BAD_REQUEST")
    void shouldResolveIllegalArgumentExceptionAsBadRequest() {
        AbstractExceptionHandler.ExceptionResolveResult result = AbstractExceptionHandler.resolveExceptionMessage(
                new IllegalArgumentException("参数非法"), null);

        assertEquals(BaseErrorCallbackCode.BAD_REQUEST.getCode(), result.exceptionCode());
        assertEquals("", result.subMessage());
    }

    @Test
    @DisplayName("BindException 应拼接校验消息并映射为 BAD_REQUEST")
    void shouldResolveBindException() {
        BeanPropertyBindingResult bindingResult = new BeanPropertyBindingResult(new DemoParam(), "demoParam");
        bindingResult.addError(new ObjectError("demoParam", "name不能为空"));
        bindingResult.addError(new ObjectError("demoParam", "age不能为空"));
        BindException bindException = new BindException(bindingResult);

        AbstractExceptionHandler.ExceptionResolveResult result = AbstractExceptionHandler.resolveExceptionMessage(
                bindException, null);

        assertEquals(BaseErrorCallbackCode.BAD_REQUEST.getCode(), result.exceptionCode());
        assertEquals("name不能为空", result.formatCode());
        assertEquals("name不能为空;age不能为空", result.subMessage());
    }

    @Test
    @DisplayName("唯一约束异常应映射为 DUPLICATE_KEY")
    void shouldResolveDuplicateKeyExceptions() {
        AbstractExceptionHandler.ExceptionResolveResult springResult = AbstractExceptionHandler.resolveExceptionMessage(
                new DuplicateKeyException("duplicate"), null);
        assertEquals(BaseErrorCallbackCode.DUPLICATE_KEY.getCode(), springResult.exceptionCode());

        AbstractExceptionHandler.ExceptionResolveResult sqlResult = AbstractExceptionHandler.resolveExceptionMessage(
                new SQLIntegrityConstraintViolationException("duplicate"), null);
        assertEquals(BaseErrorCallbackCode.DUPLICATE_KEY.getCode(), sqlResult.exceptionCode());
    }

    @Test
    @DisplayName("扩展映射器可解析其它异常")
    void shouldResolveOtherExceptionFromMapping() {
        ExceptionHandlerMapping mapping = new ExceptionHandlerMapping() {
            @Override
            public BaseCallbackCode resolveOtherException(Exception exception) {
                if (exception instanceof IllegalStateException) {
                    return BaseErrorCallbackCode.REQUEST_TOO_MANY;
                }
                return null;
            }
        };

        AbstractExceptionHandler.ExceptionResolveResult result = AbstractExceptionHandler.resolveExceptionMessage(
                new IllegalStateException("busy"), mapping);

        assertEquals(BaseErrorCallbackCode.REQUEST_TOO_MANY.getCode(), result.exceptionCode());
        assertEquals(BaseErrorCallbackCode.REQUEST_TOO_MANY.getBizMessage(), result.formatDefaultMessage());
    }

    @Test
    @DisplayName("未识别异常应保留原始消息作为子消息")
    void shouldKeepOriginalMessageForUnknownException() {
        AbstractExceptionHandler.ExceptionResolveResult result = AbstractExceptionHandler.resolveExceptionMessage(
                new RuntimeException("unknown-error"), null);

        assertEquals("", result.exceptionCode());
        assertEquals("unknown-error", result.subMessage());
    }

    /**
     * 演示参数
     */
    static class DemoParam {
        @NotBlank
        private String name;
    }
}
