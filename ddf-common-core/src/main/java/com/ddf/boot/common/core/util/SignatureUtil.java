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
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.TreeMap;
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
     * ascii 升序排序参数，仅处理基本数据类型、包装类和字符串，复杂的嵌套对象或者集合等会被忽略。
     *
     * @param data
     * @param <T>
     * @return
     */
    public static <T> String asciiSortToQueryStringOnlyBasicType(T data) {
        return generateSignString(data, false);
    }

    /**
     * ascii 升序排序参数, 对于复杂嵌套对象，采用平铺方式处理。如
     * {"a":1,"b":{"c":2,"d":3}}  => a=1&b.c=2&b.d=3
     *
     * @param data
     * @param <T>
     * @return
     */
    public static <T> String asciiSortToQueryStringFlatten(T data) {
        return generateSignString(data, true);
    }

    /**
     * 统一的签名字符串生成逻辑
     *
     * @param data    原始数据
     * @param flatten 是否需要平铺嵌套对象
     * @return 排序后的查询字符串
     */
    private static <T> String generateSignString(T data, boolean flatten) {
        // 先将对象转换为map
        Map<String, Object> params;
        if (data instanceof Map<?, ?> map) {
            params = (Map<String, Object>) map;
        } else {
            params = JsonUtil.toBean(JsonUtil.asString(data), Map.class);
        }

        if (params == null || params.isEmpty()) {
            return "";
        }

        // 使用 TreeMap 自动处理参数名按照ASCII码表升序排序
        Map<String, Object> resultMap = new TreeMap<>();
        collectParams(params, null, resultMap, flatten);

        // 按照排序拼接参数名与参数值
        StringBuilder paramBuffer = new StringBuilder();
        for (Map.Entry<String, Object> entry : resultMap.entrySet()) {
            if (!paramBuffer.isEmpty()) {
                paramBuffer.append("&");
            }
            paramBuffer
                    .append(entry.getKey())
                    .append("=")
                    .append(entry.getValue());
        }
        System.out.println("queryString: " + paramBuffer.toString());
        return paramBuffer.toString();
    }

    /**
     * 核心递归逻辑：负责参数的过滤、平铺与收集
     *
     * @param currentMap 当前层级的 Map
     * @param prefix     前缀路径 (用于平铺)
     * @param result     结果容器
     * @param flatten    是否平铺
     */
    private static void collectParams(Map<String, Object> currentMap, String prefix, Map<String, Object> result,
            boolean flatten) {
        if (currentMap == null) {
            return;
        }

        for (Map.Entry<String, Object> entry : currentMap.entrySet()) {
            String key = entry.getKey();
            Object obj = entry.getValue();

            // 排除参数为空的和以及签名字段
            if (ObjectUtils.isEmpty(obj) || BaseSign.SELF_SIGNATURE_FIELD.equals(key)) {
                continue;
            }

            // 计算全路径 Key
            String fullKey = (prefix == null || prefix.isEmpty()) ? key : prefix + "." + key;

            // 只对基本类型参数加签
            if (isBasicType(obj)) {
                result.put(fullKey, obj);
            } else if (flatten && obj instanceof Map<?, ?> childMap) {
                // 如果是嵌套对象且开启了平铺，递归处理
                collectParams((Map<String, Object>) childMap, fullKey, result, true);
            } else if (flatten && obj instanceof List<?> list) {
                // 处理列表类型：遍历列表并构造带索引的 Key，如 list[0].name
                for (int i = 0; i < list.size(); i++) {
                    Object item = list.get(i);
                    String listKey = fullKey + "[" + i + "]";
                    if (isBasicType(item)) {
                        result.put(listKey, item);
                    } else if (item instanceof Map<?, ?> itemMap) {
                        collectParams((Map<String, Object>) itemMap, listKey, result, true);
                    }
                }
            }
        }
    }

    /**
     * map转查询串
     *
     * @param dataMap
     * @return
     */
    public static String mapToQueryString(Map<String, Object> dataMap) {
        // 1. 参数名按照ASCII码表升序排序
        String[] keys = dataMap
                .keySet()
                .toArray(new String[0]);
        Arrays.sort(keys);

        // 2. 按照排序拼接参数名与参数值
        StringBuilder paramBuffer = new StringBuilder();
        Object obj;
        for (String key : keys) {
            obj = dataMap.get(key);
            // 排除参数为空的和以及签名字段
            if (ObjectUtils.isEmpty(obj)) {
                paramBuffer
                        .append(key)
                        .append("=");
                continue;
            }
            if (!paramBuffer.isEmpty()) {
                paramBuffer.append("&");
            }
            paramBuffer
                    .append(key)
                    .append("=")
                    .append(obj);
        }
        return paramBuffer.toString();
    }


    /**
     * 生成自己系统的签名信息规则
     *
     * @param secretKey 产品私钥
     * @param data      参数对象
     * @return
     */
    public static <T> String genSelfSignature(String secretKey, T data) {
        return genSelfSignature(secretKey, data, false);
    }

    /**
     * 生成自己系统的签名信息规则
     * <p>
     * <p>
     * 1. 参数名按ASCII升序排序；
     * 2. 参数值非空按 k=v&k1=v2 形式组合数据。 如果flatten为true的话，针对复杂嵌套对象，会将参数平铺成一级结构，如{"a":1,"b":{"c":2,"d":3}}  => a=1&b.c=2&b.d=3
     * 3. 使用 HmacSHA256 算法加密，Access Key Secret 作为加密的秘钥
     * 4. 以 16 进制小写形式输出加密后的内容
     * 5. sign参数不建议放在data中， 如果放的话， 这个方法需要识别出这个字段的值是哪个， 否则无法剔除这个字段的影响，目前固定为sign
     *
     * @param secretKey 产品私钥
     * @param data      参数对象
     * @param flatten   是否平铺, 如果为false的话，则跳过复杂嵌套对象的处理，只处理基本类型
     * @return
     */
    public static <T> String genSelfSignature(String secretKey, T data, boolean flatten) {
        final String queryString = generateSignString(data, flatten);
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
     * 验证签名，默认不平铺复杂结构
     *
     * @param data
     * @param sign
     * @param keySecret
     * @param <T>
     * @return
     */
    public static <T> boolean verifySelfSignature(T data, String sign, String keySecret) {
        return verifySelfSignature(data, sign, keySecret, false);
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
     *
     * 针对这种有几种解决方案
     * 1. 强制对json的key进行ascii排序
     * 2. 跳过处理复杂结构
     * 3. 将复杂的嵌套结构全部平铺到第一层级
     *
     * 这个方法就是给出了后面两种解决方案， 第一种方案，太过随机，不建议使用
     *
     * @param keySecret 秘钥
     * @param sign
     * @param data      参数对象
     * @param flatten   是否平铺, 如果为false的话，则跳过复杂嵌套对象的处理，只处理基本类型
     * @return
     */
    public static <T> boolean verifySelfSignature(T data, String sign, String keySecret, boolean flatten) {
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
        return Objects.equals(genSelfSignature(keySecret, map, flatten), sign);
    }

    /**
     * 是否基本类型或基本对象
     *
     * @param obj
     * @return
     */
    private static boolean isBasicType(Object obj) {
        return obj instanceof String || obj instanceof Number || obj instanceof Boolean || obj instanceof Character;
    }


    /**
     * 对键值对参数进行升序url编码后进行sha1
     *
     * @param params
     * @return
     */
    public static String sha1(Map<String, Object> params) {
        final String s = asciiSortToQueryStringOnlyBasicType(params);
        return SecureUtil.sha1(s);
    }
    /**
     * @param args 参数
     */
    public static void main(String[] args) {
        // f8d2ef16c48c87a1a00b9920c57f168213cae6da12686736737a528ab6b7d3fc
        String str =
                "{\"nonZeroNumber\":119,\"zeroNumber\":0,\"nullObj\":null,\"string\":\"有效字符\",\"emptyString\":\"\",\"simpleList\":[\"index0\",\"index1\"],\"complexList\":[{\"key1\":\"0.key1_value\",\"key2\":\"0.key2_value\"},{\"key1\":\"1.key1_value\",\"key2\":\"1.key2_value\"}],\"map\":{\"key1\":\"value1\",\"key2\":\"value2\"},\"booleanValue\":false}";
        final Map bean = JsonUtil.toBean(str, Map.class);
        boolean flatten = true;
        String secret = "abcdefghijklmnopqrstuvw987654321";
        bean.put("nonce", "1692697533554");
        final String sign = genSelfSignature(secret, bean, flatten);
        System.out.println("sign = " + sign);
        System.out.println();
        final boolean verified = verifySelfSignature(bean, sign, secret, flatten);
        System.out.println("verified = " + verified);

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