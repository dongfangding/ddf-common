package com.ddf.boot.common.ext.sms.helper;

import cn.hutool.core.util.RandomUtil;
import cn.hutool.json.JSONUtil;
import com.aliyuncs.CommonRequest;
import com.aliyuncs.CommonResponse;
import com.aliyuncs.DefaultAcsClient;
import com.aliyuncs.IAcsClient;
import com.aliyuncs.http.MethodType;
import com.aliyuncs.http.ProtocolType;
import com.aliyuncs.profile.DefaultProfile;
import com.ddf.boot.common.ext.sms.config.SmsProperties;
import com.ddf.boot.common.ext.sms.domain.AliYunSmsActionEnum;
import com.ddf.boot.common.ext.sms.domain.AliYunSmsRequest;
import lombok.Data;
import lombok.SneakyThrows;
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
public class AliYunSmsHelper {

    @Autowired
    private SmsProperties smsProperties;

    /**
     * 随机生成基于验证码变量code的短信模板参数
     *
     * @return
     */
    public String randomCodeTemplateParam() {
        int code = RandomUtil.randomInt(100000, 999999);
        return "{\"code\": " + code + "}";
    }


    /**
     * 发送短信验证码
     *
     * @param aliYunSmsRequest
     */
    @SneakyThrows
    public CommonResponse sendSms(AliYunSmsRequest aliYunSmsRequest) {
        DefaultProfile profile = DefaultProfile.getProfile("cn-hangzhou", smsProperties.getAccessKeyId(),
                smsProperties.getAccessKeySecret()
        );
        IAcsClient client = new DefaultAcsClient(profile);
        CommonRequest request = new CommonRequest();
        request.setSysMethod(MethodType.POST);
        request.setSysProtocol(ProtocolType.valueOf(smsProperties.getProtocol()));
        request.setSysDomain(smsProperties.getEndpoint());
        request.setSysVersion("2017-05-25");
        request.setSysAction(AliYunSmsActionEnum.SendSms.name());
        request.putQueryParameter("RegionId", "cn-hangzhou");
        request.putQueryParameter("PhoneNumbers", aliYunSmsRequest.getPhoneNumbers());
        if (aliYunSmsRequest.isUseRandomCode()) {
            request.putQueryParameter("TemplateParam", randomCodeTemplateParam());
        } else {
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
        CommonResponse response = client.getCommonResponse(request);
        // fixme 可能有问题，没找到代表错误的字段是哪个，没充值也不确定成功时这里会不会有消息
        if (response.getData() != null) {
            final String message = JSONUtil.parse(response.getData()).getByPath("Message", String.class);
            if (!"OK".equals(message)) {
                throw new BusinessException(message);
            }
        }
        return response;
    }
}
