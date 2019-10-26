package com.ddf.common.websocket.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.ddf.common.websocket.model.entity.MerchantBaseDevice;
import org.apache.ibatis.annotations.Param;

/**
 * @author dongfang.ding
 * @date 2019/8/21 14:56
 */
public interface MerchantBaseDeviceMapper extends BaseMapper<MerchantBaseDevice> {
    /**
     * 查询指定业务组在线设备的云闪付账户信息
     * @since 2019年08月24日
     * @author honglin.jiang
     * @param merchantId
     * @return
     */

    /**描述: 绑定设备
    *@Param: [number]
    *@return: com.yk.pay.message.ws.model.datao.MerchantBaseDevice
    *@Author: wangchi
    *@date: 2019/8/29
    */
    MerchantBaseDevice getBaseDeviceByNumber(@Param("number") String number);

    /**
     * 同步设备的版本
     * 
     * @param deviceNumber
     * @param token
     * @return
     * @author dongfang.ding
     * @date 2019/9/24 17:15
     */
    int syncVersionList(@Param("deviceNumber") String deviceNumber, @Param("token") String token);
}
