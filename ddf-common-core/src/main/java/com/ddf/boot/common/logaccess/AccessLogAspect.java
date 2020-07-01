package com.ddf.boot.common.logaccess;

import com.ddf.boot.common.util.AopUtil;
import com.ddf.boot.common.util.JsonUtil;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.annotation.Pointcut;
import org.aspectj.lang.reflect.MethodSignature;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.concurrent.ThreadPoolTaskExecutor;

import java.util.Map;

/**
 *
 * 拦截指定的请求为某些注解功能提供支持，目前支持功能如下
 * <ul>
 *     <li>{@link EnableLogAspect}</li>
 * </ul>
 *
 * _ooOoo_
 * o8888888o
 * 88" . "88
 * (| -_- |)
 * O\ = /O
 * ___/`---'\____
 * .   ' \\| |// `.
 * / \\||| : |||// \
 * / _||||| -:- |||||- \
 * | | \\\ - /// | |
 * | \_| ''\---/'' | |
 * \ .-\__ `-` ___/-. /
 * ___`. .' /--.--\ `. . __
 * ."" '< `.___\_<|>_/___.' >'"".
 * | | : `- \`.;`\ _ /`;.`/ - ` : | |
 * \ \ `-. \_ __\ /__ _/ .-` / /
 * ======`-.____`-.___\_____/___.-`____.-'======
 * `=---='
 * .............................................
 * 佛曰：bug泛滥，我已瘫痪！
 *
 * @author dongfang.ding on 2018/10/9
 */
@Aspect
public class AccessLogAspect {
    private final Logger logger = LoggerFactory.getLogger(this.getClass());

    public static final String BEAN_NAME = "accessLogAspect";

    @Autowired
    private LogAspectConfiguration logAspectConfiguration;

    @Autowired(required = false)
    private SlowEventAction slowEventAction;

    @Autowired
    private ThreadPoolTaskExecutor defaultThreadPool;

    @Pointcut(value = "execution(public * com..controller..*(..))")
    public void pointCut() {}


    /**
     * 打印方法调用参数和执行结果，对接口耗时进行统计，本拦截类只关心用户自定义的慢接口捕捉和处理；
     * 不提供操作日志的持久化
     *
     * 因为操作日志如果不做到对请求前和请求后的数据获取的话，仅仅记录调用本身意义也不大
     * @param joinPoint
     * @return
     */
    @Around("pointCut()")
    public Object handler(ProceedingJoinPoint joinPoint) throws Throwable {
        // 获取当前类名
        Class<?> pointClass = AopUtil.getJoinPointClass(joinPoint);
        // 获取当前方法名
        MethodSignature pointMethod = AopUtil.getJoinPointMethod(joinPoint);
        // 获取请求参数
        Map<String, Object> paramMap = AopUtil.getParamMap(joinPoint);
        String paramJson = JsonUtil.asString(paramMap);
        // 调用起始时间
        long beforeTime = System.currentTimeMillis();
        // 执行方法
//        try {
            Object proceed = joinPoint.proceed();
            long consumerTime = System.currentTimeMillis() - beforeTime;
            // 打印返回值和接口耗时
            logger.debug("[{}]-[{}]请求参数: {}, 执行返回结果: {}, 共耗时: [{}ms]", pointClass.getName(), pointMethod.getName(),
                    paramJson, JsonUtil.asString(proceed), consumerTime);
            // 执行慢接口逻辑判断
            dealSlowTimeHandler(pointClass.getSimpleName(), pointMethod.getName(), paramJson, consumerTime);
            return proceed;
/*        } catch (Throwable throwable) {
            logger.debug("[{}]-[{}]请求参数: {}, 执行出现异常！", pointClass.getName(), pointMethod.getName(),
                    JsonUtil.asString(paramMap), throwable);
            // fixme 本意是想拦截到异常之类打印出来，或者做些别的处理，但是catch之后怎么抛出原来的异常呢？
            // 这里不能抛出原来的异常会影响很多异常对应的状态码处理，只能暂时不catch了
            // 不能丢失自定义异常的code码
            throw new GlobalCustomizeException(throwable.getMessage());
        }*/
    }

    /**
     * 如果接口耗时超过预设值，提供一个异步回调接口给使用者实现处理逻辑
     * @param className
     * @param methodName
     * @param consumerTime
     */
    private void dealSlowTimeHandler(String className, String methodName, String params, long consumerTime) {
        long slowTime = logAspectConfiguration.getSlowTime();
        if (consumerTime > slowTime && slowEventAction != null && !checkIgnore(className)) {
            // 需要使用方自己去实现doAction接口接收参数自定义自己的处理机制
            SlowEventAction.SlowEvent slowEvent = new SlowEventAction.SlowEvent(className, methodName, params, consumerTime, slowTime);
            logger.info("{}-{}耗时{}，准备执行处理回调。。。。", className, methodName, consumerTime);
            defaultThreadPool.execute(() -> slowEventAction.doAction(slowEvent));
        }
    }

    /**
     * 判断是否忽略处理当前类
     * 这个功能的意义是有些接口天生就是慢接口的，但是又不想统计这个接口，因为开发时已经知道了，所以要跳过这个接口处理
     * @param className
     * @return
     */
    private boolean checkIgnore(String className) {
        String[] ignore = logAspectConfiguration.getIgnore();
        if (ignore != null && ignore.length > 0) {
            for (String aClass : ignore) {
                if (aClass.equals(className) || className.startsWith(aClass)) {
                    return true;
                }
            }
        }
        return false;
    }

}
