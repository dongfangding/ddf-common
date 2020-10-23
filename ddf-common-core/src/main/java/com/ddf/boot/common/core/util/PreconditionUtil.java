package com.ddf.boot.common.core.util;

import com.ddf.boot.common.core.exception200.BaseCallbackCode;
import com.ddf.boot.common.core.exception200.BusinessException;

/**
 * <p>提供断言，抛出系统自定义异常信息</p >
 *
 * @author dongfang.ding
 * @version 1.0
 * @date 2020/10/23 18:38
 */
public class PreconditionUtil {

    /**
     * 检查参数
     *
     * @param expression
     * @param message
     */
    public static void checkArgument(boolean expression, String message) {
        if (!expression) {
            throw new BusinessException(message);
        }
    }

    /**
     * 检查参数
     * @param expression
     * @param code
     * @param message
     */
    public static void checkArgument(boolean expression, String code, String message) {
        if (!expression) {
            throw new BusinessException(code, message);
        }
    }

    /**
     * 检查参数
     *
     * @param expression
     * @param callbackCode
     */
    public static void checkArgument(boolean expression, BaseCallbackCode callbackCode) {
        if (!expression) {
            throw new BusinessException(callbackCode);
        }
    }
}
