package com.ddf.boot.common.exception200;

/**
 * <p>错误请求，通常用于处理接口请求</p >
 *
 * @author Snowball
 * @version 1.0
 * @date 2020/06/28 13:14
 */
public class BadRequestException extends BaseException {

    public BadRequestException(Throwable throwable) {
        super(throwable);
    }

    public BadRequestException(BaseCallbackCode baseCallbackCode) {
        super(baseCallbackCode);
    }


    public BadRequestException(String description) {
        super(description);
    }

    public BadRequestException(String code, String description) {
        super(code, description);
    }


    /**
     * 当前异常默认响应状态码
     *
     * @return
     */
    @Override
    public BaseCallbackCode defaultCallback() {
        return BaseErrorCallbackCode.BAD_REQUEST;
    }
}
