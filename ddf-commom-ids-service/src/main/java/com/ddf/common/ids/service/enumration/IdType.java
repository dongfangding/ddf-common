package com.ddf.common.ids.service.enumration;

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
public enum IdType {

    /**
     * 雪花
     */
    SNOW(1, "雪花"),

    /**
     * 分段
     */
    SEGMENT(2, "分段"),

    /**
     * 递增
     */
    INCREMENT(3, "递增"),

    ;

    @Getter
    private int code;

    @Getter
    private String message;
}
