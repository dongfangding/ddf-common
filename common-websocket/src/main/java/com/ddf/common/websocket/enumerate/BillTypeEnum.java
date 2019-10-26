package com.ddf.common.websocket.enumerate;

import java.io.Serializable;

/**
 * 账单类型，入账和支出
 *
 * @author dongfang.ding
 * @date 2019/10/16 10:56
 */
public enum BillTypeEnum implements Serializable {

    /** 入账 */
    INCOME,

    /**
     * 支出
     */
    PAY

}
