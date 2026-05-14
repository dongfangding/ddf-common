package com.ddf.boot.common.api.exception;

import lombok.Getter;

/**
 * <p>异常定义枚举</p >
 *
 * @author dongfang.ding
 * @version 1.0
 * @since 2020/06/28 13:14
 */
public enum BaseErrorCallbackCode implements BaseCallbackCode {

    /**
     * 异常状态码，可持续补充
     */
    // 枚举定义里也可以不指定状态码，这样就会使用每个自定义异常内的通用状态码 com.nvwa.boot.common.core.exception200.BaseException.defaultCallback
    /**
     * 用户异常体系状态码定义
     */
    COMMON_LOGIC_ERROR("COMMON_LOGIC_ERROR", "逻辑参数不正确"),
    /**
     * 演示异常占位符以及使用异常自己的通用状态码来标识code
     * throw new BusinessException(BaseErrorCallbackCode.FILL_PARAM_DEMO, "粉刷匠", "强");
     */
    FILL_PARAM_DEMO("FILL_PARAM_DEMO", "我是一个{0}， 粉刷本领{1}"),

    DEMO_BLA_BLA("DEMO_BLA_BLA", "系统内部出现问题，bla bla...", "系统异常，请稍后确认"),

    TEST_FILL_EXCEPTION("TEST_FILL_EXCEPTION", "带占位符的异常演示[{0}]"),

    TEST_FILL_BIZ_EXCEPTION("TEST_FILL_BIZ_EXCEPTION", "带占位符的异常演示[{0}],客户端隐藏详细信息", "报错啦"),

    PAGE_NUM_NOT_ALLOW_NULL("PAGE_NUM_NOT_ALLOW_NULL", "当前页数不能为空"),

    PAGE_SIZE_NOT_ALLOW_NULL("PAGE_SIZE_NOT_ALLOW_NULL", "每页大小不能为空"),

    ENUM_CODE_NOT_MAPPING("ENUM_CODE_NOT_MAPPING", "枚举代码未映射"),

    SIGN_ERROR("SIGN_ERROR", "签名校验失败，数据不合法"),

    SIGN_TIMESTAMP_ERROR("SIGN_TIMESTAMP_ERROR", "签名校验失败，数据已过期"),

    ILLEGAL_TOKEN("ILLEGAL_TOKEN", "非法Token"),

    MAIL_SEND_FAILURE("MAIL_SEND_FAILURE", "邮件发送失败"),
    SERIALIZE_PARAM_ERROR("JSON_SERIALIZER_FILED",
            "序列化参数失败， 请检查是否有入参对象无法序列化[com.nvwa.boot.common.core.util.AopUtil.getSerializableParamMap]"),

    JSON_SERIALIZER_FILED("JSON_SERIALIZER_FILED", "Json序列化失败"),
    JSON_DESERIALIZER_FILED("JSON_DESERIALIZER_FILED", "Json反序列化失败"),

    COMPLETE("200", "请求成功"),

    BAD_REQUEST("BAD_REQUEST", "错误请求", "错误请求"),

    RESOURCE_REQUEST_ERROR("RESOURCE_REQUEST_ERROR", "资源请求错误", "资源请求错误"),

    UNAUTHORIZED("401", "未通过认证"),

    ACCESS_FORBIDDEN("403", "权限未通过，访问被拒绝", "权限未通过，访问被拒绝"),

    SERVER_ERROR("SERVER_ERROR", "服务端异常", "请求失败，请联系客服人员~"),

    BIZ_EXCEPTION("BIZ_EXCEPTION", "业务异常"),
    UPLOAD_FILE_ERROR("UPLOAD_FILE_ERROR", "文件上传失败"),

    REDIS_KEY_ARGS_NOT_MATCH_TEMPLATE("REDIS_KEY_ARGS_NOT_MATCH_TEMPLATE", "redis key参数与模板不匹配"),
    REDIS_SHARDING_KEY_NOT_MATCH_ARGS("REDIS_SHARDING_KEY_NOT_MATCH_ARGS", "redis路由key定义变量与实际参数不匹配"),

    PARAM_ERROR("PARAM_ERROR", "参数错误"),
    PARAM_MISSING("PARAM_MISSING", "参数丢失"),
    ILLEGAL_PARAM("ILLEGAL_PARAM", "参数不合法"),
    ILLEGAL_REQUEST("ILLEGAL_REQUEST", "请求不合法"),

    DUPLICATE_KEY("DUPLICATE_KEY", "违反唯一约束", "已存在相同的记录"),

    ENTRY_NOT_EXISTS("ENTRY_NOT_EXISTS", "数据不存在", "操作的资源不存在"),

    ILLEGAL_OPERATE("ILLEGAL_OPERATE", "不允许的操作", "不允许执行该操作"),
    /**
     * 验证码异常
     */
    VERIFY_CODE_EXPIRED("VERIFY_CODE_EXPIRED", "验证码已过期"),
    VERIFY_CODE_NOT_MATCH("VERIFY_CODE_NOT_MATCH", "验证码不匹配"),
    DATA_EXPIRED("DATA_EXPIRED", "数据已过期，请重新尝试"),
    PLEASE_SLOW_DOWN("PLEASE_SLOW_DOWN", "手速太快啦~"),
    VERSION_CONTROL("VERSION_CONTROL", "请更新至最新版本再使用该功能!"),
    ILLEGAL_EMAIL("ILLEGAL_EMAIL", "邮箱格式不正确"),
    NO_SUPPORT_EMAIL_AREA("NO_SUPPORT_EMAIL_AREA", "不支持的邮箱区域"),
    ILLEGAL_PASSWORD("ILLEGAL_PASSWORD", "ILLEGAL_PASSWORD"),
    REQUEST_TOO_MANY("REQUEST_TOO_MANY", "请求已达上限"),
    USER_INFO_EXPIRED_OR_NOT_EXIST("USER_INFO_EXPIRED_OR_NOT_EXIST", "用户信息已失效，请重新登录"),



    ;

    /**
     * 异常code码
     */
    @Getter
    private final String code;

    /**
     * 异常消息
     *
     * @param description 描述信息
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
