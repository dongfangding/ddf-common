package com.ddf.boot.common.api.model.common.request;

import java.io.Serializable;
import javax.validation.constraints.NotNull;
import lombok.Data;

/**
 * <p>接口签名相关参数接口类
 * 未使用基础类， 让请求类继承的方式，而是通过接口
 * </p >
 *
 * @author rebot
 * @version 1.0
 * @date 2022/01/04 20:04
 */
public interface BaseSign extends Serializable {


    /**
     * 自己系统加签字段固定参数名
     */
    String SELF_SIGNATURE_FIELD = "sign";

    /**
     * 自己系统时间戳固定参数名
     */
    String SELF_TIMESTAMP_FIELD = "nonceTimestamp";

    /**
     * 获取签名摘要值
     *
     * @return
     */
    String getSign();

    /**
     * 获取请求时间戳， 用作简单重放判断
     *
     * @return
     */
    Long getNonceTimestamp();

    /**
     * <p>通用开关型请求类， 使用0否1是来代表关和开，
     * 然后业务系统接收后，转换为对应的业务状态， 前端永远只关心是开还是关，不关心具体对应状态</p >
     *
     * @author dongfang.ding
     * @version 1.0
     * @date 2021/02/23 10:19
     */
    @Data
    class CommonSwitchRequest {

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
}
