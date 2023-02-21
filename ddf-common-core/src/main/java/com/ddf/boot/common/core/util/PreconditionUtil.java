package com.ddf.boot.common.core.util;

import com.ddf.boot.common.api.exception.BadRequestException;
import com.ddf.boot.common.api.exception.BaseCallbackCode;
import com.ddf.boot.common.api.exception.BaseErrorCallbackCode;
import com.ddf.boot.common.api.exception.BusinessException;
import java.text.MessageFormat;
import java.util.Iterator;
import java.util.Objects;
import java.util.Set;
import javax.validation.ConstraintViolation;
import javax.validation.Validation;
import javax.validation.Validator;
import org.springframework.lang.NonNull;

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
    private static final Validator VALIDATOR = Validation.buildDefaultValidatorFactory()
            .getValidator();

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
     *
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
     * 校验参数抛出外部传入运行时异常
     *
     * @param expression
     * @param exception
     */
    public static void checkArgument(boolean expression, RuntimeException exception) {
        if (!expression) {
            throw exception;
        }
    }

    /**
     * 检查参数并格式化占位符消息
     *
     * @param expression
     * @param callbackCode
     * @param args
     */
    static void checkArgumentAndFormat(boolean expression, @NonNull BaseCallbackCode callbackCode, Object... args) {
        checkArgument(expression, callbackCode.getCode(), MessageFormat.format(callbackCode.getDescription(), args));
    }


    /**
     * 检查参数
     *
     * @param expression
     * @param message
     */
    public static void checkBadRequest(boolean expression, String message) {
        if (!expression) {
            throw new BusinessException("400", message);
        }
    }

    /**
     * 提供一种手动式的必传参数校验
     *
     * @param request
     */
    public static <T> void requiredParamCheck(T request) {
        PreconditionUtil.checkArgument(
                Objects.nonNull(request), BaseErrorCallbackCode.BAD_REQUEST
        );
        Set<ConstraintViolation<T>> constraintViolations = VALIDATOR.validate(request);
        if (constraintViolations.size() == 0) {
            return;
        }
        Iterator<ConstraintViolation<T>> iterator = constraintViolations.iterator();
        throw new BadRequestException(iterator.next().getMessage());
    }
}
