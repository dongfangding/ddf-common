package com.ddf.boot.common.core.exception200;

import lombok.Getter;

/**
 * <p>异常定义枚举</p >
 *
 * @author dongfang.ding
 * @version 1.0
 * @date 2020/06/28 13:14
 */
public enum BaseErrorCallbackCode implements BaseCallbackCode {

    /**
     * 异常状态码，可持续补充
     */
    DEMO_BLA_BLA("base_0001", "系统内部出现问题，bla bla...", "系统异常，请稍后确认"),

    TEST_FILL_EXCEPTION("base_0002", "带占位符的异常演示[{0}]"),

    TEST_FILL_BIZ_EXCEPTION("base_0003", "带占位符的异常演示[{0}],客户端隐藏详细信息", "报错啦"),

    PAGE_NUM_NOT_ALLOW_NULL("base_0004", "当前页数不能为空"),

    PAGE_SIZE_NOT_ALLOW_NULL("base_0005", "每页大小不能为空"),

    COMPLETE("200", "请求成功"),

    BAD_REQUEST("400", "错误请求"),

    UNAUTHORIZED("401", "未通过认证"),

    ACCESS_FORBIDDEN("403", "权限未通过，访问被拒绝"),

    SERVER_ERROR("500", "服务端异常"),

    ;

    /**
     * 异常code码
     */
    @Getter
    private final String code;

    /**
     * 异常消息
     */
    @Getter
    private final String description;

    /**
     * 返回给用户的异常消息
     */
    @Getter
    private final String bizMessage;

    BaseErrorCallbackCode(String code, String description) {
        this.code = code;
        this.description = description;
        this.bizMessage = description;
    }

    BaseErrorCallbackCode(String code, String description, String bizMessage) {
        this.code = code;
        this.description = description;
        this.bizMessage = bizMessage;
    }
}
