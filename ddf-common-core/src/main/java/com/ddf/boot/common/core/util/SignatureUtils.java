package com.ddf.boot.common.core.util;

import cn.hutool.crypto.digest.DigestUtil;
import cn.hutool.crypto.digest.HMac;
import cn.hutool.crypto.digest.HmacAlgorithm;
import com.ddf.boot.common.core.exception200.BusinessException;
import com.ddf.boot.common.core.exception200.GlobalCallbackCode;
import java.nio.charset.StandardCharsets;
import java.util.Arrays;
import java.util.Map;
import java.util.Objects;
import org.springframework.util.StringUtils;

/**
 * 生成及验证签名信息工具类
 *
 * @author snowball
 */
public class SignatureUtils {


    /**
     * 自己系统的加签字段
     */
    private static final String SELF_SIGNATURE_FIELD = "sign";

    /**
     * 生成签名
     *
     * @param secretKey
     * @param params
     * @return
     */
    public static String genSignature(String secretKey, Map<String, String> params) {
        params.remove(SELF_SIGNATURE_FIELD);
        // 1. 参数名按照ASCII码表升序排序
        String[] keys = params.keySet().toArray(new String[0]);
        Arrays.sort(keys);

        // 2. 按照排序拼接参数名与参数值
        StringBuilder paramBuffer = new StringBuilder();
        for (String key : keys) {
            paramBuffer.append(key).append(params.get(key) == null ? "" : String.valueOf(params.get(key)));
        }
        // 3. 将secretKey拼接到最后
        paramBuffer.append(secretKey);

        // 4. MD5是128位长度的摘要算法，用16进制表示，一个十六进制的字符能表示4个位，所以签名后的字符串长度固定为32个十六进制字符。
        return DigestUtil.md5Hex(paramBuffer.toString());
    }


    /**
     * 生成自己系统的签名信息规则
     * <p>
     * <p>
     * 1. 参数名按ASCII升序排序；
     * 2. 参数值非空按 k=v&k1=v2 形式组合数据（参数值为数组时，请将值转换成 json；
     *    参数值为对象时，请保证按照ASCII升序排序去设置json字段的值，至少在生成签名时要保证这个规则
     * 3. 使用 HmacSHA256 算法加密，Access Key Secret 作为加密的秘钥；这里用了jackson的反序列属性保证反序列化时字段有序
     * 4. 以 16 进制小写形式输出加密后的内容
     * 5. sign参数不建议放在data中， 如果放的话， 这个方法需要识别出这个字段的值是哪个， 否则无法剔除这个字段的影响，目前固定为sign
     *
     * @param secretKey 产品私钥
     * @param data      参数对象
     * @return
     */
    public static <T> String genSelfSignature(String secretKey, T data) {
        // 先将对象转换为map
        Map<String, Object> params = JsonUtil.toBean(JsonUtil.asString(data), Map.class);
        // 1. 参数名按照ASCII码表升序排序
        String[] keys = params.keySet().toArray(new String[0]);
        Arrays.sort(keys);

        // 2. 按照排序拼接参数名与参数值
        StringBuilder paramBuffer = new StringBuilder();
        int i = 0;
        Object obj;
        for (String key : keys) {
            // 排除参数为空的
            if (StringUtils.isEmpty(params.get(key)) || SELF_SIGNATURE_FIELD.equals(key)) {
                continue;
            }
            if (i != 0) {
                paramBuffer.append("&");
            }
            obj = params.get(key);
            // 排除为null的，如果是字符串空串，这里不会排除
            if (Objects.isNull(obj)) {
                continue;
            }
            if (isBasic(obj)) {
                paramBuffer.append(key).append("=").append(obj);
            } else {
                // 这里如果是对象序列化的话，已经设置了jackson序列化要按照ASCII升序排序，所以传参时顺序不重要，但是在加签时这个顺序必须正确。
                paramBuffer.append(key).append("=").append(JsonUtil.asString(obj));
            }
            i++;
        }
        HMac mac = new HMac(HmacAlgorithm.HmacSHA256, secretKey.getBytes(StandardCharsets.UTF_8));
        System.out.println(paramBuffer.toString());
        return mac.digestHex(paramBuffer.toString(), "UTF-8");
    }

    /**
     * 验证签名
     *
     * @param sign      签名参数
     * @param keySecret 秘钥
     * @return
     */
    public static <T> boolean verifySelfSignature(String sign, String keySecret, T data) {
        if (StringUtils.isEmpty(sign)) {
            throw new BusinessException(GlobalCallbackCode.SIGN_ERROR);
        }
        String str = JsonUtil.asString(data);
        Map<String, Object> map = JsonUtil.toBean(str, Map.class);
        String s = genSelfSignature(keySecret, map);
        return s.equals(sign);
    }

    /**
     * 是否基本类型或基本对象
     *
     * @param obj
     * @return
     */
    private static boolean isBasic(Object obj) {
        return obj instanceof Integer || obj instanceof String || obj instanceof Double || obj instanceof Float ||
                obj instanceof Byte || obj instanceof Short || obj instanceof Long || obj instanceof Boolean;
    }
}
