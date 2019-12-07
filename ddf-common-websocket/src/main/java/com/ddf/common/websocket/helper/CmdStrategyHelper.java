package com.ddf.common.websocket.helper;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.ddf.common.exception.GlobalCustomizeException;
import com.ddf.common.util.JsonUtil;
import com.ddf.common.util.StringUtil;
import com.ddf.common.websocket.biz.HandlerTemplateType;
import com.ddf.common.websocket.enumerate.CmdEnum;
import com.ddf.common.websocket.exception.*;
import com.ddf.common.websocket.interceptor.SmsParseProcessor;
import com.ddf.common.websocket.model.entity.ChannelTransfer;
import com.ddf.common.websocket.model.entity.MerchantBaseDevice;
import com.ddf.common.websocket.model.entity.MerchantMessageInfo;
import com.ddf.common.websocket.model.entity.PlatformMessageTemplate;
import com.ddf.common.websocket.model.ws.*;
import com.ddf.common.websocket.service.ChannelTransferService;
import com.ddf.common.websocket.service.MerchantBaseDeviceService;
import com.ddf.common.websocket.service.MerchantMessageInfoService;
import com.ddf.common.websocket.service.PlatformMessageTemplateService;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.scheduling.concurrent.ThreadPoolTaskExecutor;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.text.SimpleDateFormat;
import java.util.*;
import java.util.stream.Collectors;

/**
 * 将一些与指令业务相关的逻辑统一定义在该类中，方便复用
 *
 * @author dongfang.ding
 * @date 2019/9/4 15:41
 */
@Component
@Slf4j
public class CmdStrategyHelper {

    @Autowired
    private PlatformMessageTemplateService platformMessageTemplateService;
    @Autowired
    private MerchantMessageInfoService merchantMessageInfoService;
    @Autowired
    private MerchantBaseDeviceService merchantBaseDeviceService;
    @Autowired(required = false)
    private SmsParseProcessor smsParseProcessor;
    @Autowired
    @Qualifier("handlerMatchOrder")
    private HandlerTemplateType handlerMatchOrder;
    @Autowired
    private ChannelTransferService channelTransferService;
    @Autowired
    @Qualifier("handlerMatchTemplateExecutor")
    private ThreadPoolTaskExecutor handlerMatchTemplateExecutor;
    @Autowired
    @Qualifier("handlerMessageBusiness")
    private ThreadPoolTaskExecutor handlerMessageBusiness;

    /**
     * 针对批次的报文，对单个报文响应
     *
     * @param key
     * @param keyValue
     * @param errorMessage
     * @return
     * @author dongfang.ding
     */
    public static Map<String, Object> buildErrorMap(String key, String keyValue, String errorMessage, boolean retry) {
        Map<String, Object> errorMap = new HashMap<>(3);
        errorMap.put(key, keyValue);
        errorMap.put("code", MessageResponse.SERVER_CODE_ERROR);
        errorMap.put("message", errorMessage);
        errorMap.put("retry", retry);
        return errorMap;
    }

    public static Map<String, Object> buildSmsErrorMap(String keyValue, String errorMessage) {
        return buildErrorMap("primaryKey", keyValue, errorMessage, false);
    }

    public static Map<String, Object> buildUPayBillOrderErrorMap(String keyValue, String errorMessage) {
        return buildErrorMap("tradeNo", keyValue, errorMessage, false);
    }

    /**
     * 针对批次的报文，对单个报文响应
     *
     * @param key
     * @param keyValue
     * @return
     * @author dongfang.ding
     */
    public static Map<String, Object> buildSuccessMap(String key, String keyValue) {
        Map<String, Object> errorMap = new HashMap<>(4);
        errorMap.put(key, keyValue);
        errorMap.put("code", MessageResponse.SERVER_CODE_COMPLETE);
        errorMap.put("message", "处理成功");
        errorMap.put("retry", false);
        return errorMap;
    }

    public static Map<String, Object> buildSmsSuccessMap(String keyValue) {
        return buildSuccessMap("primaryKey", keyValue);
    }

    public static Map<String, Object> buildUPayBillOrderSuccessMap(String keyValue, String billType, Integer qrCodeType) {
        Map<String, Object> map = buildSuccessMap("tradeNo", keyValue);
        map.put("billType", billType);
        map.put("qrCodeType", qrCodeType);
        return map;
    }

    /**
     * 获取报文主体数据 对象
     *
     * @param message
     * @param errorMessage
     * @return
     * @author dongfang.ding
     */
    @SuppressWarnings("unchecked")
    public static Map<String, Object> getMapPayload(Message message, String errorMessage, boolean isThrowable) {
        if (message == null || message.getBody() == null || "".equals(message.getBody())) {
            throw new MessageFormatInvalid(errorMessage);
        }
        Map<String, Object> payload;
        try {
            payload = (Map) message.getBody();
        } catch (Exception e) {
            log.error(errorMessage + "报文格式: [{}]", message.getBody(), e);
            throw new MessageFormatInvalid(errorMessage);
        }
        if (payload == null || payload.isEmpty()) {
            if (isThrowable) {
                throw new MessageFormatInvalid(errorMessage);
            }
        }
        return payload;
    }

    /**
     * 获取报文主体数据 对象
     *
     * @param message
     * @param errorMessage
     * @return
     * @author dongfang.ding
     */
    @SuppressWarnings("unchecked")
    public static Map<String, Object> getMapPayload(Message message, String errorMessage) {
        return getMapPayload(message, errorMessage, true);
    }

    /**
     * 获取报文主体数据 对象数组
     *
     * @param message
     * @param errorMessage
     * @param isThrowable 报文数据为空时是否抛出异常
     * @return
     * @author dongfang.ding
     */
    @SuppressWarnings("unchecked")
    public static List<Map<String, Object>> getListPayload(Message message, String errorMessage, boolean isThrowable) {
        if (message == null || message.getBody() == null || "".equals(message.getBody())) {
            throw new MessageFormatInvalid(errorMessage);
        }
        List<Map<String, Object>> payload;
        try {
            payload = (List) message.getBody();
        } catch (Exception e) {
            log.error(errorMessage + "报文格式: [{}]", message.getBody(), e);
            throw new MessageFormatInvalid(errorMessage);
        }
        if (payload == null || payload.isEmpty()) {
            if (isThrowable) {
                throw new MessageFormatInvalid(errorMessage);
            }
        }
        return payload;
    }

    public static List<Map<String, Object>> getListPayload(Message message, String errorMessage) {
        return getListPayload(message, errorMessage, true);
    }


    /**
     * 解析模板数据
     *
     * @param templates
     * @param content
     * @author dongfang.ding
     * @return
     */
    public static ParseContent parseMessageTemplate(List<PlatformMessageTemplate> templates, String content) {
        ParseContent parseContent = null;
        boolean isSuccess = false;
        for (PlatformMessageTemplate template : templates) {
            if (StringUtils.isBlank(template.getTemplateContext())) {
                continue;
            }
            boolean isGarbage = SmsParseUtil.isGarbage(Collections.singletonList(template), content);
            if (isGarbage) {
                parseContent = new ParseContent();
                parseContent.setGarbage(true);
                parseContent.setPlatformMessageTemplate(template);
                isSuccess = true;
                break;
            }
            try {
                parseContent = SmsParseUtil.parseToObj(content, template.getTemplateContext());
            } catch (Exception e) {
                continue;
            }
            if (parseContent == null || !content.equals(parseContent.getParseContent())) {
                continue;
            }
            isSuccess = true;
            parseContent.setPlatformMessageTemplate(template);
            break;
        }
        return isSuccess ? parseContent : null;
    }

    /**
     * 异步处理接收到的短信数据
     *
     * @param authPrincipal
     * @param message
     * @param infoList
     * @return
     * @author dongfang.ding
     * @date 2019/9/20 10:56
     */
    @Transactional(rollbackFor = Exception.class)
    public void doBankSmsBusiness(AuthPrincipal authPrincipal, Message message, List<MerchantMessageInfo> infoList) {
        handlerMessageBusiness.execute(() -> {
            MerchantBaseDevice baseDevice = checkDevice(authPrincipal, infoList);

            List<PlatformMessageTemplate> templates = platformMessageTemplateService.getAllTemplateOrderBySort();

            for (MerchantMessageInfo merchantMessageInfo : infoList) {
                parseSmsDetail(authPrincipal, baseDevice, message, templates, merchantMessageInfo);
            }
        });
    }
    
    /**
     * 详细匹配模板步骤
     *
     * @param authPrincipal
     * @param baseDevice
     * @param message
     * @param templates
     * @param merchantMessageInfo
     * @return
     * @author dongfang.ding
     * @date 2019/9/29 18:06
     */
    private void parseSmsDetail(AuthPrincipal authPrincipal, MerchantBaseDevice baseDevice, Message message
            , List<PlatformMessageTemplate> templates, MerchantMessageInfo merchantMessageInfo) {
        handlerMatchTemplateExecutor.execute(() -> {

            if (smsParseProcessor != null) {
                smsParseProcessor.before(authPrincipal, message, merchantMessageInfo);
            }
            merchantMessageInfo.setMerchantId(baseDevice.getMerchantId());
            merchantMessageInfo.setDeviceId(baseDevice.getId());
            SmsContent smsContent;
            String errorMessage;
            String singleMessagePayload = null;
            try {
                singleMessagePayload = merchantMessageInfo.getSingleMessagePayload();
                if (StringUtils.isBlank(singleMessagePayload)) {
                    return;
                }
                smsContent = JsonUtil.toBean(singleMessagePayload, SmsContent.class);
                if (smsContent == null) {
                    throw new MessageFormatInvalid("短信内容转换后为空！");
                }
                if (StringUtils.isAnyBlank(smsContent.getCredit(), smsContent.getPrimaryKey(),
                        smsContent.getContent(), smsContent.getReceiveTime() + "")) {
                    throw new MessageFormatInvalid("短信发件人，短信id, 短信内容, 收件时间不能为空!");
                }
            } catch (Exception e) {
                log.error("短信内容解析失败！！！内容: {}", singleMessagePayload, e);
                errorMessage = StringUtil.exceptionToString(e);
                merchantMessageInfo.setStatus(MerchantMessageInfo.STATUS_DATA_INVALID);
                merchantMessageInfo.setErrorMessage(errorMessage);
                merchantMessageInfo.setErrorStack(StringUtil.exceptionToString(e));
                merchantMessageInfoService.fillStatus(merchantMessageInfo, baseDevice);
                return;
            }
            if (templates == null || templates.isEmpty()) {
                log.error("非垃圾短信未配置短信解析模板! [{}]", merchantMessageInfo);
                merchantMessageInfo.setStatus(MerchantMessageInfo.STATUS_NOT_MATCH_TEMPLATE);
                merchantMessageInfo.setErrorMessage("非垃圾短信未配置短信解析模板！");
                merchantMessageInfoService.fillStatus(merchantMessageInfo, baseDevice);
                return;
            }
            ParseContent parseContent = CmdStrategyHelper.parseMessageTemplate(templates, smsContent.getContent());
            if (parseContent == null) {
                log.error("短信匹配有误！请检查短信模板! [{}]", merchantMessageInfo);
                merchantMessageInfo.setStatus(MerchantMessageInfo.STATUS_NOT_MATCH_TEMPLATE);
                merchantMessageInfo.setErrorMessage("短信匹配有误！请检查短信模板");
                merchantMessageInfoService.fillStatus(merchantMessageInfo, baseDevice);
                return;
            }
            merchantMessageInfo.setDescription(parseContent.getParseContent());
            PlatformMessageTemplate matchTemplate = parseContent.getPlatformMessageTemplate();
            if (matchTemplate == null) {
                log.error("解析后未将模板数据返回！业务无法进行！！{}", merchantMessageInfo);
                merchantMessageInfo.setStatus(MerchantMessageInfo.STATUS_LOGIC_ERROR);
                merchantMessageInfo.setErrorMessage("解析后未将模板数据返回！业务无法进行");
                merchantMessageInfoService.fillStatus(merchantMessageInfo, baseDevice);
                return;
            }
            // 验证来源如果为空，则不验证
            if (StringUtils.isNotBlank(matchTemplate.getCredit()) && !parseContent.isGarbage()
                    && !Objects.equals(matchTemplate.getCredit(), smsContent.getCredit())) {
                log.error("短信发送方未通过认证.短信中发件人: {}, 模板中认证标识: {}, ！！！{}",
                        smsContent.getCredit(), matchTemplate.getCredit(), matchTemplate);
                merchantMessageInfo.setStatus(MerchantMessageInfo.STATUS_ERROR_CREDIT);
                merchantMessageInfo.setErrorMessage(String.format("模板认证标识与短信中不一致!短信【%s】, 模板【%s】",
                        smsContent.getCredit(), matchTemplate.getCredit()));
                merchantMessageInfoService.fillStatus(merchantMessageInfo, baseDevice);
                return;
            }
            try {
                parseContent.byBankSms(smsContent, message.getCmd());
                merchantMessageInfo.setReceiveTime(parseContent.getOrderTime());
                merchantMessageInfo.setTradeNo(parseContent.getTradeNo());

            } catch (InvalidFutureTimeException e) {
                log.error("未来的时间异常！", e);
                merchantMessageInfo.setStatus(MerchantMessageInfo.STATUS_LOGIC_ERROR);
                merchantMessageInfo.setErrorMessage(e.getMessage());
                merchantMessageInfoService.fillStatus(merchantMessageInfo, baseDevice);
                return;
            } catch (Exception e) {
                log.error("构建短信解析对象失败！", e);
                merchantMessageInfo.setStatus(MerchantMessageInfo.STATUS_LOGIC_ERROR);
                merchantMessageInfo.setErrorMessage(e.getMessage());
                merchantMessageInfoService.fillStatus(merchantMessageInfo, baseDevice);
                throw new GlobalCustomizeException(e);
            }
            if (smsParseProcessor != null) {
                smsParseProcessor.after(authPrincipal, parseContent, message, baseDevice, merchantMessageInfo);
            }
        });
    }



    /**
     * 校验设备信息
     * @param authPrincipal
     * @param infoList
     * @return
     * @author dongfang.ding
     */
    private MerchantBaseDevice checkDevice(AuthPrincipal authPrincipal, List<MerchantMessageInfo> infoList) {
        if (infoList == null || infoList.isEmpty()) {
            return null;
        }
        MerchantBaseDevice baseDevice = merchantBaseDeviceService.getByAuthPrincipal(authPrincipal);
        if (baseDevice == null) {
            log.error("未找到设备信息！！[" + authPrincipal + "]");
            merchantMessageInfoService
                    .fastFailure(infoList, MerchantMessageInfo.STATUS_LOGIC_ERROR, "未找到设备信息！！[" + authPrincipal + "]");
            return null;
        }
        return baseDevice;
    }

    /**
     * 校验云闪付账单的时间格式
     * @param orderTime
     * @return
     * @author dongfang.ding
     */
    public static Date checkTimeFormat(String orderTime) {
        if (StringUtils.isBlank(orderTime)) {
            throw new MessageFormatInvalid("时间不能为空!");
        }
        List<SimpleDateFormat> simpleDateFormats = new ArrayList<>(4);
        simpleDateFormats.add(new SimpleDateFormat("yyyy-MM-dd HH:mm:ss"));
        simpleDateFormats.add(new SimpleDateFormat("yyyy-MM-dd HH:mm"));
        simpleDateFormats.add(new SimpleDateFormat("yyyyMMdd HH:mm:ss"));
        simpleDateFormats.add(new SimpleDateFormat("yyyyMMdd HH:mm"));
        simpleDateFormats.add(new SimpleDateFormat("yyyyMMddHHmmss"));
        for (SimpleDateFormat simpleDateFormat : simpleDateFormats) {
            try {
                return simpleDateFormat.parse(orderTime);
            } catch (Exception e) {
                //
            }
        }
        throw new MessageFormatInvalid("消息时间格式解析有误！");
    }


    /**
     * 重试未完成的消息列表
     *
     * @param
     * @return
     * @author dongfang.ding
     * @date 2019/9/29 10:23
     */
    public void retryMerchantMessageInfo() {
        List<MerchantMessageInfo> retryInfos = merchantMessageInfoService.getRetryInfos();

        if (retryInfos.isEmpty()) {
            log.warn("未查询到需要重试的消息数据");
            return;
        }

        log.info("查询到需要重试的数据, {}", JsonUtil.asString(retryInfos));

        Set<String> requestIdSet = new HashSet<>();
        Set<String> deviceNumberSet = new HashSet<>();
        for (MerchantMessageInfo merchantMessageInfo : retryInfos) {
            requestIdSet.add(merchantMessageInfo.getRequestId());
            deviceNumberSet.add(merchantMessageInfo.getDeviceNumber());
        }

        LambdaQueryWrapper<ChannelTransfer> channelTransferQuery = Wrappers.lambdaQuery();
        channelTransferQuery.in(ChannelTransfer::getRequestId, requestIdSet);
        List<ChannelTransfer> channelTransfers = channelTransferService.list(channelTransferQuery);
        if (channelTransfers == null || channelTransfers.isEmpty()) {
            return;
        }

        LambdaQueryWrapper<MerchantBaseDevice> baseDeviceQuery = Wrappers.lambdaQuery();
        baseDeviceQuery.in(MerchantBaseDevice::getNumber, deviceNumberSet);
        List<MerchantBaseDevice> baseDeviceList = merchantBaseDeviceService.list(baseDeviceQuery);
        if (baseDeviceList == null || baseDeviceList.isEmpty()) {
            return;
        }

        Map<String, List<ChannelTransfer>> requestMap = channelTransfers.stream().collect(Collectors
                .groupingBy(ChannelTransfer::getRequestId));

        Map<String, List<MerchantBaseDevice>> baseDeviceMap = baseDeviceList.stream().collect(Collectors
                .groupingBy(MerchantBaseDevice::getNumber));

        if (requestMap.isEmpty()) {
            log.warn("重试消息表的数据未能查询到原始日志请求");
            return;
        }

        if (baseDeviceMap.isEmpty()) {
            log.warn("重试消息表的数据未能查询到设备信息");
            return;
        }

        List<PlatformMessageTemplate> templates = platformMessageTemplateService.getAllTemplateOrderBySort();

        MerchantBaseDevice baseDevice;
        ChannelTransfer channelTransfer;
        AuthPrincipal authPrincipal;
        for (MerchantMessageInfo info : retryInfos) {
            if (baseDeviceMap.get(info.getDeviceNumber()) == null || baseDeviceMap.get(info.getDeviceNumber()).isEmpty()) {
                continue;
            }
            baseDevice = baseDeviceMap.get(info.getDeviceNumber()).get(0);
            if (requestMap.get(info.getRequestId()) == null || requestMap.get(info.getRequestId()).isEmpty()) {
                continue;
            }
            channelTransfer = requestMap.get(info.getRequestId()).get(0);
            authPrincipal = new AuthPrincipal(baseDevice.getNumber(), baseDevice.getRandomCode(),
                    AuthPrincipal.LoginType.ANDROID);

            if (CmdEnum.BANK_SMS.name().equals(info.getCmd())) {
                parseSmsDetail(authPrincipal, baseDevice, JsonUtil
                        .toBean(channelTransfer.getRequest(), Message.class), templates, info);
            }
        }
    }
}



