package com.ddf.boot.common.mvc.requestsign;

import com.ddf.boot.common.api.exception.BaseErrorCallbackCode;
import com.ddf.boot.common.api.exception.BusinessException;
import com.ddf.boot.common.api.model.common.request.BaseSign;
import com.ddf.boot.common.core.config.GlobalProperties;
import com.ddf.boot.common.core.util.SignatureUtil;
import java.lang.reflect.Method;
import java.util.Map;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.reflect.MethodSignature;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

/**
 * RequestSignAccessFilterChain 测试
 *
 * @author Codex
 * @since 2026/04/20
 */
class RequestSignAccessFilterChainTest {

    @Test
    @DisplayName("无 RequestSign 注解时应直接放行")
    void shouldPassWhenMethodHasNoRequestSignAnnotation() throws Exception {
        GlobalProperties globalProperties = new GlobalProperties();
        RequestSignAccessFilterChain chain = new RequestSignAccessFilterChain(globalProperties);
        ProceedingJoinPoint joinPoint = mock(ProceedingJoinPoint.class);
        MethodSignature methodSignature = mock(MethodSignature.class);
        Method method = DemoController.class.getDeclaredMethod("plain", SignedPayload.class);

        when(joinPoint.getSignature()).thenReturn(methodSignature);
        when(methodSignature.getMethod()).thenReturn(method);

        assertTrue(chain.filter(joinPoint, DemoController.class, methodSignature));
        assertEquals(-1, chain.getOrder());
    }

    @Test
    @DisplayName("缺少签名时应抛出 SIGN_ERROR")
    void shouldThrowWhenSignIsMissing() throws Exception {
        GlobalProperties globalProperties = new GlobalProperties();
        globalProperties.setSignSecret("abcdefghijklmnopqrstuvwxyz123456");
        RequestSignAccessFilterChain chain = new RequestSignAccessFilterChain(globalProperties);
        ProceedingJoinPoint joinPoint = buildJoinPoint(
                DemoController.class.getDeclaredMethod("signed", SignedPayload.class),
                new SignedPayload(null, null, "demo"));

        BusinessException exception = assertThrows(BusinessException.class,
                () -> chain.filter(joinPoint, DemoController.class, (MethodSignature) joinPoint.getSignature()));

        assertEquals(BaseErrorCallbackCode.SIGN_ERROR.getCode(), exception.getCode());
    }

    @Test
    @DisplayName("开启 nonce 且时间戳过期时应抛出 SIGN_TIMESTAMP_ERROR")
    void shouldThrowWhenNonceTimestampExpired() throws Exception {
        GlobalProperties globalProperties = new GlobalProperties();
        globalProperties.setSignSecret("abcdefghijklmnopqrstuvwxyz123456");
        RequestSignAccessFilterChain chain = new RequestSignAccessFilterChain(globalProperties);
        SignedPayload payload = new SignedPayload("fake-sign", System.currentTimeMillis() - 10_000L, "demo");
        ProceedingJoinPoint joinPoint = buildJoinPoint(
                DemoController.class.getDeclaredMethod("signedWithNonce", SignedPayload.class), payload);

        BusinessException exception = assertThrows(BusinessException.class,
                () -> chain.filter(joinPoint, DemoController.class, (MethodSignature) joinPoint.getSignature()));

        assertEquals(BaseErrorCallbackCode.SIGN_TIMESTAMP_ERROR.getCode(), exception.getCode());
    }

    @Test
    @DisplayName("合法签名应通过校验")
    void shouldPassWhenSignatureIsValid() throws Exception {
        String secret = "abcdefghijklmnopqrstuvwxyz123456";
        GlobalProperties globalProperties = new GlobalProperties();
        globalProperties.setSignSecret(secret);
        RequestSignAccessFilterChain chain = new RequestSignAccessFilterChain(globalProperties);
        SignedPayload payload = new SignedPayload(null, null, "demo");
        payload.setSign(SignatureUtil.genSelfSignature(secret, payload));
        ProceedingJoinPoint joinPoint = buildJoinPoint(
                DemoController.class.getDeclaredMethod("signed", SignedPayload.class), payload);

        assertTrue(chain.filter(joinPoint, DemoController.class, (MethodSignature) joinPoint.getSignature()));
    }

    @Test
    @DisplayName("普通参数签名合法时也应通过")
    void shouldPassWhenPlainArgumentsSignatureIsValid() throws Exception {
        String secret = "abcdefghijklmnopqrstuvwxyz123456";
        GlobalProperties globalProperties = new GlobalProperties();
        globalProperties.setSignSecret(secret);
        RequestSignAccessFilterChain chain = new RequestSignAccessFilterChain(globalProperties);
        Map<String, Object> signSource = new java.util.LinkedHashMap<>();
        signSource.put("name", "codex");
        String sign = SignatureUtil.genSelfSignature(secret, signSource);
        ProceedingJoinPoint joinPoint = buildJoinPoint(
                DemoController.class.getDeclaredMethod("signedPlain", String.class, String.class),
                new String[] {"sign", "name"}, sign, "codex");

        assertTrue(chain.filter(joinPoint, DemoController.class, (MethodSignature) joinPoint.getSignature()));
    }

    private static ProceedingJoinPoint buildJoinPoint(Method method, Object arg) {
        return buildJoinPoint(method, new String[] {"payload"}, arg);
    }

    private static ProceedingJoinPoint buildJoinPoint(Method method, String[] parameterNames, Object... args) {
        ProceedingJoinPoint joinPoint = mock(ProceedingJoinPoint.class);
        MethodSignature methodSignature = mock(MethodSignature.class);
        when(joinPoint.getSignature()).thenReturn(methodSignature);
        when(joinPoint.getArgs()).thenReturn(args);
        when(methodSignature.getMethod()).thenReturn(method);
        when(methodSignature.getParameterNames()).thenReturn(parameterNames);
        return joinPoint;
    }

    static class DemoController {

        public void plain(SignedPayload payload) {
        }

        @RequestSign
        public void signed(SignedPayload payload) {
        }

        @RequestSign(nonce = true, nonceIntervalSeconds = 1)
        public void signedWithNonce(SignedPayload payload) {
        }

        @RequestSign
        public void signedPlain(String sign, String name) {
        }
    }


    static class SignedPayload implements BaseSign {

        private String sign;
        private Long nonceTimestamp;
        private String name;

        SignedPayload(String sign, Long nonceTimestamp, String name) {
            this.sign = sign;
            this.nonceTimestamp = nonceTimestamp;
            this.name = name;
        }

        @Override
        public String getSign() {
            return sign;
        }

        public void setSign(String sign) {
            this.sign = sign;
        }

        @Override
        public Long getNonceTimestamp() {
            return nonceTimestamp;
        }

        public void setNonceTimestamp(Long nonceTimestamp) {
            this.nonceTimestamp = nonceTimestamp;
        }

        public String getName() {
            return name;
        }

        public void setName(String name) {
            this.name = name;
        }
    }
}
