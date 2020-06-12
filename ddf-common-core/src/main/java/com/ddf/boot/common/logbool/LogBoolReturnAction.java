package com.ddf.boot.common.logbool;

/**
 * <p>提供一个接口返回记录布尔返回值的返回数据类</p >
 *
 * @author Snowball
 * @version 1.0
 * @date 2020/06/12 15:40
 */
public interface LogBoolReturnAction {

    /**
     * 消费LogBoolReturnResult
     * @param logBoolReturnResult
     */
    void doAction(LogBoolReturnResult logBoolReturnResult);
}
