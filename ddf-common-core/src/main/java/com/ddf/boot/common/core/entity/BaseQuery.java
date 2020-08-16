package com.ddf.boot.common.core.entity;

import lombok.Data;

/**
 * <p>description</p >
 *
 * @author Snowball
 * @version 1.0
 * @date 2020/06/29 14:58
 */
@Data
public class BaseQuery {

    /**
     * 当前分页数
     */
    private Integer current = 1;

    /**
     * 每页显示条数
     */
    private Integer size = 10;
}
