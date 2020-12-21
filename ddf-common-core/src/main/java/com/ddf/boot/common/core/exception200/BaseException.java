package com.ddf.boot.common.core.exception200;

import java.text.MessageFormat;

/**
 * <p>基准异常类</p >
 *
 * @author dongfang.ding
 * @version 1.0
 * @date 2020/06/17 15:55
 */
public abstract class BaseException extends RuntimeException implements BaseCallbackCode {

    /**
     * 异常code码
     */
    private String code;

    /**
     * 异常消息
     */
    private String description;

    /**
     * 如果使用国际化的话，由于消息会提前定义在资源文件中， 某些消息需要提供占位符希望运行时填充数据，这里可以传入占位符对应的参数
     */
    private Object[] params;

    public BaseException() {

    }

    public BaseException(Throwable throwable) {
        super(throwable);
        initCallback(defaultCallback());
    }

    public BaseException(BaseCallbackCode baseCallbackCode) {
        super(baseCallbackCode.getDescription());
        initCallback(baseCallbackCode);
    }


    public BaseException(String description) {
        super(description);
        initCallback(defaultCallback());
    }

    public BaseException(String code, String description) {
        super(description);
        initCallback(code, description);
    }

    public BaseException(String code, String description, Object... params) {
        super(MessageFormat.format(description, params));
        initCallback(code, description, params);
    }

    /**
     * 提供一种消息占位符的方式， baseCallbackCode中的message包含占位符， 使用的时候格式化参数后作为最终异常消息
     *
     * @param baseCallbackCode
     * @param params
     */
    public BaseException(BaseCallbackCode baseCallbackCode, Object... params) {
        super(MessageFormat.format(baseCallbackCode.getDescription(), params));
        initCallback(baseCallbackCode.getCode(), baseCallbackCode.getDescription(), params);
    }


    private void initCallback(BaseCallbackCode baseCallbackCode) {
        initCallback(
                baseCallbackCode.getCode() == null ? defaultCallback().getCode() : baseCallbackCode.getCode(),
                baseCallbackCode.getDescription()
        );
    }


    /**
     * 初始化状态码
     *
     * @param code
     * @param description
     */
    private void initCallback(String code, String description, Object... params) {
        this.code = code == null ? defaultCallback().getCode() : code;
        this.params = params;
        this.description = MessageFormat.format(description, params);
    }


    /**
     * 当前异常默认响应状态码
     *
     * @return
     */
    public abstract BaseCallbackCode defaultCallback();

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

    public Object[] getParams() {
        return params;
    }

    public static void main(String[] args) {

    }
}
