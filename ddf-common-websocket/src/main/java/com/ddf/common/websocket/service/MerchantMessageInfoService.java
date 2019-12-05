package com.ddf.common.websocket.service;


import com.baomidou.mybatisplus.extension.service.IService;
import com.ddf.common.websocket.enumerate.CmdEnum;
import com.ddf.common.websocket.model.entity.MerchantBaseDevice;
import com.ddf.common.websocket.model.entity.MerchantMessageInfo;
import com.ddf.common.websocket.model.ws.AuthPrincipal;
import com.ddf.common.websocket.model.ws.Message;
import com.ddf.common.websocket.model.ws.MessageWrapper;
import com.ddf.common.websocket.model.ws.ParseContent;

import javax.validation.constraints.NotNull;
import java.util.Date;
import java.util.List;
import java.util.Map;

/**
 * 云闪付收款到账消息服务类
 */
public interface MerchantMessageInfoService extends IService<MerchantMessageInfo> {

    /**
     * 插入云闪付到账消息，包含云闪付到账消息，账单消息， 短信消息
     * 
     * @param authPrincipal
     * @param parseContent
     * @return
     * @author dongfang.ding
     * @date 2019/9/7 14:24 
     */
    int insertByParseContent(AuthPrincipal authPrincipal, ParseContent parseContent, CmdEnum cmd);


    /**
     * 根据tradeNo获取消息记录
     * @param tradeNo
     * @param cmd
     * @return
     */
    MerchantMessageInfo getByTradeNo(String tradeNo, CmdEnum cmd);


    /**
     * 接收短信数据保存到message_info中，对报文中的列表数据进行分开存储，除了需要解析trade_no去重，其它任何非报文数据
     * 都不进行处理直接保存。
     * 根据trade_no去重，只向调用方返回真正插入的数据
     * @param message
     * @param payload
     * @param authPrincipal
     * @return
     * @author dongfang.ding
     */
    MessageWrapper<?, List<MerchantMessageInfo>> insertMessageInfoByBankSms(Message message
            ,List<Map<String, Object>> payload, AuthPrincipal authPrincipal);


    /**
     * 接收短信数据保存到message_info中，对报文中的列表数据进行分开存储，除了需要解析trade_no去重，其它任何非报文数据
     * 都不进行处理直接保存。
     * 根据trade_no去重，只向调用方返回真正插入的数据
     * @param message
     * @param payload
     * @param authPrincipal
     * @return
     * @author dongfang.ding
     */
    MessageWrapper<String, MerchantMessageInfo> insertMessageInfoByUPayMessage(Message message
            ,Map<String, Object> payload, AuthPrincipal authPrincipal);


    /**
     * 接收云闪付账单记录数据保存到message_info中，对报文中的列表数据进行分开存储，除了需要解析trade_no去重，其它任何非报文数据
     * 都不进行处理直接保存。
     * 根据trade_no去重，只向调用方返回真正插入的数据
     * @param message
     * @param payload
     * @param authPrincipal
     * @return
     * @author dongfang.ding
     */
    MessageWrapper<List<Map<String, Object>>, List<MerchantMessageInfo>> insertMessageInfoByUPayBillOrder(
            Message message, List<Map<String, Object>> payload, AuthPrincipal authPrincipal);


    /**
     * 处理接收到的短信，填充业务数据和状态
     * @param messageInfo
     * @param merchantBaseDevice
     * @return
     */
    boolean fillStatus(MerchantMessageInfo messageInfo, MerchantBaseDevice merchantBaseDevice);

    /**
     * 处理接收到的短信，填充业务数据和状态
     * @param messageInfo
     * @param merchantBaseDevice
     * @param tradeNo
     * @param receiveTime
     * @return
     * @author dongfang.ding
     */
    boolean fillStatus(MerchantMessageInfo messageInfo, MerchantBaseDevice merchantBaseDevice, String tradeNo
            , Date receiveTime);


    /**
     * 对处理的数据快速更新失败原因
     * @param messageInfos
     * @param status
     * @param errorMessage
     * @return
     * @author dongfang.ding
     */
    boolean fastFailure(List<MerchantMessageInfo> messageInfos, Byte status, String errorMessage);

    /**
     *  获取需要重试的数据
     * 
     * @param
     * @return
     * @author dongfang.ding
     * @date 2019/9/25 16:09 
     */
    List<MerchantMessageInfo> getRetryInfos();

    /**
     * 检查当前订单是否存在其它渠道处理成功的数据
     * @param orderId
     * @param currSourceType
     * @return
     */
    boolean checkOtherMessageSuccess(@NotNull String orderId, @NotNull Byte currSourceType);
}
