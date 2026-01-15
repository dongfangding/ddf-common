package com.ddf.boot.common.api.model.common.response;

import com.ddf.boot.common.api.exception.BaseErrorCallbackCode;
import com.ddf.boot.common.api.exception.BusinessException;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import java.util.Collections;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

/**
 * {@link ResponseData} 测试类
 *
 * @author dongfang.ding
 */
@DisplayName("ResponseData 测试")
class ResponseDataTest {

    @Nested
    @DisplayName("success 方法测试")
    class SuccessMethodTests {

        @Test
        @DisplayName("success(T data) - 成功返回数据")
        void successWithData_ShouldReturnSuccessResponse() {
            String testData = "test data";
            ResponseData<String> response = ResponseData.success(testData);

            assertThat(response.getCode()).isEqualTo(BaseErrorCallbackCode.COMPLETE.getCode());
            assertThat(response.getMessage()).isEqualTo(BaseErrorCallbackCode.COMPLETE.getCode());
            assertThat(response.getSubMessage()).isEqualTo(BaseErrorCallbackCode.COMPLETE.getDescription());
            assertThat(response.getData()).isEqualTo(testData);
            assertThat(response.getTimestamp()).isPositive();
            assertThat(response.isSuccess()).isTrue();
        }

        @Test
        @DisplayName("success(T data, String desc) - 成功返回数据自定义描述")
        void successWithDataAndDesc_ShouldReturnSuccessResponse() {
            String testData = "test data";
            String customDesc = "操作成功自定义描述";
            ResponseData<String> response = ResponseData.success(testData, customDesc);

            assertThat(response.getCode()).isEqualTo(BaseErrorCallbackCode.COMPLETE.getCode());
            assertThat(response.getMessage()).isEqualTo(BaseErrorCallbackCode.COMPLETE.getCode());
            assertThat(response.getSubMessage()).isEqualTo(customDesc);
            assertThat(response.getData()).isEqualTo(testData);
            assertThat(response.isSuccess()).isTrue();
        }

        @Test
        @DisplayName("success(T data, Object extra) - 成功返回数据带扩展信息")
        void successWithDataAndExtra_ShouldReturnSuccessResponse() {
            String testData = "test data";
            String extraInfo = "extra info";
            ResponseData<String> response = ResponseData.success(testData, (Object) extraInfo);

            assertThat(response.getCode()).isEqualTo(BaseErrorCallbackCode.COMPLETE.getCode());
            assertThat(response.getData()).isEqualTo(testData);
            assertThat(response.getExtra()).isEqualTo(extraInfo);
            assertThat(response.isSuccess()).isTrue();
        }

        @Test
        @DisplayName("empty() - 返回空数据")
        void empty_ShouldReturnEmptySuccessResponse() {
            ResponseData<Void> response = ResponseData.empty();

            assertThat(response.getCode()).isEqualTo(BaseErrorCallbackCode.COMPLETE.getCode());
            assertThat(response.getMessage()).isEqualTo(BaseErrorCallbackCode.COMPLETE.getCode());
            assertThat(response.getSubMessage()).isEqualTo(BaseErrorCallbackCode.COMPLETE.getDescription());
            assertThat(response.getData()).isNull();
            assertThat(response.isSuccess()).isTrue();
        }

        @Test
        @DisplayName("success(null) - 返回 null 数据")
        void successWithNull_ShouldReturnSuccessResponse() {
            ResponseData<String> response = ResponseData.success(null);

            assertThat(response.getCode()).isEqualTo(BaseErrorCallbackCode.COMPLETE.getCode());
            assertThat(response.getData()).isNull();
            assertThat(response.isSuccess()).isTrue();
        }
    }

    @Nested
    @DisplayName("failure 方法测试")
    class FailureMethodTests {

        @Test
        @DisplayName("failure(BaseCallbackCode) - 使用错误码枚举失败响应")
        void failureWithCallbackCode_ShouldReturnFailureResponse() {
            ResponseData<Object> response = ResponseData.failure(BaseErrorCallbackCode.PARAM_ERROR);

            assertThat(response.getCode()).isEqualTo(BaseErrorCallbackCode.PARAM_ERROR.getCode());
            assertThat(response.getMessage()).isEqualTo(BaseErrorCallbackCode.PARAM_ERROR.getBizMessage());
            assertThat(response.getSubMessage()).isEqualTo(BaseErrorCallbackCode.PARAM_ERROR.getDescription());
            assertThat(response.getData()).isNull();
            assertThat(response.isSuccess()).isFalse();
        }

        @Test
        @DisplayName("failure(BaseCallbackCode, Object extra) - 使用错误码枚举带扩展信息")
        void failureWithCallbackCodeAndExtra_ShouldReturnFailureResponse() {
            String extraInfo = "extra error info";
            ResponseData<Object> response = ResponseData.failure(BaseErrorCallbackCode.PARAM_ERROR, extraInfo);

            assertThat(response.getCode()).isEqualTo(BaseErrorCallbackCode.PARAM_ERROR.getCode());
            assertThat(response.getExtra()).isEqualTo(extraInfo);
            assertThat(response.isSuccess()).isFalse();
        }

        @Test
        @DisplayName("failure(String code, String message) - 使用字符串参数失败响应")
        void failureWithCodeAndMessage_ShouldReturnFailureResponse() {
            String code = "CUSTOM_CODE";
            String message = "自定义错误信息";
            ResponseData<Object> response = ResponseData.failure(code, message);

            assertThat(response.getCode()).isEqualTo(code);
            assertThat(response.getMessage()).isEqualTo(message);
            assertThat(response.getSubMessage()).isEqualTo(message);
            assertThat(response.getData()).isNull();
            assertThat(response.isSuccess()).isFalse();
        }

        @Test
        @DisplayName("failure(String code, String message, String subMessage) - 使用完整参数失败响应")
        void failureWithAllParams_ShouldReturnFailureResponse() {
            String code = "CUSTOM_CODE";
            String message = "用户可见错误信息";
            String subMessage = "详细错误信息（开发用）";
            ResponseData<Object> response = ResponseData.failure(code, message, subMessage);

            assertThat(response.getCode()).isEqualTo(code);
            assertThat(response.getMessage()).isEqualTo(message);
            assertThat(response.getSubMessage()).isEqualTo(subMessage);
            assertThat(response.getData()).isNull();
            assertThat(response.isSuccess()).isFalse();
        }

        @Test
        @DisplayName("failure 带 formatParams - 失败响应带格式化参数")
        void failureWithFormatParams_ShouldReturnFailureResponse() {
            String code = "CUSTOM_CODE";
            String message = "错误信息";
            String subMessage = "详细错误信息";
            Object extra = "extra";
            Object[] formatParams = new Object[]{"param1", "param2"};
            ResponseData<Object> response = ResponseData.failure(code, message, subMessage, extra, formatParams);

            assertThat(response.getCode()).isEqualTo(code);
            assertThat(response.getMessage()).isEqualTo(message);
            assertThat(response.getSubMessage()).isEqualTo(subMessage);
            assertThat(response.getExtra()).isEqualTo(extra);
            assertThat(response.getFormatParams()).isEqualTo(formatParams);
            assertThat(response.isSuccess()).isFalse();
        }
    }

    @Nested
    @DisplayName("isSuccess 方法测试")
    class IsSuccessMethodTests {

        @Test
        @DisplayName("isSuccess - 成功响应返回 true")
        void successResponse_ShouldReturnTrue() {
            ResponseData<String> response = ResponseData.success("data");
            assertThat(response.isSuccess()).isTrue();
        }

        @Test
        @DisplayName("isSuccess - 失败响应返回 false")
        void failureResponse_ShouldReturnFalse() {
            ResponseData<String> response = ResponseData.failure(BaseErrorCallbackCode.PARAM_ERROR);
            assertThat(response.isSuccess()).isFalse();
        }

        @Test
        @DisplayName("isSuccess - 空响应返回 true")
        void emptyResponse_ShouldReturnTrue() {
            ResponseData<Void> response = ResponseData.empty();
            assertThat(response.isSuccess()).isTrue();
        }
    }

    @Nested
    @DisplayName("requiredSuccess 方法测试")
    class RequiredSuccessMethodTests {

        @Test
        @DisplayName("requiredSuccess - 成功响应返回数据")
        void successResponse_ShouldReturnData() {
            String testData = "test data";
            ResponseData<String> response = ResponseData.success(testData);

            String result = response.requiredSuccess();

            assertThat(result).isEqualTo(testData);
        }

        @Test
        @DisplayName("requiredSuccess - 失败响应抛出 BusinessException")
        void failureResponse_ShouldThrowBusinessException() {
            ResponseData<String> response = ResponseData.failure(BaseErrorCallbackCode.PARAM_ERROR);

            assertThatThrownBy(response::requiredSuccess)
                    .isInstanceOf(BusinessException.class)
                    .hasMessageContaining(BaseErrorCallbackCode.PARAM_ERROR.getDescription());
        }

        @Test
        @DisplayName("requiredSuccess - 失败响应带格式化参数抛出异常")
        void failureResponseWithFormatParams_ShouldThrowBusinessException() {
            Object[] formatParams = new Object[]{"testParam"};
            ResponseData<String> response = ResponseData.failure(
                    "CODE", "错误信息{0}", "详细错误信息", null, formatParams
            );

            assertThatThrownBy(response::requiredSuccess)
                    .isInstanceOf(BusinessException.class);
        }
    }

    @Nested
    @DisplayName("failureDefault 方法测试")
    class FailureDefaultMethodTests {

        @Test
        @DisplayName("failureDefault - 成功响应返回原始数据")
        void successResponse_ShouldReturnOriginalData() {
            String testData = "test data";
            String defaultValue = "default";
            ResponseData<String> response = ResponseData.success(testData);

            String result = response.failureDefault(defaultValue);

            assertThat(result).isEqualTo(testData);
        }

        @Test
        @DisplayName("failureDefault - 失败响应返回默认值")
        void failureResponse_ShouldReturnDefaultValue() {
            String defaultValue = "default value";
            ResponseData<String> response = ResponseData.failure(BaseErrorCallbackCode.PARAM_ERROR);

            String result = response.failureDefault(defaultValue);

            assertThat(result).isEqualTo(defaultValue);
        }

        @Test
        @DisplayName("failureDefault - null 作为默认值")
        void failureResponseWithNullDefault_ShouldReturnNull() {
            ResponseData<String> response = ResponseData.failure(BaseErrorCallbackCode.PARAM_ERROR);

            String result = response.failureDefault(null);

            assertThat(result).isNull();
        }
    }

    @Nested
    @DisplayName("构造方法测试")
    class ConstructorTests {

        @Test
        @DisplayName("全参数构造 - 4参数")
        void constructorWithFourParams_ShouldCreateResponse() {
            ResponseData<String> response = new ResponseData<>(
                    "code", "message", "subMessage", System.currentTimeMillis(), "data"
            );

            assertThat(response.getCode()).isEqualTo("code");
            assertThat(response.getMessage()).isEqualTo("message");
            assertThat(response.getSubMessage()).isEqualTo("subMessage");
            assertThat(response.getData()).isEqualTo("data");
        }

        @Test
        @DisplayName("全参数构造 - 6参数带extra")
        void constructorWithSixParams_ShouldCreateResponse() {
            ResponseData<String> response = new ResponseData<>(
                    "code", "message", "subMessage", System.currentTimeMillis(), "data", "extra"
            );

            assertThat(response.getCode()).isEqualTo("code");
            assertThat(response.getMessage()).isEqualTo("message");
            assertThat(response.getData()).isEqualTo("data");
            assertThat(response.getExtra()).isEqualTo("extra");
        }

        @Test
        @DisplayName("全参数构造 - 7参数带formatParams")
        void constructorWithSevenParams_ShouldCreateResponse() {
            Object[] formatParams = new Object[]{"param1", "param2"};
            ResponseData<String> response = new ResponseData<>(
                    "code", "message", "subMessage", System.currentTimeMillis(), "data", "extra", formatParams
            );

            assertThat(response.getCode()).isEqualTo("code");
            assertThat(response.getData()).isEqualTo("data");
            assertThat(response.getExtra()).isEqualTo("extra");
            assertThat(response.getFormatParams()).isEqualTo(formatParams);
        }
    }

    @Nested
    @DisplayName("泛型类型测试")
    class GenericTypeTests {

        @Test
        @DisplayName("泛型 String 类型")
        void genericStringType_ShouldWork() {
            ResponseData<String> response = ResponseData.success("string data");
            assertThat(response.getData()).isInstanceOf(String.class);
        }

        @Test
        @DisplayName("泛型 Integer 类型")
        void genericIntegerType_ShouldWork() {
            ResponseData<Integer> response = ResponseData.success(123);
            assertThat(response.getData()).isInstanceOf(Integer.class);
        }

        @Test
        @DisplayName("泛型 List 类型")
        void genericListType_ShouldWork() {
            List<String> listData = Collections.singletonList("item");
            ResponseData<List<String>> response = ResponseData.success(listData);
            assertThat(response.getData()).isInstanceOf(List.class);
        }

        @Test
        @DisplayName("泛型自定义对象类型")
        void genericCustomObjectType_ShouldWork() {
            TestDataObject customObject = new TestDataObject("field1", "field2");
            ResponseData<TestDataObject> response = ResponseData.success(customObject);

            assertThat(response.getData()).isInstanceOf(TestDataObject.class);
            assertThat(response.getData().getField1()).isEqualTo("field1");
            assertThat(response.getData().getField2()).isEqualTo("field2");
        }

        /**
         * 测试用自定义对象
         */
        private static class TestDataObject {
            private final String field1;
            private final String field2;

            TestDataObject(String field1, String field2) {
                this.field1 = field1;
                this.field2 = field2;
            }

            public String getField1() {
                return field1;
            }

            public String getField2() {
                return field2;
            }
        }
    }
}
