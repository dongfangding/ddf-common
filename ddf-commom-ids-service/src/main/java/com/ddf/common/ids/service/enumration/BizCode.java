package com.ddf.common.ids.service.enumration;

import com.ddf.boot.common.core.exception200.BaseCallbackCode;
import lombok.AllArgsConstructor;
import lombok.Getter;

/**
 * <p>description</p >
 *
 * @author YUNTAO
 * @version 1.0
 * @date 2020/10/17 18:04
 */
@AllArgsConstructor
public enum BizCode implements BaseCallbackCode {

    /**
     * 生成雪花id失败
     */
    GEN_ID_FAILURE("14001", "生成id失败"),

    /**
     * biz tag 已存在
     */
    BIZ_TAG_HAS_EXIST("14002", "biz tag 已存在"),
    ;

    @Getter
    private final String code;

    @Getter
    private final String description;

}
