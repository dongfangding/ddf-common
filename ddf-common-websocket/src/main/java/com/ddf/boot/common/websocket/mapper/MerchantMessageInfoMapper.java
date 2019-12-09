package com.ddf.boot.common.websocket.mapper;


import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.ddf.boot.common.websocket.model.entity.MerchantMessageInfo;
import org.springframework.data.repository.query.Param;

import java.util.List;

/**
 * 云闪付收款到账消息Mapper接口
 * @author xujinquan
 * @create 2019-08-21
 */
public interface MerchantMessageInfoMapper extends BaseMapper<MerchantMessageInfo> {

    /**
     * 根据tradeNo去重插入
     * @param merchantMessageInfo
     * @return
     */
    int ignoreSave(@Param("message") MerchantMessageInfo merchantMessageInfo);


    /**
     * 根据tradeNo批量去重插入
     * @param merchantMessageInfoList
     * @return
     */
    int batchIgnoreSave(@Param("messages") List<MerchantMessageInfo> merchantMessageInfoList);
}
