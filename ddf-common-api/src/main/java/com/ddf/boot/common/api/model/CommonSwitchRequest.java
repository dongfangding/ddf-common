package com.ddf.boot.common.api.model;

import javax.validation.constraints.NotNull;
import lombok.Data;

/**
 * <p>通用开关型请求类， 使用0否1是来代表关和开，
 * 然后业务系统接收后，转换为对应的业务状态， 前端永远只关心是开还是关，不关心具体对应状态</p >
 *
 * @author dongfang.ding
 * @version 1.0
 * @date 2021/02/23 10:19
 */
@Data
public class CommonSwitchRequest {

    /**
     * id
     */
    @NotNull(message = "id不能为空")
    private Long id;

    /**
     * 通用开关标识 0 关 1 开
     * 如0 未激活  1 已激活
     * 如0 禁用 1 启用
     *
     */
    @NotNull(message = "开关标识不能为空")
    private Integer switchFlag;
}
