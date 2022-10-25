package com.ddf.boot.common.core.util;

import cn.hutool.crypto.digest.HMac;
import cn.hutool.crypto.digest.HmacAlgorithm;
import com.ddf.boot.common.api.model.request.BaseSign;
import com.ddf.boot.common.core.exception200.BusinessException;
import com.ddf.boot.common.core.exception200.GlobalCallbackCode;
import java.nio.charset.StandardCharsets;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;
import java.util.concurrent.TimeUnit;
import org.springframework.util.StringUtils;

/**
 * 生成及验证签名信息工具类
 *
 * @author snowball
 */
public class SignatureUtils {

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
            // 排除参数为空的和以及签名字段
            if (StringUtils.isEmpty(params.get(key)) || BaseSign.SELF_SIGNATURE_FIELD.equals(key)) {
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
            // 只对基本类型参数加签， 如果是嵌套对象，目前不考虑
            if (isBasic(obj)) {
                paramBuffer.append(key).append("=").append(obj);
            }
            //            else {
//                final MapConverter converter = new MapConverter(HashMap.class);
//                final Map<?, ?> convert = converter.convert(obj, new HashMap<>());
//                convert.remove(BaseSign.SELF_SIGNATURE_FIELD);
//                convert.remove(BaseSign.SELF_TIMESTAMP_FIELD);
//                // 这里如果是对象序列化的话，已经设置了jackson序列化要按照ASCII升序排序，所以传参时顺序不重要，但是在加签时这个顺序必须正确。
//                paramBuffer.append(key).append("=").append(JsonUtil.asString(convert));
//            }
            i++;
        }
        HMac mac = new HMac(HmacAlgorithm.HmacSHA256, secretKey.getBytes(StandardCharsets.UTF_8));
        return mac.digestHex(paramBuffer.toString(), StandardCharsets.UTF_8);
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
                || data.getNonceTimestamp() < System.currentTimeMillis() - TimeUnit.SECONDS.toMillis(nonceTimeoutSeconds)) {
            throw new BusinessException(GlobalCallbackCode.SIGN_TIMESTAMP_ERROR);
        }
        return verifySelfSignature(data, keySecret, data.getSign());
    }

    /**
     * 验证签名
     *
     * 主要问题是json的问题，json的问题不是要对json里面的字段进行map排序。而是一整个json其实是一个value，而不是参数的键值对
     * 即使拿最简单的查询字符串来说，其实类似于与这样param1={"id":1,"name":"haha"}&param2={"id":1,"name":"haha"}
     * 那么其实要保证的是参数的value里面的json要有序， 否则客户端和服务端可能因为id和name的前后顺序不一致而导致加签结果不同。
     *
     * 这里和param1=1&param2=chen&param3=上海    这种情况并不一致，这种查询串直接map保持一定规则就行，而不是上面那种复杂形势。
     *
     * 所以如果是post + json面临的问题就是最上面说的那种复杂情况，要保证json的字段有一定顺序（当然放入可以没有，但是加签一定要保证顺序）
     * 因为json传参的时候有一个形参来接收整个json字符串。那么加签的时候其实就是对这个形参=json字符串进行加签，而不是对json字符串里面的字符再排序再加钱。
     * 这里的json已经是一个参数的具体value了，是一个字符串
     *
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
        String str = JsonUtil.asString(data);
        Map<String, Object> map = JsonUtil.toBean(str, Map.class);
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

    public static void main(String[] args) {
        Map<String, Object> map = new HashMap<>();
        map.put("name", "张三");
        map.put("age", 18);
        map.put("height", 1.8);
        map.put("weight", 70);
        map.put("isMarried", true);
        map.put("nonceTimestamp", System.currentTimeMillis());
        final String sign = genSelfSignature("1234567890", map);
        map.put("sign", sign);
        System.out.println("sign = " + sign);
        System.out.println(verifySelfSignature(map, sign, "1234567890"));
    }
}
