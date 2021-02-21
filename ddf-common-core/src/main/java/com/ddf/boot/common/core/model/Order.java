package com.ddf.boot.common.core.model;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.data.domain.Sort;


/**
 * mybatis 排序对象
 *
 * @author snowball
 *
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class Order {

    private String[] column;

    private Sort.Direction direction;
}
