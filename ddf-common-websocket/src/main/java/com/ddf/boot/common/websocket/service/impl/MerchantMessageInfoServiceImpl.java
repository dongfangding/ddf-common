package com.ddf.boot.common.websocket.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.conditions.update.LambdaUpdateWrapper;
import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.ddf.boot.common.core.exception.GlobalCustomizeException;
import com.ddf.boot.common.core.util.JsonUtil;
import com.ddf.boot.common.websocket.enumerate.CmdEnum;
import com.ddf.boot.common.websocket.mapper.MerchantMessageInfoMapper;
import com.ddf.boot.common.websocket.model.entity.MerchantBaseDevice;
import com.ddf.boot.common.websocket.model.entity.MerchantMessageInfo;
import com.ddf.boot.common.websocket.model.ws.AuthPrincipal;
import com.ddf.boot.common.websocket.model.ws.Message;
import com.ddf.boot.common.websocket.service.MerchantMessageInfoService;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.exception.ExceptionUtils;
import org.apache.commons.lang3.time.DateUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import javax.validation.constraints.NotNull;
import java.util.*;

/**
 * 云闪付收款到账消息 服务实现类
 *
 * @author dongfang.ding
 * @date 2019/12/21
 */
@Service
@Slf4j
public class MerchantMessageInfoServiceImpl extends ServiceImpl<MerchantMessageInfoMapper, MerchantMessageInfo> implements MerchantMessageInfoService {
    @Autowired
    private MerchantMessageInfoMapper merchantMessageInfoMapper;

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
        try {
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
            updateWrapper.set(MerchantMessageInfo::getSequence, merchantBaseDevice.getSequence());
            updateWrapper.set(MerchantMessageInfo::getMerchantId, merchantBaseDevice.getMerchantId());
            updateWrapper.set(MerchantMessageInfo::getDescription, messageInfo.getDescription());
            updateWrapper.set(MerchantMessageInfo::getOrderId, messageInfo.getOrderId());
            updateWrapper.set(MerchantMessageInfo::getParseContent, messageInfo.getParseContent());
            updateWrapper.set(MerchantMessageInfo::getBillTime, messageInfo.getBillTime());
            updateWrapper.set(MerchantMessageInfo::getMatchById, messageInfo.getMatchById() );
            updateWrapper.set(MerchantMessageInfo::getMatchByName, messageInfo.getMatchByName());
            updateWrapper.set(MerchantMessageInfo::getClientChannel, messageInfo.getClientChannel());

            if (messageInfo.getOrderType() == null) {
                messageInfo.setOrderType(MerchantMessageInfo.STATUS_NOT_DEAL);
            }
            updateWrapper.set(MerchantMessageInfo::getOrderType, messageInfo.getOrderType());

            if (messageInfo.getSourceType() == null) {
                updateWrapper.set(MerchantMessageInfo::getSourceType, MerchantMessageInfo.SOURCE_TYPE_UNKNOWN);
            } else {
                updateWrapper.set(MerchantMessageInfo::getSourceType, messageInfo.getSourceType());
            }
            if (StringUtils.isNotBlank(messageInfo.getTradeNo()) && messageInfo.getReceiveTime() != null) {
                updateWrapper.set(MerchantMessageInfo::getTradeNo, messageInfo.getTradeNo());
                updateWrapper.set(MerchantMessageInfo::getReceiveTime, messageInfo.getReceiveTime());
            }

            if (!update(updateWrapper)) {
                log.error("消息更新失败！{}", messageInfo);
            }
            return true;
        } catch (Exception e) {
            // 没有控制器调用的方法，异常必须手动error才能看到异常消息
            log.error("更新消息表状态失败！", e);
            throw new GlobalCustomizeException(ExceptionUtils.getStackTrace(e));
        }
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
        merchantMessageInfo.setDeviceNumber(authPrincipal.getAccessKeyId());
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
    public boolean fastFailure(List<MerchantMessageInfo> messageInfos, Integer status, String errorMessage) {
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
