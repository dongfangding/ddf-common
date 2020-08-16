package com.ddf.boot.common.core.exception200;

/**
 * <p>访问被拒绝，通常用于权限判断</p >
 *
 * @author Snowball
 * @version 1.0
 * @date 2020/06/28 13:28
 */
public class AccessDeniedException extends BaseException {


    public AccessDeniedException(Throwable throwable) {
        super(throwable);
    }

    public AccessDeniedException(BaseCallbackCode baseCallbackCode) {
        super(baseCallbackCode);
    }


    public AccessDeniedException(String description) {
        super(description);
    }

    public AccessDeniedException(String code, String description) {
        super(code, description);
    }

    /**
     * 当前异常默认响应状态码
     *
     * @return
     */
    @Override
    public BaseCallbackCode defaultCallback() {
        return BaseErrorCallbackCode.ACCESS_FORBIDDEN;
    }
}
