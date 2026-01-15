package com.ddf.boot.common.api.exception;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatNoException;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

/**
 * {@link BusinessException} 测试类
 *
 * @author dongfang.ding
 */
@DisplayName("BusinessException 测试")
class BusinessExceptionTest {

    @Nested
    @DisplayName("构造方法测试")
    class ConstructorTests {

        @Test
        @DisplayName("构造 - Throwable")
        void constructorWithThrowable_ShouldSetDefaultCode() {
            RuntimeException cause = new RuntimeException("original error");
            BusinessException exception = new BusinessException(cause);

            assertThat(exception.getCause()).isEqualTo(cause);
            assertThat(exception.getCode()).isEqualTo(BaseErrorCallbackCode.BIZ_EXCEPTION.getCode());
            assertThat(exception.getDescription()).isEqualTo("original error");
        }

        @Test
        @DisplayName("构造 - BaseCallbackCode")
        void constructorWithCallbackCode_ShouldSetCodeAndDescription() {
            BusinessException exception = new BusinessException(BaseErrorCallbackCode.PARAM_ERROR);

            assertThat(exception.getCode()).isEqualTo(BaseErrorCallbackCode.PARAM_ERROR.getCode());
            assertThat(exception.getDescription()).isEqualTo(BaseErrorCallbackCode.PARAM_ERROR.getDescription());
            assertThat(exception.getBaseCallbackCode()).isEqualTo(BaseErrorCallbackCode.PARAM_ERROR);
        }

        @Test
        @DisplayName("构造 - String description")
        void constructorWithDescription_ShouldSetDescription() {
            String description = "自定义业务异常描述";
            BusinessException exception = new BusinessException(description);

            assertThat(exception.getCode()).isEqualTo(BaseErrorCallbackCode.BIZ_EXCEPTION.getCode());
            assertThat(exception.getDescription()).isEqualTo(description);
        }

        @Test
        @DisplayName("构造 - String code, String description")
        void constructorWithCodeAndDescription_ShouldSetBoth() {
            String code = "CUSTOM_CODE";
            String description = "自定义业务异常描述";
            BusinessException exception = new BusinessException(code, description);

            assertThat(exception.getCode()).isEqualTo(code);
            assertThat(exception.getDescription()).isEqualTo(description);
        }

        @Test
        @DisplayName("构造 - String code, String description, Object... params")
        void constructorWithCodeDescriptionAndParams_ShouldFormatMessage() {
            String code = "CUSTOM_CODE";
            String description = "参数 {0} 错误，应该是 {1}";
            BusinessException exception = new BusinessException(code, description, "age", "number");

            assertThat(exception.getCode()).isEqualTo(code);
            assertThat(exception.getDescription()).isEqualTo("参数 age 错误，应该是 number");
            assertThat(exception.getParams()).containsExactly("age", "number");
        }

        @Test
        @DisplayName("构造 - BaseCallbackCode, Object... params")
        void constructorWithCallbackCodeAndParams_ShouldFormatMessage() {
            BusinessException exception = new BusinessException(
                    BaseErrorCallbackCode.TEST_FILL_EXCEPTION,
                    "测试参数"
            );

            assertThat(exception.getCode()).isEqualTo(BaseErrorCallbackCode.TEST_FILL_EXCEPTION.getCode());
            assertThat(exception.getDescription()).isEqualTo("带占位符的异常演示[测试参数]");
            assertThat(exception.getParams()).containsExactly("测试参数");
        }

        @Test
        @DisplayName("构造 - Object extra, BaseCallbackCode, Object... params")
        void constructorWithExtraCallbackCodeAndParams_ShouldSetExtraAndFormatMessage() {
            String extra = "extra info";
            BusinessException exception = new BusinessException(
                    extra,
                    BaseErrorCallbackCode.TEST_FILL_EXCEPTION,
                    "测试参数"
            );

            assertThat(exception.getCode()).isEqualTo(BaseErrorCallbackCode.TEST_FILL_EXCEPTION.getCode());
            assertThat(exception.getDescription()).isEqualTo("带占位符的异常演示[测试参数]");
            assertThat(exception.getExtra()).isEqualTo(extra);
            assertThat(exception.getParams()).containsExactly("测试参数");
        }
    }

    @Nested
    @DisplayName("默认回调测试")
    class DefaultCallbackTests {

        @Test
        @DisplayName("defaultCallback - 返回 BIZ_EXCEPTION")
        void defaultCallback_ShouldReturnBizException() {
            BusinessException exception = new BusinessException("测试异常");

            assertThat(exception.defaultCallback()).isEqualTo(BaseErrorCallbackCode.BIZ_EXCEPTION);
        }

        @Test
        @DisplayName("isMaskErrorDetails - 返回 false")
        void isMaskErrorDetails_ShouldReturnFalse() {
            BusinessException exception = new BusinessException("测试异常");

            assertThat(exception.isMaskErrorDetails()).isFalse();
        }
    }

    @Nested
    @DisplayName("异常抛出测试")
    class ThrowExceptionTests {

        @Test
        @DisplayName("抛出 - 简单业务异常")
        void throwSimpleBusinessException_ShouldWork() {
            BusinessException exception = new BusinessException(BaseErrorCallbackCode.PARAM_ERROR);
            assertThat(exception).isNotNull();
        }

        @Test
        @DisplayName("抛出 - 带占位符的异常")
        void throwBusinessExceptionWithParams_ShouldWork() {
            BusinessException exception = new BusinessException(BaseErrorCallbackCode.FILL_PARAM_DEMO, "粉刷匠", "强");
            assertThat(exception.getDescription()).contains("粉刷匠");
        }

        @Test
        @DisplayName("抛出 - 带 extra 的异常")
        void throwBusinessExceptionWithExtra_ShouldWork() {
            String extra = "额外信息";
            BusinessException exception = new BusinessException(extra, BaseErrorCallbackCode.PARAM_ERROR);
            assertThat(exception.getExtra()).isEqualTo(extra);
        }

        @Test
        @DisplayName("异常堆栈信息")
        void exceptionStackTrace_ShouldContainMessage() {
            String description = "测试异常描述";
            BusinessException exception = new BusinessException(description);

            assertThat(exception.getMessage()).isEqualTo(description);
        }
    }

    @Nested
    @DisplayName("多语言占位符测试")
    class PlaceholderTests {

        @Test
        @DisplayName("单个占位符替换")
        void singlePlaceholder_ShouldReplaceCorrectly() {
            BusinessException exception = new BusinessException(
                    "CODE",
                    "错误信息: {0}",
                    "测试值"
            );

            assertThat(exception.getDescription()).isEqualTo("错误信息: 测试值");
        }

        @Test
        @DisplayName("多个占位符替换")
        void multiplePlaceholders_ShouldReplaceCorrectly() {
            BusinessException exception = new BusinessException(
                    "CODE",
                    "用户{0}的{1}无效",
                    "张三", "手机号"
            );

            assertThat(exception.getDescription()).isEqualTo("用户张三的手机号无效");
        }

        @Test
        @DisplayName("连续占位符")
        void consecutivePlaceholders_ShouldReplaceCorrectly() {
            BusinessException exception = new BusinessException(
                    "CODE",
                    "错误代码{0}：第{1}行，第{2}列",
                    "ERR001", "10", "5"
            );

            assertThat(exception.getDescription()).isEqualTo("错误代码ERR001：第10行，第5列");
        }

        @Test
        @DisplayName("带特殊字符的占位符")
        void placeholdersWithSpecialChars_ShouldReplaceCorrectly() {
            BusinessException exception = new BusinessException(
                    "CODE",
                    "金额{0}超出限制{1}",
                    "100.50", "99.99"
            );

            assertThat(exception.getDescription()).isEqualTo("金额100.50超出限制99.99");
        }
    }
}
