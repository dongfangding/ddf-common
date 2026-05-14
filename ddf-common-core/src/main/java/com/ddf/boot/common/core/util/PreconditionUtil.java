package com.ddf.boot.common.core.util;

import com.ddf.boot.common.api.exception.BadRequestException;
import com.ddf.boot.common.api.exception.BaseCallbackCode;
import com.ddf.boot.common.api.exception.BaseErrorCallbackCode;
import com.ddf.boot.common.api.exception.BaseException;
import com.ddf.boot.common.api.exception.BusinessException;
import jakarta.validation.ConstraintViolation;
import jakarta.validation.Validation;
import jakarta.validation.Validator;
import jakarta.validation.ValidatorFactory;
import java.text.MessageFormat;
import java.util.Iterator;
import java.util.Objects;
import java.util.Set;
import org.springframework.lang.NonNull;

/**
 * <p>提供断言，抛出系统自定义异常信息</p >
 *
 * @author dongfang.ding
 * @version 1.0
 * @since 2020/10/23 18:38
 */
public class PreconditionUtil {

    /**
     * Validator 实例。
     * Validator instances can be pooled and shared by the implementation.
     * 使用静态块初始化，并确保 Factory 被正确关闭或由容器管理。
     * 这个东西不缓存下来，并发一上来，tomcat线程会刷刷的创建然后blocked，非常非常非常影响qps
     */
    private static final Validator VALIDATOR;

    static {
        try (ValidatorFactory factory = Validation.buildDefaultValidatorFactory()) {
            VALIDATOR = factory.getValidator();
        }
    }


    /**
     * 检查参数
     *
     * @param expression expression参数
     * @param message 消息内容
     */
    public static void checkArgument(boolean expression, String message) {
        if (!expression) {
            throw new BusinessException(message);
        }
    }

    /**
     * 检查参数
     *
     * @param expression expression参数
     * @param code 编码值
     * @param message 消息内容
     */
    public static void checkArgument(boolean expression, String code, String message) {
        if (!expression) {
            throw new BusinessException(code, message);
        }
    }

    /**
     * 检查参数
     *
     * @param expression expression参数
     * @param callbackCode callback编码
     */
    public static void checkArgument(boolean expression, BaseCallbackCode callbackCode) {
        if (!expression) {
            throw new BusinessException(callbackCode);
        }
    }

    /**
     * 检查参数
     *
     * @param expression expression参数
     * @param baseException 参数
     */
    public static void checkArgument(boolean expression, BaseException baseException) {
        if (!expression) {
            throw baseException;
        }
    }

    /**
     * 校验参数抛出外部传入运行时异常
     *
     * @param expression expression参数
     * @param exception 异常对象
     */
    public static void checkArgument(boolean expression, RuntimeException exception) {
        if (!expression) {
            throw exception;
        }
    }

    /**
     * 检查参数并格式化占位符消息
     *
     * @param expression expression参数
     * @param callbackCode callback编码
     * @param args 方法入参数组
     */
    public static void checkArgumentAndFormat(boolean expression, @NonNull BaseCallbackCode callbackCode,
            Object... args) {
        checkArgument(expression, callbackCode.getCode(), MessageFormat.format(callbackCode.getDescription(), args));
    }


    /**
     * 检查参数
     *
     * @param expression expression参数
     * @param message 消息内容
     */
    public static void checkBadRequest(boolean expression, String message) {
        if (!expression) {
            throw new BadRequestException(message);
        }
    }

    /**
     * 提供一种手动式的必传参数校验
     *
     * @param request 请求对象
     */
    public static <T> void requiredParamCheck(T request) {
        PreconditionUtil.checkArgument(Objects.nonNull(request), BaseErrorCallbackCode.BAD_REQUEST);
        Set<ConstraintViolation<T>> constraintViolations = VALIDATOR.validate(request);
        if (constraintViolations.size() == 0) {
            return;
        }
        Iterator<ConstraintViolation<T>> iterator = constraintViolations.iterator();
        throw new BadRequestException(iterator.next().getMessage());
    }
}
