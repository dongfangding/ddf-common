package com.ddf.boot.common.core.requestsign;

import cn.hutool.core.util.StrUtil;
import com.ddf.boot.common.api.exception.BaseErrorCallbackCode;
import com.ddf.boot.common.api.exception.BusinessException;
import com.ddf.boot.common.api.model.common.request.BaseSign;
import com.ddf.boot.common.core.config.GlobalProperties;
import com.ddf.boot.common.core.logaccess.AccessFilterChain;
import com.ddf.boot.common.core.util.AopUtil;
import com.ddf.boot.common.core.util.SignatureUtils;
import java.util.Map;
import java.util.Objects;
import java.util.concurrent.TimeUnit;
import lombok.RequiredArgsConstructor;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.reflect.MethodSignature;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

/**
 * <p>description</p >
 *
 * @author Snowball
 * @version 1.0
 * @date 2022/01/13 21:27
 */
@Component
@RequiredArgsConstructor(onConstructor_={@Autowired})
public class RequestSignAccessFilterChain implements AccessFilterChain {

    private final GlobalProperties globalProperties;

    /**
     * 实现的执行顺序
     * <p>
     * order越小，优先级越高
     *
     * @return
     */
    @Override
    public Integer getOrder() {
        return -1;
    }

    /**
     * 将aop参数暴露， 允许多实现实现自己的拦截业务处理， 如数据校验、签名校验，用户校验交给原生的filter去做了
     *
     * 1. sign相关参数并没有放到请求头中，这是因为很容易被猜到，一看到字段就知道含义。现在是放到请求参数中，如果是查询串就固定参数sign,
     * 如果是json, 就固定字段sign
     *
     * @param joinPoint
     * @param pointClass
     * @param pointMethod
     * @return
     */
    @Override
    public boolean filter(ProceedingJoinPoint joinPoint, Class<?> pointClass, MethodSignature pointMethod) {
        final RequestSign requestSign = pointMethod.getMethod().getAnnotation(RequestSign.class);
        if (Objects.isNull(requestSign)) {
            return Boolean.TRUE;
        }
        final Map<String, Object> paramMap = AopUtil.getSerializableParamMap(joinPoint);
        // 如果没有参数直接放行
        if (paramMap.isEmpty()) {
            return Boolean.TRUE;
        }
        // 签名值
        String sign;
        // 重放时间戳
        Long timestamp = null;
        // 最终用来验签的参数对象， 强制定义规则， 如果参数对象没有实现过BaseSign， 则不会当做对象来处理。
        // 这个时候使用map参数对象，否则使用BaseSign实现类
        Object data;
        final BaseSign baseSign = (BaseSign) paramMap.values()
                .stream()
                .filter((obj -> obj instanceof BaseSign))
                .findFirst()
                .orElse(null);
        // 接收参数如果实现了BaseSign接口，则按基础对象处理。否则按查询串处理，这里已经包含了application/json和application/x-www-form-urlencoded的处理，
        // 即入参对象必须实现接口BaseSign
        if (Objects.nonNull(baseSign)) {
            data = baseSign;
            sign = baseSign.getSign();
            timestamp = baseSign.getNonceTimestamp();
        } else {
            if (!paramMap.containsKey(BaseSign.SELF_SIGNATURE_FIELD)) {
                throw new BusinessException(BaseErrorCallbackCode.SIGN_ERROR);
            }
            sign = (String) paramMap.get(BaseSign.SELF_SIGNATURE_FIELD);
            data = paramMap;
        }
        if (StrUtil.isBlank(sign)) {
            throw new BusinessException(BaseErrorCallbackCode.SIGN_ERROR);
        }
        String keySecret = globalProperties.getSignSecret();
        boolean result;
        if (requestSign.nonce()) {
            if (Objects.isNull(timestamp) && !paramMap.containsKey(BaseSign.SELF_TIMESTAMP_FIELD)) {
                throw new BusinessException(BaseErrorCallbackCode.SIGN_TIMESTAMP_ERROR);
            }
            if (Objects.isNull(timestamp)) {
                timestamp = Long.parseLong((String) paramMap.get(BaseSign.SELF_TIMESTAMP_FIELD));
            }
            // 时间戳参数超过一定间隔，视作重放
            if (timestamp < System.currentTimeMillis() - TimeUnit.SECONDS.toMillis(requestSign.nonceIntervalSeconds())) {
                throw new BusinessException(BaseErrorCallbackCode.SIGN_TIMESTAMP_ERROR);
            }
        }
        result = SignatureUtils.verifySelfSignature(data, sign, keySecret);
        if (!result) {
            throw new BusinessException(BaseErrorCallbackCode.SIGN_ERROR);
        }
        return result;
    }
}
