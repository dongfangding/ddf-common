package com.ddf.common.ons.console.constant;

/**
 * <p>幂等性控制操作</p >
 *
 * @author Snowball
 * @version 1.0
 * @date 2021/05/18 18:06
 */
public enum IdempotentActionEnum {

    /**
     * NEVER 不需要幂等性控制
     * SYSTEM 系统控制幂等性
     * SELF   业务自实现幂等性控制
     */
    NEVER,
    SYSTEM,
    SELF
}
