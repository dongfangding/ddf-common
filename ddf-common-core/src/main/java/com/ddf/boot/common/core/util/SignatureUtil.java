package com.ddf.boot.common.core.util;

import cn.hutool.crypto.SecureUtil;
import cn.hutool.crypto.digest.HMac;
import cn.hutool.crypto.digest.HmacAlgorithm;
import com.ddf.boot.common.api.exception.BaseErrorCallbackCode;
import com.ddf.boot.common.api.exception.BusinessException;
import com.ddf.boot.common.api.model.common.request.BaseSign;
import com.ddf.boot.common.api.util.JsonUtil;
import java.nio.charset.StandardCharsets;
import java.util.Arrays;
import java.util.Map;
import java.util.Objects;
import java.util.concurrent.TimeUnit;
import org.apache.commons.lang3.StringUtils;
import org.springframework.util.ObjectUtils;

/**
 * 生成及验证签名信息工具类
 *
 * @author snowball
 */
public class SignatureUtil {

    /**
     * ascii 升序排序参数
     *
     * @param data
     * @param <T>
     * @return
     */
    public static <T> String asciiSortToQueryString(T data) {
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
            obj = params.get(key);
            // 排除参数为空的和以及签名字段
            if (ObjectUtils.isEmpty(obj) || BaseSign.SELF_SIGNATURE_FIELD.equals(key)) {
                continue;
            }
            // 只对基本类型参数加签， 如果是嵌套对象，目前不考虑
            if (isBasic(obj)) {
                if (paramBuffer.length() > 0) {
                    paramBuffer.append("&");
                }
                paramBuffer.append(key).append("=").append(obj);
            }
            i++;
        }
        return paramBuffer.toString();
    }


    /**
     * map转查询串
     *
     * @param dataMap
     * @return
     */
    public static String mapToQueryString(Map<String, Object> dataMap) {
        // 1. 参数名按照ASCII码表升序排序
        String[] keys = dataMap.keySet().toArray(new String[0]);
        Arrays.sort(keys);

        // 2. 按照排序拼接参数名与参数值
        StringBuilder paramBuffer = new StringBuilder();
        Object obj;
        for (String key : keys) {
            obj = dataMap.get(key);
            // 排除参数为空的和以及签名字段
            if (ObjectUtils.isEmpty(obj)) {
                paramBuffer.append(key).append("=");
                continue;
            }
            if (paramBuffer.length() > 0) {
                paramBuffer.append("&");
            }
            paramBuffer.append(key).append("=").append(obj);
        }
        return paramBuffer.toString();
    }


    /**
     * 生成自己系统的签名信息规则
     * <p>
     * <p>
     * 1. 参数名按ASCII升序排序；
     * 2. 参数值非空按 k=v&k1=v2 形式组合数据（参数值为数组时，请将值转换成 json；
     * 参数值为对象时，请保证按照ASCII升序排序去设置json字段的值，至少在生成签名时要保证这个规则
     * 3. 使用 HmacSHA256 算法加密，Access Key Secret 作为加密的秘钥；这里用了jackson的反序列属性保证反序列化时字段有序
     * 4. 以 16 进制小写形式输出加密后的内容
     * 5. sign参数不建议放在data中， 如果放的话， 这个方法需要识别出这个字段的值是哪个， 否则无法剔除这个字段的影响，目前固定为sign
     *
     * @param secretKey 产品私钥
     * @param data      参数对象
     * @return
     */
    public static <T> String genSelfSignature(String secretKey, T data) {
        final String queryString = asciiSortToQueryString(data);
        HMac mac = new HMac(HmacAlgorithm.HmacSHA256, secretKey.getBytes(StandardCharsets.UTF_8));
        return mac.digestHex(queryString, StandardCharsets.UTF_8);
    }

    /**
     * 验证签名
     *
     * @param data
     * @param keySecret           秘钥
     * @param nonceTimeoutSeconds 重放校验时间， 单位秒
     * @return
     */
    public static <T extends BaseSign> boolean verifySelfSignature(T data, String keySecret, long nonceTimeoutSeconds) {
        // 时间戳参数超过一定间隔，视作重放
        if (Objects.isNull(data.getNonceTimestamp())
                || data.getNonceTimestamp() < System.currentTimeMillis() - TimeUnit.SECONDS.toMillis(
                nonceTimeoutSeconds)) {
            throw new BusinessException(BaseErrorCallbackCode.SIGN_TIMESTAMP_ERROR);
        }
        return verifySelfSignature(data, keySecret, data.getSign());
    }

    /**
     * 验证签名
     * <p>
     * 主要问题是json的问题，json的问题不是要对json里面的字段进行map排序。而是一整个json其实是一个value，而不是参数的键值对
     * 即使拿最简单的查询字符串来说，其实类似于与这样param1={"id":1,"name":"haha"}&param2={"id":1,"name":"haha"}
     * 那么其实要保证的是参数的value里面的json要有序， 否则客户端和服务端可能因为id和name的前后顺序不一致而导致加签结果不同。
     * <p>
     * 这里和param1=1&param2=chen&param3=上海    这种情况并不一致，这种查询串直接map保持一定规则就行，而不是上面那种复杂形势。
     * <p>
     * 所以如果是post + json面临的问题就是最上面说的那种复杂情况，要保证json的字段有一定顺序（当然放入可以没有，但是加签一定要保证顺序）
     * 因为json传参的时候有一个形参来接收整个json字符串。那么加签的时候其实就是对这个形参=json字符串进行加签，而不是对json字符串里面的字符再排序再加钱。
     * 这里的json已经是一个参数的具体value了，是一个字符串
     *
     * @param keySecret 秘钥
     * @param sign
     * @param data
     * @return
     */
    public static <T> boolean verifySelfSignature(T data, String sign, String keySecret) {
        if (StringUtils.isEmpty(sign)) {
            return false;
        }
        Map<String, Object> map;
        if (data instanceof Map map1) {
            map = map1;
        } else {
            String str = JsonUtil.asString(data);
            map = JsonUtil.toBean(str, Map.class);
        }
        return Objects.equals(genSelfSignature(keySecret, map), sign);
    }

    /**
     * 是否基本类型或基本对象
     *
     * @param obj
     * @return
     */
    private static boolean isBasic(Object obj) {
        return obj instanceof Integer || obj instanceof String || obj instanceof Double || obj instanceof Float
                || obj instanceof Byte || obj instanceof Short || obj instanceof Long || obj instanceof Boolean;
    }


    /**
     * 对键值对参数进行升序url编码后进行sha1
     *
     * @param params
     * @return
     */
    public static String sha1(Map<String, Object> params) {
        final String s = asciiSortToQueryString(params);
        return SecureUtil.sha1(s);
    }

    public static void main(String[] args) {
        // f8d2ef16c48c87a1a00b9920c57f168213cae6da12686736737a528ab6b7d3fc
        String str =
                "{\"giftId\":119,\"versionCode\":\"1\",\"combo\":0,\"count\":1,\"timeStamp\":0,\"chatroomId\":\"8922\",\"os\":\"0\",\"dstUidS\":[\"273\"],\"tag\":\"\",\"fullMic\":false}";
        final Map bean = JsonUtil.toBean(str, Map.class);
        bean.put("nonce", "1692697533554");
        final String s = genSelfSignature("abcdefghijklmnopqrstuvw987654321", bean);
        System.out.println("s = " + s);



        //        Map<String, Object> map = new HashMap<>();
        //        map.put("name", "张三");
        //        map.put("age", 18);
        //        map.put("height", 1.8);
        //        map.put("weight", 70);
        //        map.put("isMarried", true);
        //        map.put("nonceTimestamp", System.currentTimeMillis());
        //        final String sign = genSelfSignature("1234567890", map);
        //        map.put("sign", sign);
        //        System.out.println("sign = " + sign);
        //        System.out.println(verifySelfSignature(map, sign, "1234567890"));
    }
}
