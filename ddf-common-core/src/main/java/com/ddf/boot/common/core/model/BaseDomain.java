package com.ddf.boot.common.core.model;

import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.experimental.SuperBuilder;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.Id;

/**
 * 通用基类
 *
 * @author dongfang.ding
 */
@Data
@SuperBuilder(toBuilder = true)
@NoArgsConstructor
public class BaseDomain {


    @Id
    protected Long id;

    /**
     * 创建时间
     */
    @CreatedDate
    protected Long gmtCreated;

    /**
     * 修改时间
     */
    protected Long gmtModified;
}
