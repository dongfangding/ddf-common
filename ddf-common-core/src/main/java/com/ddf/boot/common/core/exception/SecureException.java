package com.ddf.boot.common.core.exception;

import com.ddf.boot.common.api.exception.BaseCallbackCode;
import com.ddf.boot.common.api.exception.BaseErrorCallbackCode;
import com.ddf.boot.common.api.exception.BaseException;

/**
 * 安全相关异常
 * <p>
 * 用于密钥未配置、加密失败等安全场景
 * </p>
 *
 * @author dongfang.ding
 * @date 2025/1/14
 */
public class SecureException extends BaseException {

    private static final long serialVersionUID = 1L;

    public SecureException(String message) {
        super(message);
    }

    public SecureException(String code, String message) {
        super(code, message);
    }

    public SecureException(BaseCallbackCode baseCallbackCode) {
        super(baseCallbackCode);
    }

    public SecureException(BaseCallbackCode baseCallbackCode, String message) {
        super(baseCallbackCode, message);
    }

    @Override
    public BaseCallbackCode defaultCallback() {
        return BaseErrorCallbackCode.SERVER_ERROR;
    }

    @Override
    public boolean isMaskErrorDetails() {
        return true;
    }
}
