package com.ddf.boot.common.core.logbool;

import lombok.Data;
import lombok.experimental.Accessors;

/**
 * <p>记录布尔返回值的返回数据类，可以消费这个数据做日志</p >
 *
 * @author dongfang.ding
 * @version 1.0
 * @date 2020/06/12 15:35
 */
@Data
@Accessors(chain = true)
public class LogBoolReturnResult {

    private BoolReturn boolReturn;

    /**
     * 日志名称
     */
    private String logName;

    /**
     * 执行类名
     */
    private String className;

    /**
     * 执行方法名
     */
    private String methodName;

    /**
     * 参数json格式
     */
    private String param;
}
