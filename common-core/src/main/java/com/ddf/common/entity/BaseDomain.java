package com.ddf.common.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;

import java.util.Date;

/**
 */
@Data
public class BaseDomain {
    @TableId(type = IdType.AUTO)
    protected Long id;

    @TableField(value = "CREATE_BY", fill = FieldFill.INSERT)
    protected String createBy;

    @TableField(value = "CREATE_TIME", fill = FieldFill.INSERT)
    protected Date createTime;

    @TableField(value = "MODIFY_BY", fill = FieldFill.INSERT_UPDATE)
    protected String modifyBy;

    @TableField(value = "MODIFY_TIME", fill = FieldFill.INSERT_UPDATE)
    protected Date modifyTime;

    @TableLogic
    protected Integer removed = 0;

    @Version
    @TableField(fill = FieldFill.INSERT)
    protected Integer version;

}