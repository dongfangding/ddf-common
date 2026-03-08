package com.ddf.boot.common.api.exception;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * <p>异常消息代码统一接口</p >
 *
 * @author dongfang.ding
 * @version 1.0
 * @since 2020/06/17 14:58
 */
public interface BaseCallbackCode {

    /**
     * 响应状态码
     *
     * @return
     */
    String getCode();

    /**
     * 响应消息
     *
     * @return
     */
    String getDescription();

    /**
     * 响应业务消息, 最终返回给用户的，如果需要隐藏系统异常细节，要记得重写这个方法
     *
     * @return
     */
    default String getBizMessage() {
        return getDescription();
    }

    @Data
    @NoArgsConstructor
    @AllArgsConstructor(staticName = "of")
    class DefaultBaseCallbackCode implements BaseCallbackCode {

        private String code;
        private String description;
        private String bizMessage;


        @Override
        public String getCode() {
            return code;
        }

        @Override
        public String getDescription() {
            return description;
        }

        @Override
        public String getBizMessage() {
            return bizMessage;
        }
        /**
         * @param code 参数
         * @param description 参数
         */
        public static BaseCallbackCode of(String code, String description) {
            return of(code, description, description);
        }
    }
}