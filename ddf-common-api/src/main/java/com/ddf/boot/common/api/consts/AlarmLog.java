package com.ddf.boot.common.api.consts;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * <p>description</p >
 *
 * @author snowball
 * @version 1.0
 * @since 2024/01/06 00:09
 */
public class AlarmLog  {
    private static final Logger LOGGER = LoggerFactory.getLogger("ALARM_FILE");

    /**
     * Log a message at the DEBUG level.
     *
     * @param msg 消息内容
     */
    public static void debug(String msg) {
        LOGGER.debug(msg);
    }

    /**
     * Log a message at the DEBUG level according to the specified format
     * and argument.
     * <p/>
     * <p>This form avoids superfluous object creation when the logger
     * is disabled for the DEBUG level. </p>
     *
     * @param format 格式字符串
     * @param arg    参数值
     */
    public static void debug(String format, Object arg) {
        LOGGER.debug(format, arg);
    }

    /**
     * Log a message at the DEBUG level according to the specified format
     * and arguments.
     * <p/>
     * <p>This form avoids superfluous object creation when the logger
     * is disabled for the DEBUG level. </p>
     *
     * @param format 格式字符串
     * @param arg1   第一个参数
     * @param arg2   第二个参数
     */
    public static void debug(String format, Object arg1, Object arg2) {
        LOGGER.debug(format, arg1, arg2);
    }

    /**
     * Log a message at the DEBUG level according to the specified format
     * and arguments.
     * <p/>
     * <p>This form avoids superfluous string concatenation when the logger
     * is disabled for the DEBUG level. However, this variant incurs the hidden
     * (and relatively small) cost of creating an <code>Object[]</code> before invoking the method,
     * even if this logger is disabled for DEBUG. The variants taking
     * {@link #debug(String, Object) one} and {@link #debug(String, Object, Object) two}
     * arguments exist solely in order to avoid this hidden cost.</p>
     *
     * @param format    格式字符串
     * @param arguments 参数列表
     */
    public static void debug(String format, Object... arguments) {
        LOGGER.debug(format, arguments);
    }

    /**
     * Log an exception (throwable) at the DEBUG level with an
     * accompanying message.
     *
     * @param msg 消息内容
     * @param t   异常对象
     */
    public static void debug(String msg, Throwable t) {
        LOGGER.debug(msg, t);
    }


    /**
     * Log a message at the INFO level.
     *
     * @param msg 消息内容
     */
    public static void info(String msg) {
        LOGGER.info(msg);
    }

    /**
     * Log a message at the INFO level according to the specified format
     * and argument.
     * <p/>
     * <p>This form avoids superfluous object creation when the logger
     * is disabled for the INFO level. </p>
     *
     * @param format 格式字符串
     * @param arg    参数值
     */
    public static void info(String format, Object arg) {
        LOGGER.info(format, arg);
    }

    /**
     * Log a message at the INFO level according to the specified format
     * and arguments.
     * <p/>
     * <p>This form avoids superfluous object creation when the logger
     * is disabled for the INFO level. </p>
     *
     * @param format 格式字符串
     * @param arg1   第一个参数
     * @param arg2   第二个参数
     */
    public static void info(String format, Object arg1, Object arg2) {
        LOGGER.info(format, arg1, arg2);
    }

    /**
     * Log a message at the INFO level according to the specified format
     * and arguments.
     * <p/>
     * <p>This form avoids superfluous string concatenation when the logger
     * is disabled for the INFO level. However, this variant incurs the hidden
     * (and relatively small) cost of creating an <code>Object[]</code> before invoking the method,
     * even if this logger is disabled for INFO. The variants taking
     * {@link #info(String, Object) one} and {@link #info(String, Object, Object) two}
     * arguments exist solely in order to avoid this hidden cost.</p>
     *
     * @param format    格式字符串
     * @param arguments 参数列表
     */
    public static void info(String format, Object... arguments) {
        LOGGER.info(format, arguments);
    }

    /**
     * Log an exception (throwable) at the INFO level with an
     * accompanying message.
     *
     * @param msg 消息内容
     * @param t   异常对象
     */
    public static void info(String msg, Throwable t) {
        LOGGER.info(msg, t);
    }


    /**
     * Log a message at the WARN level.
     *
     * @param msg 消息内容
     */
    public static void warn(String msg) {
        LOGGER.warn(msg);
    }

    /**
     * Log a message at the WARN level according to the specified format
     * and argument.
     * <p/>
     * <p>This form avoids superfluous object creation when the logger
     * is disabled for the WARN level. </p>
     *
     * @param format 格式字符串
     * @param arg    参数值
     */
    public static void warn(String format, Object arg) {
        LOGGER.warn(format, arg);
    }

    /**
     * Log a message at the WARN level according to the specified format
     * and arguments.
     * <p/>
     * <p>This form avoids superfluous string concatenation when the logger
     * is disabled for the WARN level. However, this variant incurs the hidden
     * (and relatively small) cost of creating an <code>Object[]</code> before invoking the method,
     * even if this logger is disabled for WARN. The variants taking
     * {@link #warn(String, Object) one} and {@link #warn(String, Object, Object) two}
     * arguments exist solely in order to avoid this hidden cost.</p>
     *
     * @param format    格式字符串
     * @param arguments 参数列表
     */
    public static void warn(String format, Object... arguments) {
        LOGGER.warn(format, arguments);
    }

    /**
     * Log a message at the WARN level according to the specified format
     * and arguments.
     * <p/>
     * <p>This form avoids superfluous object creation when the logger
     * is disabled for the WARN level. </p>
     *
     * @param format 格式字符串
     * @param arg1   第一个参数
     * @param arg2   第二个参数
     */
    public static void warn(String format, Object arg1, Object arg2) {
        LOGGER.warn(format, arg1, arg2);
    }

    /**
     * Log an exception (throwable) at the WARN level with an
     * accompanying message.
     *
     * @param msg 消息内容
     * @param t   异常对象
     */
    public static void warn(String msg, Throwable t) {
        LOGGER.warn(msg, t);
    }


    /**
     * Log a message at the ERROR level.
     *
     * @param msg 消息内容
     */
    public static void error(String msg) {
        LOGGER.error(msg);
    }

    /**
     * Log a message at the ERROR level according to the specified format
     * and argument.
     * <p/>
     * <p>This form avoids superfluous object creation when the logger
     * is disabled for the ERROR level. </p>
     *
     * @param format 格式字符串
     * @param arg    参数值
     */
    public static void error(String format, Object arg) {
        LOGGER.error(format, arg);
    }

    /**
     * Log a message at the ERROR level according to the specified format
     * and arguments.
     * <p/>
     * <p>This form avoids superfluous object creation when the logger
     * is disabled for the ERROR level. </p>
     *
     * @param format 格式字符串
     * @param arg1   第一个参数
     * @param arg2   第二个参数
     */
    public static void error(String format, Object arg1, Object arg2) {
        LOGGER.error(format, arg1, arg2);
    }

    /**
     * Log a message at the ERROR level according to the specified format
     * and arguments.
     * <p/>
     * <p>This form avoids superfluous string concatenation when the logger
     * is disabled for the ERROR level. However, this variant incurs the hidden
     * (and relatively small) cost of creating an <code>Object[]</code> before invoking the method,
     * even if this logger is disabled for ERROR. The variants taking
     * {@link #error(String, Object) one} and {@link #error(String, Object, Object) two}
     * arguments exist solely in order to avoid this hidden cost.</p>
     *
     * @param format    格式字符串
     * @param arguments 参数列表
     */
    public static void error(String format, Object... arguments) {
        LOGGER.error(format, arguments);
    }

    /**
     * Log an exception (throwable) at the ERROR level with an
     * accompanying message.
     *
     * @param msg 消息内容
     * @param t   异常对象
     */
    public static void error(String msg, Throwable t) {
        LOGGER.error(msg, t);
    }
}
