package com.ddf.boot.common.jwt.exception;

import com.ddf.boot.common.core.exception200.BaseCallbackCode;
import com.ddf.boot.common.core.exception200.BaseErrorCallbackCode;
import com.ddf.boot.common.core.exception200.BaseException;

/**
 * 用户信息丢失异常
 *
 * @author dongfang.ding
 * @date 2019-12-07 16:45
 */
public class UserClaimMissionException extends BaseException {

    public UserClaimMissionException(String message) {
        super(message);
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
