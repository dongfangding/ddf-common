package com.ddf.boot.common.core.exception200;

/**
 * <p>未通过认证异常，通常用于登录认证</p >
 *
 * @author Snowball
 * @version 1.0
 * @date 2020/06/28 13:30
 */
public class UnauthorizedException extends BaseException {

    public UnauthorizedException(Throwable throwable) {
        super(throwable);
    }

    public UnauthorizedException(BaseCallbackCode baseCallbackCode) {
        super(baseCallbackCode);
    }


    public UnauthorizedException(String description) {
        super(description);
    }

    public UnauthorizedException(String code, String description) {
        super(code, description);
    }

    /**
     * 当前异常默认响应状态码
     *
     * @return
     */
    @Override
    public BaseCallbackCode defaultCallback() {
        return BaseErrorCallbackCode.UNAUTHORIZED;
    }
}
