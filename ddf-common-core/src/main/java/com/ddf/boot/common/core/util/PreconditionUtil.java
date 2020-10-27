package com.ddf.boot.common.core.util;

import com.ddf.boot.common.core.exception200.BadRequestException;
import com.ddf.boot.common.core.exception200.BaseCallbackCode;
import com.ddf.boot.common.core.exception200.BusinessException;

import javax.validation.ConstraintViolation;
import javax.validation.Validation;
import javax.validation.Validator;
import java.util.Iterator;
import java.util.Set;

/**
 * <p>提供断言，抛出系统自定义异常信息</p >
 *
 * @author dongfang.ding
 * @version 1.0
 * @date 2020/10/23 18:38
 */
public class PreconditionUtil {

    /**
     * Validator instances can be pooled and shared by the implementation.
     * 这个东西不缓存下来，并发一上来，tomcat线程会刷刷的创建然后blocked，非常非常非常影响qps
     */
    private static final Validator VALIDATOR = Validation.buildDefaultValidatorFactory().getValidator();

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


    /**
     * 提供一种手动式的必传参数校验
     * @param request
     */
    public static <T> void requiredParamCheck(T request) {
        Set<ConstraintViolation<T>> constraintViolations = VALIDATOR.validate(request);
        if (constraintViolations.size() == 0) {
            return;
        }
        Iterator<ConstraintViolation<T>> iterator = constraintViolations.iterator();
        throw new BadRequestException(iterator.next().getMessage());
    }
}
