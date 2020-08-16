package com.ddf.boot.common.core.exception200;

/**
 * <p>基准异常类</p >
 *
 * @author Snowball
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


    private void initCallback(BaseCallbackCode baseCallbackCode) {
        initCallback(baseCallbackCode.getCode(), baseCallbackCode.getDescription());
    }


    /**
     * 初始化状态码
     * @param code
     * @param description
     */
    private void initCallback(String code, String description) {
        this.code = code;
        this.description = description;
    }


    /**
     * 当前异常默认响应状态码
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

}
