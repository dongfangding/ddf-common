package com.ddf.boot.common.core.model;

import lombok.AllArgsConstructor;
import lombok.Data;

/**
 * <p>description</p >
 *
 * @author Snowball
 * @version 1.0
 * @date 2021/01/15 14:51
 */
@Data
@AllArgsConstructor
public class NumberDTO {

    /**
     * count
     */
    private Long count;


    public static NumberDTO of(long count) {
        return new NumberDTO(count);
    }
}
