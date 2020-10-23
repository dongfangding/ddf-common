package com.ddf.boot.common.core.exception200;

/**
 * <p>用户账户体系异常定义</p >
 *
 * @author dongfang.ding
 * @version 1.0
 * @date 2020/06/28 15:07
 */
public enum GlobalCallbackCode implements BaseCallbackCode {

    /**
     * 用户异常体系状态码定义
     */
    COMMON_LOGIC_ERROR("400", "逻辑参数不正确"),


    ;

    /**
     * 异常code码
     */
    private final String code;

    /**
     * 异常消息
     */
    private final String description;

    GlobalCallbackCode(String code, String description) {
        this.code = code;
        this.description = description;
    }

    /**
     * 响应状态码
     *
     * @return
     */
    @Override
    public String getCode() {
        return code;
    }

    /**
     * 响应消息
     *
     * @return
     */
    @Override
    public String getDescription() {
        return description;
    }
}
