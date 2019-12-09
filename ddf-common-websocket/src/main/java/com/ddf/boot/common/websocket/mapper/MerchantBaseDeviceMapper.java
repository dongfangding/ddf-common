package com.ddf.boot.common.websocket.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.ddf.boot.common.websocket.model.entity.MerchantBaseDevice;
import org.apache.ibatis.annotations.Param;

/**
 * @author dongfang.ding
 * @date 2019/8/21 14:56
 */
public interface MerchantBaseDeviceMapper extends BaseMapper<MerchantBaseDevice> {
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
