package com.ddf.boot.common.api.exception;

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
    // 枚举定义里也可以不指定状态码，这样就会使用每个自定义异常内的通用状态码 com.ddf.boot.common.core.exception200.BaseException.defaultCallback
    /**
     * 用户异常体系状态码定义
     */
    COMMON_LOGIC_ERROR("COMMON_LOGIC_ERROR", "逻辑参数不正确"),
    /**
     * 演示异常占位符以及使用异常自己的通用状态码来标识code
     * throw new BusinessException(BaseErrorCallbackCode.FILL_PARAM_DEMO, "粉刷匠", "强");
     */
    FILL_PARAM_DEMO("FILL_PARAM_DEMO", "我是一个{0}， 粉刷本领{1}"),

    DEMO_BLA_BLA("base_0001", "系统内部出现问题，bla bla...", "系统异常，请稍后确认"),

    TEST_FILL_EXCEPTION("base_0002", "带占位符的异常演示[{0}]"),

    TEST_FILL_BIZ_EXCEPTION("base_0003", "带占位符的异常演示[{0}],客户端隐藏详细信息", "报错啦"),

    PAGE_NUM_NOT_ALLOW_NULL("base_0004", "当前页数不能为空"),

    PAGE_SIZE_NOT_ALLOW_NULL("base_0005", "每页大小不能为空"),

    ENUM_CODE_NOT_MAPPING("ENUM_CODE_NOT_MAPPING", "枚举代码未映射"),

    SIGN_ERROR("SIGN_ERROR", "签名校验失败，数据不合法"),

    SIGN_TIMESTAMP_ERROR("SIGN_TIMESTAMP_ERROR", "签名校验失败，数据已过期"),

    MAIL_SEND_FAILURE("MAIL_SEND_FAILURE", "邮件发送失败"),
    SERIALIZE_PARAM_ERROR("JSON_SERIALIZER_FILED", "序列化参数失败， 请检查是否有入参对象无法序列化[com.ddf.boot.common.core.util.AopUtil.getSerializableParamMap]"),

    JSON_SERIALIZER_FILED("JSON_SERIALIZER_FILED", "Json序列化失败"),
    JSON_DESERIALIZER_FILED("JSON_DESERIALIZER_FILED", "Json反序列化失败"),

    COMPLETE("200", "请求成功"),

    BAD_REQUEST("BAD_REQUEST", "错误请求"),

    UNAUTHORIZED("401", "未通过认证"),

    ACCESS_FORBIDDEN("403", "权限未通过，访问被拒绝"),

    SERVER_ERROR("500", "服务端异常"),
    UPLOAD_FILE_ERROR("UPLOAD_FILE_ERROR", "文件上传失败")

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

    BaseErrorCallbackCode(String description) {
        this.code = null;
        this.description = description;
        this.bizMessage = description;
    }

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