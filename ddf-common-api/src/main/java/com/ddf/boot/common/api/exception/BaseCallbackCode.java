package com.ddf.boot.common.api.exception;

/**
 * <p>异常消息代码统一接口</p >
 *
 * @author dongfang.ding
 * @version 1.0
 * @date 2020/06/17 14:58
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
}
