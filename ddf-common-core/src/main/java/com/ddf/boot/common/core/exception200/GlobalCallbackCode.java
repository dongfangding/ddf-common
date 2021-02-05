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
    FILL_PARAM_DEMO("101", "我是一个{0}， 粉刷本领{1}"),


    RATE_LIMIT("999", "接口已限流"),

    REPEAT_SUBMIT("998", "操作频繁")

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


    public static void main(String[] args) {
        if (true) {
            try {
                throw new BadRequestException(GlobalCallbackCode.FILL_PARAM_DEMO, "粉刷匠", "强");
            } catch (BadRequestException exception) {
                exception.printStackTrace();
            }
        }
    }
}
