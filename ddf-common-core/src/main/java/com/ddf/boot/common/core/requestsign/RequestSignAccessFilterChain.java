package com.ddf.boot.common.core.requestsign;

import cn.hutool.core.util.StrUtil;
import com.ddf.boot.common.core.exception200.BusinessException;
import com.ddf.boot.common.core.exception200.GlobalCallbackCode;
import com.ddf.boot.common.core.logaccess.AccessFilterChain;
import com.ddf.boot.common.core.model.request.BaseSign;
import com.ddf.boot.common.core.util.AopUtil;
import com.ddf.boot.common.core.util.SignatureUtils;
import java.util.Map;
import java.util.Objects;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.reflect.MethodSignature;
import org.springframework.stereotype.Component;

/**
 * <p>description</p >
 *
 * @author Snowball
 * @version 1.0
 * @date 2022/01/13 21:27
 */
@Component
public class RequestSignAccessFilterChain implements AccessFilterChain {

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
        final Map<String, Object> paramMap = AopUtil.getAllParamMap(joinPoint);
        final String sign = (String) paramMap.get(BaseSign.SELF_SIGNATURE_FIELD);
        if (StrUtil.isBlank(sign)) {
            throw new BusinessException(GlobalCallbackCode.SIGN_ERROR);
        }
        String keySecret = "";
        if (requestSign.nonce()) {
            Long timestamp = Long.parseLong((String) paramMap.get(BaseSign.SELF_TIMESTAMP_FIELD));
            return SignatureUtils.verifySelfSignature(paramMap, keySecret, sign, timestamp, requestSign.nonceIntervalSeconds());
        }
        return SignatureUtils.verifySelfSignature(paramMap, keySecret, sign);
    }
}
