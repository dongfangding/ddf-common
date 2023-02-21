package com.ddf.boot.common.trace.context;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;


/**
 *
 * 存储用户身份相关信息
 *
 * @author dongfang.ding
 * @date 2021/8/24 10:35
 **/
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class Identity {

    /**
     * 平台
     */
    private String os;

    /**
     * 设备编号
     */
    private String imei;

    /**
     * 用户id
     */
    private Integer uid;

    public static Identity empty() {
        return new Identity();
    }
}
