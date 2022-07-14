package com.ddf.boot.common.core.exception200;

/**
 * <p>用户账户体系异常定义</p >
 *
 * @author dongfang.ding
 * @version 1.0
 * @date 2020/06/28 15:07
 */
public enum GlobalCallbackCode implements BaseCallbackCode {

    // 枚举定义里也可以不指定状态码，这样就会使用每个自定义异常内的通用状态码 com.ddf.boot.common.core.exception200.BaseException.defaultCallback
    /**
     * 用户异常体系状态码定义
     */
    COMMON_LOGIC_ERROR("400", "逻辑参数不正确"),
    /**
     * 演示异常占位符以及使用异常自己的通用状态码来标识code
     * throw new BusinessException(GlobalCallbackCode.FILL_PARAM_DEMO, "粉刷匠", "强");
     */
    FILL_PARAM_DEMO("fill_param_demo", "我是一个{0}， 粉刷本领{1}"),

    ENUM_CODE_NOT_MAPPING("enum_code_not_mapping", "枚举代码未映射"),

    SIGN_ERROR("sign_error", "签名校验失败，数据不合法"),

    SIGN_TIMESTAMP_ERROR("sign_timestamp_error", "签名校验失败，数据已过期"),

    MAIL_SEND_FAILURE("mail_send_failure", "邮件发送失败"),
    SERIALIZE_PARAM_ERROR("序列化参数失败， 请检查是否有入参对象无法序列化[com.ddf.boot.common.core.util.AopUtil.getSerializableParamMap]")

    ;

    /**
     * 异常code码
     */
    private final String code;

    /**
     * 异常消息
     */
    private final String description;

    GlobalCallbackCode(String description) {
        this.code = null;
        this.description = description;
    }


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
