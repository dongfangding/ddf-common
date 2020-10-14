package com.ddf.boot.common.ext.sms.helper;

import com.ddf.boot.common.ext.sms.config.SmsProperties;
import com.ddf.boot.common.ext.sms.domain.AliYunSmsActionEnum;
import com.ddf.boot.common.ext.sms.domain.AliYunSmsRequest;
import lombok.Data;
import lombok.SneakyThrows;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.nio.charset.StandardCharsets;
import java.text.SimpleDateFormat;
import java.util.HashMap;
import java.util.Map;

/**
 * <p>description</p >
 *
 * @author Snowball
 * @version 1.0
 * @date 2020/10/14 11:01
 */
@Data
@Component
public class AliYunSmsHelper {

    @Autowired
    private SmsProperties smsProperties;

    /**
     * 发送短信验证码
     * @param aliYunSmsRequest
     */
    public void sendSms(AliYunSmsRequest aliYunSmsRequest) {
        SimpleDateFormat simpleDateFormat = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss'Z'");
        // 这里一定要设置GMT时区
        simpleDateFormat.setTimeZone(new java.util.SimpleTimeZone(0, "GMT"));
        Map<String, String> paras = new HashMap<>();

        // 指定请求参数
        paras.put("SignName", smsProperties.getSinaName());
        if (StringUtils.isNotBlank(aliYunSmsRequest.getSinaName())) {
            paras.put("SignName", aliYunSmsRequest.getSinaName());
        }
        paras.put("TemplateCode", smsProperties.getTemplateCode());
        if (StringUtils.isNotBlank(aliYunSmsRequest.getTemplateCode())) {
            paras.put("TemplateCode", aliYunSmsRequest.getTemplateCode());
        }
        paras.put("PhoneNumbers", aliYunSmsRequest.getPhoneNumbers());
        paras.put("TemplateParam", aliYunSmsRequest.getTemplateParam());
        paras.put("AccessKeyId", smsProperties.getAccessKeyId());
        paras.put("Action", AliYunSmsActionEnum.SendSms.name());
        paras.put("RegionId", "cn-hangzhou");
        paras.put("SignatureMethod", "HMAC-SHA1");
        paras.put("SignatureNonce", java.util.UUID.randomUUID().toString());
        paras.put("SignatureVersion", "1.0");
        paras.put("Timestamp", simpleDateFormat.format(new java.util.Date()));
        paras.put("Version", "2017-05-25");

        // 根据参数Key排序（顺序）
        java.util.TreeMap<String, String> sortParas = new java.util.TreeMap<>(paras);
        java.util.Iterator<String> it = sortParas.keySet().iterator();

        // 构造待签名的请求串
        StringBuilder sortQueryStringTmp = new StringBuilder();
        while (it.hasNext()) {
            String key = it.next();
            sortQueryStringTmp.append("&").append(specialUrlEncode(key)).append("=").append(specialUrlEncode(paras.get(key)));
        }
        String sortedQueryString = sortQueryStringTmp.substring(1);

        // 签名
        StringBuilder stringToSign = new StringBuilder();
        stringToSign.append("GET").append("&");
        stringToSign.append(specialUrlEncode("/")).append("&");
        stringToSign.append(specialUrlEncode(sortedQueryString));
        String sign = sign(smsProperties.getAccessKeySecret() + "&", stringToSign.toString());
        String signature = specialUrlEncode(sign);

        // 增加签名结果到请求参数中，发送请求
        final String finalQueryString = smsProperties.getProtocol() + smsProperties.getEndpoint() + "?Signature=" + signature + sortQueryStringTmp;
        System.out.println("finalQueryString = " + finalQueryString);
    }

    /**
     * 签名
     * @param accessSecret
     * @param stringToSign
     * @return
     * @throws Exception
     */
    @SneakyThrows
    public static String sign(String accessSecret, String stringToSign) {
        javax.crypto.Mac mac = javax.crypto.Mac.getInstance("HmacSHA1");
        mac.init(new javax.crypto.spec.SecretKeySpec(accessSecret.getBytes(StandardCharsets.UTF_8), "HmacSHA1"));
        byte[] signData = mac.doFinal(stringToSign.getBytes(StandardCharsets.UTF_8));
        return new sun.misc.BASE64Encoder().encode(signData);
    }

    /**
     * 下面会用到的特殊URL编码这个是POP特殊的一种规则，即在一般的URLEncode后再增加三种字符替换：加号 （+）替换成 %20、星号 （*）替换成 %2A、 %7E 替换回波浪号 （~）参考代码如下：
     * @param value
     * @return
     */
    @SneakyThrows
    public static String specialUrlEncode(String value) {
        return java.net.URLEncoder.encode(value, "UTF-8").replace("+", "%20")
                .replace("*", "%2A").replace("%7E", "~");
    }
}
