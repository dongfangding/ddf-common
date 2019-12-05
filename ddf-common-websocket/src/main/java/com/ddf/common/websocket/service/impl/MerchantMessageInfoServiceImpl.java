package com.ddf.common.websocket.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.conditions.update.LambdaUpdateWrapper;
import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.ddf.common.util.JsonUtil;
import com.ddf.common.util.StringUtil;
import com.ddf.common.websocket.enumerate.CmdEnum;
import com.ddf.common.websocket.exception.MessageFormatInvalid;
import com.ddf.common.websocket.helper.CmdStrategyHelper;
import com.ddf.common.websocket.mapper.MerchantMessageInfoMapper;
import com.ddf.common.websocket.model.entity.MerchantBaseDevice;
import com.ddf.common.websocket.model.entity.MerchantMessageInfo;
import com.ddf.common.websocket.model.ws.*;
import com.ddf.common.websocket.service.MerchantBaseDeviceService;
import com.ddf.common.websocket.service.MerchantMessageInfoService;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.time.DateUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import javax.validation.constraints.NotNull;
import java.util.*;

/**
 * 云闪付收款到账消息 服务实现类
 */
@Service
@Slf4j
public class MerchantMessageInfoServiceImpl extends ServiceImpl<MerchantMessageInfoMapper, MerchantMessageInfo> implements MerchantMessageInfoService {

    @Autowired
    private MerchantBaseDeviceService merchantBaseDeviceService;
    @Autowired
    private MerchantMessageInfoMapper merchantMessageInfoMapper;

    /**
     * 插入云闪付到账消息，包含云闪付到账消息，账单消息， 短信消息
     *
     * @param authPrincipal
     * @param parseContent
     * @return
     * @author dongfang.ding
     * @date 2019/9/7 14:24
     */
    @Override
    @Transactional(rollbackFor = Exception.class)
    public int insertByParseContent(AuthPrincipal authPrincipal, ParseContent parseContent, CmdEnum cmd) {
        if (authPrincipal == null || parseContent == null) {
            return 0;
        }
        MerchantBaseDevice baseDevice = merchantBaseDeviceService.getByAuthPrincipal(authPrincipal);
        if (baseDevice == null) {
            return 0;
        }
        MerchantMessageInfo merchantMessageInfo = new MerchantMessageInfo();
        merchantMessageInfo.setMerchantId(baseDevice.getMerchantId());
        merchantMessageInfo.setDeviceId(baseDevice.getId());
        merchantMessageInfo.setDeviceNumber(baseDevice.getNumber());
        merchantMessageInfo.setCmd(cmd.name());
        merchantMessageInfo.setTradeNo(parseContent.getTradeNo());
        merchantMessageInfo.setDescription(parseContent.buildMessage());
        merchantMessageInfo.setReceiveTime(parseContent.getOrderTime());
        return merchantMessageInfoMapper.ignoreSave(merchantMessageInfo);
    }

    /**
     * 根据tradeNo获取消息记录
     *
     * @param tradeNo
     * @return
     */
    @Override
    public MerchantMessageInfo getByTradeNo(String tradeNo, CmdEnum cmd) {
        if (StringUtils.isBlank(tradeNo)) {
            return null;
        }
        LambdaQueryWrapper<MerchantMessageInfo> queryWrapper = Wrappers.lambdaQuery();
        queryWrapper.eq(MerchantMessageInfo::getTradeNo, tradeNo);
        queryWrapper.eq(MerchantMessageInfo::getCmd, cmd);
        return getOne(queryWrapper);
    }

    /**
     * 接收短信数据保存到message_info中，对报文中的列表数据进行分开存储。除了需要解析trade_no去重，其它任何非报文数据
     * 都不进行处理直接保存。
     * 根据trade_no去重，只向调用方返回真正插入的数据
     *
     * @param message
     * @param payload
     * @param authPrincipal
     * @return
     * @author dongfang.ding
     * @date 2019/9/20 10:55
     */
    @Override
    @Transactional(rollbackFor = Exception.class)
    public MessageWrapper<List<Map<String, Object>>, List<MerchantMessageInfo>> insertMessageInfoByBankSms(Message message,
                                                                                                           List<Map<String, Object>> payload, AuthPrincipal authPrincipal) {
        if (message == null || payload == null || authPrincipal == null) {
            return null;
        }
        List<MerchantMessageInfo> infoList = new ArrayList<>();
        List<String> tradeNoList = new ArrayList<>(payload.size());
        SmsContent smsContent;
        String primaryKey = "";
        List<Map<String, Object>> returnClientMap = new ArrayList<>(payload.size());
        for (Map<String, Object> sms : payload) {
            MerchantMessageInfo merchantMessageInfo = initMessageInfo(message, authPrincipal, sms);
            try {
                if (sms == null) {
                    log.error("传输数据不能为空!! requestId: [{}]", message.getRequestId());
                    continue;
                }
                // 必须有短信id才能保证后面的数据能够对应上，没有短信id的数据不落此表，从报文日志中查
                primaryKey = sms.get("primaryKey") + "";
                if (StringUtils.isBlank(primaryKey)) {
                    log.error("报文中短信id不能为空!! requestId: [{}]", message.getRequestId());
                    continue;
                }
                tradeNoList.add(primaryKey);
                merchantMessageInfo.setTradeNo(primaryKey);
                infoList.add(merchantMessageInfo);
                try {
                    smsContent = JsonUtil.toBean(JsonUtil.asString(sms), SmsContent.class);
                } catch (Exception e) {
                    throw new MessageFormatInvalid("报文格式有误！");
                }
                if (StringUtils.isBlank(smsContent.getPrimaryKey())) {
                    throw new MessageFormatInvalid("短信唯一标识符不能为空!");
                }
                if (smsContent.getReceiveTime() == null) {
                    throw new MessageFormatInvalid("收件时间格式有误！");
                }
                if (StringUtils.isAnyBlank(smsContent.getCredit(), smsContent.getPrimaryKey(),
                        smsContent.getContent(), smsContent.getReceiveTime() + "")) {
                    throw new MessageFormatInvalid("短信发送方标识，短信id, 短信内容, 收件时间不能为空!");
                }
                merchantMessageInfo.setDescription(smsContent.getContent());
                returnClientMap.add(CmdStrategyHelper.buildSmsSuccessMap(primaryKey));
            } catch (MessageFormatInvalid messageFormatInvalid) {
                log.error("解析报文出错！", messageFormatInvalid);
                merchantMessageInfo.setStatus(MerchantMessageInfo.STATUS_DATA_INVALID);
                merchantMessageInfo.setErrorMessage(messageFormatInvalid.getMessage());
                merchantMessageInfo.setErrorStack(StringUtil.exceptionToString(messageFormatInvalid));
                returnClientMap.add(CmdStrategyHelper.buildSmsErrorMap(primaryKey, messageFormatInvalid.getMessage()));
            }
        }
        merchantMessageInfoMapper.batchIgnoreSave(infoList);
        List<MerchantMessageInfo> validBusinessData = getValidBusinessData(infoList, tradeNoList);
        return MessageWrapper.withBusiness(Message.buildMessage(message, returnClientMap,
                MessageResponse.SERVER_CODE_RECEIVED), validBusinessData);
    }

    /**
     * 接收短信数据保存到message_info中，对报文中的列表数据进行分开存储，除了需要解析trade_no去重，其它任何非报文数据
     * 都不进行处理直接保存。
     * 根据trade_no去重，只向调用方返回真正插入的数据
     *
     * @param message
     * @param payload
     * @param authPrincipal
     * @return
     * @author dongfang.ding
     */
    @Override
    public MessageWrapper<String, MerchantMessageInfo> insertMessageInfoByUPayMessage(Message message,
            Map<String, Object> payload, AuthPrincipal authPrincipal) {
        if (message == null || payload == null || authPrincipal == null) {
            return null;
        }
        MerchantMessageInfo merchantMessageInfo = initMessageInfo(message, authPrincipal, payload);
        boolean isError = false;
        UPayMessage uPayMessage = null;
        try {
            try {
                uPayMessage = JsonUtil.toBean(JsonUtil.asString(payload), UPayMessage.class);
            } catch (Exception e) {
                log.error("报文格式有误！", e);
                throw new MessageFormatInvalid("报文格式有误！");
            }
            if (StringUtils.isAnyBlank(uPayMessage.getContent(), uPayMessage.getOrderId())) {
                throw new MessageFormatInvalid("云闪付通知内容、订单号不能为空!");
            }
            if (uPayMessage.getOrderTime() == null) {
                throw new MessageFormatInvalid("云闪付通知时间不能为空!");
            }
            merchantMessageInfo.setTradeNo(uPayMessage.getOrderId());
            merchantMessageInfo.setDescription(uPayMessage.getContent());
        } catch (MessageFormatInvalid messageFormatInvalid) {
            isError = true;
            log.error("解析报文出错！", messageFormatInvalid);
            merchantMessageInfo.setStatus(MerchantMessageInfo.STATUS_DATA_INVALID);
            merchantMessageInfo.setErrorMessage(messageFormatInvalid.getMessage());
            merchantMessageInfo.setErrorStack(StringUtil.exceptionToString(messageFormatInvalid));
        }
        merchantMessageInfoMapper.ignoreSave(merchantMessageInfo);
        if (isError) {
            return MessageWrapper.withBusiness(Message.buildMessage(message, merchantMessageInfo.getErrorMessage(),
                    MessageResponse.SERVER_CODE_ERROR), null);
        }
        MerchantMessageInfo validBusinessData = getValidBusinessData(merchantMessageInfo, uPayMessage.getOrderId(),
                message.getCmd());
        return MessageWrapper.withBusiness(Message.success(message), validBusinessData);
    }

    /**
     * 接收短信数据保存到message_info中，对报文中的列表数据进行分开存储，除了需要解析trade_no去重，其它任何非报文数据
     * 都不进行处理直接保存。
     * 根据trade_no去重，只向调用方返回真正插入的数据
     *
     * @param message
     * @param payload
     * @param authPrincipal
     * @return
     * @author dongfang.ding
     */
    @Override
    public MessageWrapper<List<Map<String, Object>>, List<MerchantMessageInfo>> insertMessageInfoByUPayBillOrder(Message message,
            List<Map<String, Object>> payload, AuthPrincipal authPrincipal) {
        if (message == null || payload == null || payload.isEmpty() || authPrincipal == null) {
            return null;
        }
        List<MerchantMessageInfo> infoList = new ArrayList<>();
        UPayBill uPayBill;
        List<String> tradeNoList = new ArrayList<>(payload.size());
        List<Map<String, Object>> returnClientMap = new ArrayList<>(payload.size());
        String tradeNo = null;
        String billType;
        Integer qrCodeType;
        for (Map<String, Object> item : payload) {
            MerchantMessageInfo merchantMessageInfo = initMessageInfo(message, authPrincipal, item);
            try {
                if (item == null) {
                    log.error("传输数据不能为空!! requestId: [{}]", message.getRequestId());
                    continue;
                }
                // 必须有云闪付的订单号才能保证后面的数据能够对应上，没有的数据不落此表，从报文日志中查
                tradeNo = item.get("tradeno") + "";
                if (StringUtils.isBlank(tradeNo)) {
                    log.error("报文中的订单号不能为空!! requestId: [{}]", message.getRequestId());
                    throw new MessageFormatInvalid("报文中的订单号不能为空!");
                }
                if (item.get("billType") == null || StringUtils.isBlank(item.get("billType") + "")) {
                    log.error("报文中的billType不能为空!! requestId: [{}]", message.getRequestId());
                    throw new MessageFormatInvalid("报文中的billType不能为空!!");
                }
                billType = item.get("billType") + "";
                if (item.get("qrCodeType") == null || StringUtils.isBlank(item.get("qrCodeType") + "")) {
                    log.error("报文中的qrCodeType不能为空!! requestId: [{}]", message.getRequestId());
                    throw new MessageFormatInvalid("报文中的qrCodeType不能为空!! ");
                }
                qrCodeType = Integer.parseInt(item.get("qrCodeType") + "");
                tradeNoList.add(tradeNo);
                infoList.add(merchantMessageInfo);
                merchantMessageInfo.setTradeNo(tradeNo);
                try {
                    uPayBill = JsonUtil.toBean(JsonUtil.asString(item), UPayBill.class);
                } catch (Exception e) {
                    throw new MessageFormatInvalid("报文格式有误！");
                }
                if (uPayBill.getAmount() == null) {
                    throw new MessageFormatInvalid(String.format("订单号[%s]的金额不能为空", tradeNo));
                }
                CmdStrategyHelper.checkTimeFormat(uPayBill.getOrderTime());
                returnClientMap.add(CmdStrategyHelper.buildUPayBillOrderSuccessMap(tradeNo, billType, qrCodeType));
            } catch (Exception messageFormatInvalid) {
                log.error("解析报文出错！", messageFormatInvalid);
                merchantMessageInfo.setStatus(MerchantMessageInfo.STATUS_DATA_INVALID);
                merchantMessageInfo.setErrorMessage(messageFormatInvalid.getMessage());
                merchantMessageInfo.setErrorStack(StringUtil.exceptionToString(messageFormatInvalid, 100));
                returnClientMap.add(CmdStrategyHelper.buildUPayBillOrderErrorMap(tradeNo, messageFormatInvalid.getMessage()));
            }
        }
        if (infoList.isEmpty()) {
            return MessageWrapper.withBusiness(Message.buildMessage(message, returnClientMap,
                    MessageResponse.SERVER_CODE_RECEIVED), null);
        }
        merchantMessageInfoMapper.batchIgnoreSave(infoList);
        List<MerchantMessageInfo> validBusinessData = getValidBusinessData(infoList, tradeNoList);
        return MessageWrapper.withBusiness(Message.buildMessage(message, returnClientMap,
                MessageResponse.SERVER_CODE_RECEIVED), validBusinessData);
    }

    /**
     * 处理接收到的短信，填充业务数据和状态
     *
     * @param messageInfo
     * @param merchantBaseDevice
     * @return
     * @author dongfang.ding
     */
    @Override
    public boolean fillStatus(MerchantMessageInfo messageInfo, MerchantBaseDevice merchantBaseDevice) {
        return fillStatus(messageInfo, merchantBaseDevice, null, null);
    }

    /**
     * 处理接收到的短信，填充业务数据和状态
     *
     * @param messageInfo
     * @param merchantBaseDevice
     * @return
     * @author dongfang.ding
     */
    @Override
    @Transactional(rollbackFor = Exception.class)
    public boolean fillStatus(MerchantMessageInfo messageInfo, MerchantBaseDevice merchantBaseDevice,
                              String tradeNo, Date receiveTime) {
        if (messageInfo == null || merchantBaseDevice == null) {
            return false;
        }
        LambdaUpdateWrapper<MerchantMessageInfo> updateWrapper = Wrappers.lambdaUpdate();
        updateWrapper.eq(MerchantMessageInfo::getId, messageInfo.getId());
        // 乐观锁控制只有状态为未处理、模板未匹配、业务处理出错的才处理
        updateWrapper.in(MerchantMessageInfo::getStatus, MerchantMessageInfo.STATUS_NOT_DEAL,
                MerchantMessageInfo.STATUS_NOT_MATCH_TEMPLATE, MerchantMessageInfo.STATUS_LOGIC_ERROR);
        updateWrapper.set(MerchantMessageInfo::getStatus, messageInfo.getStatus());
        updateWrapper.set(MerchantMessageInfo::getErrorMessage, messageInfo.getErrorMessage() == null ? "" :
                messageInfo.getErrorMessage().length() > 2000 ? messageInfo.getErrorMessage().substring(0, 2000) : messageInfo.getErrorMessage());
        updateWrapper.set(MerchantMessageInfo::getErrorStack, messageInfo.getErrorStack());
        updateWrapper.set(MerchantMessageInfo::getDeviceId, merchantBaseDevice.getId());
        updateWrapper.set(MerchantMessageInfo::getMerchantId, merchantBaseDevice.getMerchantId());
        updateWrapper.set(MerchantMessageInfo::getDescription, messageInfo.getDescription());
        updateWrapper.set(MerchantMessageInfo::getOrderId, messageInfo.getOrderId());
        if (messageInfo.getSourceType() == null) {
            messageInfo.setSourceType(MerchantMessageInfo.SOURCE_TYPE_UNKNOWN);
        } else {
            updateWrapper.set(MerchantMessageInfo::getSourceType, messageInfo.getSourceType());
        }
        if (tradeNo != null && receiveTime != null) {
            updateWrapper.set(MerchantMessageInfo::getTradeNo, tradeNo);
            updateWrapper.set(MerchantMessageInfo::getReceiveTime, receiveTime);
        }
        return update(updateWrapper);
    }

    /**
     * 根据订单号或短信id获取报文中数据对应在数据库中的真实数据，然后过滤掉非本次保存并且格式不合法的数据；
     *
     * @param infoList
     * @param tradeNoList
     * @author dongfang.ding
     */
    private List<MerchantMessageInfo> getValidBusinessData(List<MerchantMessageInfo> infoList, List<String> tradeNoList) {
        // 由于插入时根据短信id做了重复忽略处理，实际上可能是没有插入的；还要根据trade_no重新查一次
        LambdaQueryWrapper<MerchantMessageInfo> messageQuery = Wrappers.lambdaQuery();
        messageQuery.in(MerchantMessageInfo::getTradeNo, tradeNoList);
        // 这才是这一批次的数据对应数据库中的记录（包含新增和已经存在的）
        List<MerchantMessageInfo> actualData = list(messageQuery);

        if (actualData == null || actualData.isEmpty()) {
            return null;
        }

        // 将最新新增的数据暴露出去，以供调用方使用,忽略的数据不需要处理
        List<MerchantMessageInfo> insertData = new ArrayList<>();
        for (MerchantMessageInfo item : actualData) {
            // 如果数据格式无效就不要给调用方浪费无用功处理了
            if (Objects.equals(MerchantMessageInfo.STATUS_DATA_INVALID, item.getStatus())) {
                continue;
            }
            for (MerchantMessageInfo value : infoList) {
                // 两个id相等，说明这条数据是最新插入的
                if (item.getId().equals(value.getId())) {
                    insertData.add(value);
                }
            }
        }
        return insertData;
    }


    /**
     * 根据订单号或短信id获取报文中数据对应在数据库中的真实数据，然后过滤掉非本次保存并且格式不合法的数据；
     *
     * @param messageInfo
     * @param tradeNo
     * @author dongfang.ding
     */
    private MerchantMessageInfo getValidBusinessData(MerchantMessageInfo messageInfo, String tradeNo, CmdEnum cmd) {
        // 由于插入时根据短信id做了重复忽略处理，实际上可能是没有插入的；还要根据trade_no重新查一次
        LambdaQueryWrapper<MerchantMessageInfo> messageQuery = Wrappers.lambdaQuery();
        messageQuery.eq(MerchantMessageInfo::getTradeNo, tradeNo);
        messageQuery.eq(MerchantMessageInfo::getCmd, cmd);
        // 这才是这一批次的数据对应数据库中的记录（包含新增和已经存在的）
        MerchantMessageInfo actualData = getOne(messageQuery);

        // 不存在或数据格式无效不需要返回给调用方做业务处理
        if (actualData == null || Objects.equals(MerchantMessageInfo.STATUS_DATA_INVALID, actualData.getStatus())) {
            return null;
        }

        // 说明这条数据是新增的
        if (actualData.getId().equals(messageInfo.getId())) {
            return actualData;
        }

        return null;
    }


    /**
     * 初始化MessageInfo基本数据
     *
     * @param message
     * @param authPrincipal
     * @param payload
     * @return
     * @author dongfang.ding
     */
    private MerchantMessageInfo initMessageInfo(Message message, AuthPrincipal authPrincipal, Map<String, Object> payload) {
        MerchantMessageInfo merchantMessageInfo = new MerchantMessageInfo();
        merchantMessageInfo.setRequestId(message.getRequestId());
        merchantMessageInfo.setDeviceNumber(authPrincipal.getIme());
        merchantMessageInfo.setCmd(message.getCmd().name());
        merchantMessageInfo.setSourceType(MerchantMessageInfo.SOURCE_TYPE_UNKNOWN);
        merchantMessageInfo.setSingleMessagePayload(JsonUtil.asString(payload));
        merchantMessageInfo.setStatus(MerchantMessageInfo.STATUS_NOT_DEAL);
        merchantMessageInfo.setErrorMessage(null);
        return merchantMessageInfo;
    }

    /**
     * 对处理的数据快速更新失败原因
     *
     * @param messageInfos
     * @param status
     * @param errorMessage
     * @return
     * @author dongfang.ding
     */
    @Override
    @Transactional(rollbackFor = Exception.class)
    public boolean fastFailure(List<MerchantMessageInfo> messageInfos, Byte status, String errorMessage) {
        if (messageInfos == null || messageInfos.isEmpty() || status == null) {
            return false;
        }
        for (MerchantMessageInfo merchantMessageInfo : messageInfos) {
            merchantMessageInfo.setStatus(status);
            merchantMessageInfo.setErrorMessage(errorMessage);
        }
        return updateBatchById(messageInfos);
    }

    /**
     * 重试未匹配模板消息
     *
     * @param
     * @return
     * @author dongfang.ding
     * @date 2019/9/25 16:09
     */
    @Override
    public List<MerchantMessageInfo> getRetryInfos() {
        LambdaQueryWrapper<MerchantMessageInfo> queryWrapper = Wrappers.lambdaQuery();
        queryWrapper.in(MerchantMessageInfo::getStatus, MerchantMessageInfo.STATUS_NOT_MATCH_TEMPLATE,
                MerchantMessageInfo.STATUS_NOT_DEAL, MerchantMessageInfo.STATUS_LOGIC_ERROR);
        queryWrapper.isNotNull(MerchantMessageInfo::getRequestId);
        queryWrapper.isNotNull(MerchantMessageInfo::getDeviceNumber);
        Date date = new Date();
        Date beforeDay = DateUtils.addDays(date, -7);
        queryWrapper.between(MerchantMessageInfo::getCreateTime, beforeDay, date);
        return list(queryWrapper);
    }

    /**
     * 检查当前订单是否存在其它渠道处理成功的数据
     *
     * @param orderId
     * @param currSourceType
     * @return
     */
    @Override
    public boolean checkOtherMessageSuccess(@NotNull String orderId, @NotNull Byte currSourceType) {
        if (StringUtils.isAnyBlank(orderId) || currSourceType == null) {
            return false;
        }
        synchronized (orderId.intern()) {
            LambdaQueryWrapper<MerchantMessageInfo> queryWrapper = Wrappers.lambdaQuery();
            queryWrapper.eq(MerchantMessageInfo::getOrderId, orderId);
            queryWrapper.eq(MerchantMessageInfo::getStatus, MerchantMessageInfo.STATUS_SUCCESS);
            queryWrapper.ne(MerchantMessageInfo::getSourceType, currSourceType);
            return count(queryWrapper) > 0;
        }
    }
}
