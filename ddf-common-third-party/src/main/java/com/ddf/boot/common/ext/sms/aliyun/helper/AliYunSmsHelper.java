package com.ddf.boot.common.ext.sms.aliyun.helper;

import cn.hutool.core.util.RandomUtil;
import cn.hutool.json.JSONUtil;
import com.aliyuncs.CommonRequest;
import com.aliyuncs.CommonResponse;
import com.aliyuncs.DefaultAcsClient;
import com.aliyuncs.IAcsClient;
import com.aliyuncs.exceptions.ClientException;
import com.aliyuncs.http.MethodType;
import com.aliyuncs.http.ProtocolType;
import com.aliyuncs.profile.DefaultProfile;
import com.ddf.boot.common.api.exception.BusinessException;
import com.ddf.boot.common.api.util.JsonUtil;
import com.ddf.boot.common.ext.constants.ExceptionCode;
import com.ddf.boot.common.ext.sms.aliyun.config.AliYunSmsProperties;
import com.ddf.boot.common.ext.sms.aliyun.domain.AliYunSmsActionEnum;
import com.ddf.boot.common.ext.sms.aliyun.domain.TemplateParamObj;
import com.ddf.boot.common.ext.sms.model.SmsSendRequest;
import com.ddf.boot.common.ext.sms.model.SmsSendResponse;
import lombok.Data;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

/**
 * <p>description</p >
 *
 * @author dongfang.ding
 * @version 1.0
 * @date 2020/10/14 11:01
 */
@Data
@Component
@Slf4j
@RequiredArgsConstructor(onConstructor_={@Autowired})
public class AliYunSmsHelper {

    private final AliYunSmsProperties smsProperties;

    /**
     * 随机生成基于验证码变量code的短信模板参数
     *
     * @return
     */
    public TemplateParamObj randomCodeTemplateParam() {
        int code = RandomUtil.randomInt(100000, 999999);
        return TemplateParamObj.builder()
                .templateParam("{\"code\": " + code + "}")
                .code(code + "")
                .build();
    }


    /**
     * 发送短信验证码
     *
     * @param aliYunSmsRequest
     */
    public SmsSendResponse sendSms(SmsSendRequest aliYunSmsRequest) {
        DefaultProfile profile = DefaultProfile.getProfile("cn-hangzhou", smsProperties.getAccessKeyId(),
                smsProperties.getAccessKeySecret()
        );
        String templateParam = aliYunSmsRequest.getTemplateParam();
        IAcsClient client = new DefaultAcsClient(profile);
        CommonRequest request = new CommonRequest();
        request.setSysMethod(MethodType.POST);
        request.setSysProtocol(ProtocolType.valueOf(smsProperties.getProtocol()));
        request.setSysDomain(smsProperties.getEndpoint());
        request.setSysVersion("2017-05-25");
        request.setSysAction(AliYunSmsActionEnum.SendSms.name());
        request.putQueryParameter("RegionId", "cn-hangzhou");
        request.putQueryParameter("PhoneNumbers", aliYunSmsRequest.getPhoneNumbers());
        TemplateParamObj templateParamObj;
        if (StringUtils.isBlank(templateParam)) {
            templateParamObj = randomCodeTemplateParam();
            templateParam = templateParamObj.getTemplateParam();
            request.putQueryParameter("TemplateParam", templateParam);
        } else {
            templateParamObj = JsonUtil.toBean(templateParam, TemplateParamObj.class);
            request.putQueryParameter("TemplateParam", aliYunSmsRequest.getTemplateParam());
        }
        if (StringUtils.isNotBlank(aliYunSmsRequest.getSinaName())) {
            request.putQueryParameter("SignName", aliYunSmsRequest.getSinaName());
        } else {
            request.putQueryParameter("SignName", smsProperties.getSinaName());
        }
        if (StringUtils.isNotBlank(aliYunSmsRequest.getTemplateCode())) {
            request.putQueryParameter("TemplateCode", aliYunSmsRequest.getTemplateCode());
        } else {
            request.putQueryParameter("TemplateCode", smsProperties.getTemplateCode());
        }
        CommonResponse response;
        try {
            response = client.getCommonResponse(request);
        } catch (ClientException e) {
            log.error("发送短信失败， mobile = {}, e = {}", aliYunSmsRequest.getPhoneNumbers(), e);
            throw new BusinessException(ExceptionCode.SMS_SEND_FAILURE);
        }
        // fixme 可能有问题，没找到代表错误的字段是哪个，没充值也不确定成功时这里会不会有消息
        if (response.getData() != null) {
            final String message = JSONUtil.parse(response.getData()).getByPath("Message", String.class);
            if (!"OK".equals(message)) {
                throw new BusinessException(message);
            }
        }
        return SmsSendResponse.builder()
                .templateParam(templateParam)
                .randomCode(templateParamObj.getCode())
                .build();
    }
}
