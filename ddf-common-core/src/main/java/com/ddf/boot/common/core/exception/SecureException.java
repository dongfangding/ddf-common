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
 * @since 2025/1/14
 */
public class SecureException extends BaseException {
    private static final long serialVersionUID = 1L;
    public SecureException(String message) {
        super(message);
    }
    /**
     * @param code 参数
     * @param message 参数
     */
    public SecureException(String code, String message) {
        super(code, message);
    }
    /**
     * @param baseCallbackCode 回调码对象
     */
    public SecureException(BaseCallbackCode baseCallbackCode) {
        super(baseCallbackCode);
    }
    /**
     * @param baseCallbackCode 回调码对象
     * @param message 参数
     */
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