package com.ddf.boot.common.websocket.exception;

import com.ddf.boot.common.exception.GlobalCustomizeException;

/**
 * 无效状态数据异常，非成功状态皆为无效数据
 *
 * @author dongfang.ding
 * @date 2019/10/22 16:25
 */
public class InvalidStatusException extends GlobalCustomizeException {


    public InvalidStatusException(String message) {
        super(message);
    }
}
