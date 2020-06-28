package com.ddf.boot.common.exception200;

/**
 * <p>异常定义枚举</p >
 *
 * @author Snowball
 * @version 1.0
 * @date 2020/06/28 13:14
 */
public enum BaseErrorCallbackCode implements BaseCallbackCode {

    /**
     * 异常状态码，可持续补充
     */
    COMPLETE("200", "请求成功"),

    BAD_REQUEST("400", "错误请求"),

    UNAUTHORIZED("401", "未通过认证"),

    ACCESS_FORBIDDEN("403", "权限未通过，访问被拒绝"),

    SERVER_ERROR("500", "服务端异常"),


    ;


    /**
     * 异常code码
     */
    private final String code;

    /**
     * 异常消息
     */
    private final String description;

    BaseErrorCallbackCode(String code, String description) {
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
