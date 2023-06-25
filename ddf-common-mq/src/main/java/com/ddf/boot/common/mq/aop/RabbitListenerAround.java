package com.ddf.boot.common.mq.aop;

import com.ddf.boot.common.mq.helper.MqMessageHelper;
import com.ddf.boot.common.mq.listener.MqEventListener;
import com.ddf.boot.common.mvc.util.AopUtil;
import java.util.List;
import java.util.Map;
import lombok.extern.slf4j.Slf4j;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.annotation.Pointcut;
import org.springframework.amqp.core.Message;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

/**
 * 该类用来拦截消费者对队列的消费情况，本意是想获取当前消费的队列，消费成功还是失败；
 * 由于本身消费端在消费的时候会使用{@link org.springframework.amqp.rabbit.annotation.RabbitListener}注解来指定消费的队列和
 * 消费模式等信息，因此不再额外定义注解，再让消费端再重新指定这几个属性；
 *
 *
 *
 * <p>
 * <p>
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
 * @author dongfang.ding
 * @date 2019/12/20 0020 9:57
 */
@Aspect
@Component
@Slf4j
public class RabbitListenerAround {

    @Autowired
    private List<MqEventListener> mqEventListenerList;

    @Autowired
    private MqMessageHelper mqMessageHelper;

    @Pointcut(value = "@annotation(org.springframework.amqp.rabbit.annotation.RabbitListener))")
    public void pointcut() {
    }

    /**
     * 用来监听消费者对Mq的消费情况
     *
     * @param joinPoint
     * @throws Throwable
     */
    @Around(value = "pointcut()")
    public void around(ProceedingJoinPoint joinPoint) throws Throwable {
        String className = joinPoint.getSignature().getDeclaringTypeName();
        String methodName = joinPoint.getSignature().getName();
        RabbitListener rabbitListener = AopUtil.getAnnotation(joinPoint, RabbitListener.class);
        Map<Class<?>, Object> args = AopUtil.getArgs(joinPoint);
        Message message = null;
        try {
            message = (Message) args.get(Message.class);
        } catch (Exception e) {
            log.error("没有获取到Message对象,无法进行消费情况捕捉！{}", args);
        }
        Message finalMessage = message;
        try {
            joinPoint.proceed();
        } catch (Throwable throwable) {
            log.error("[{}#{}]消费失败！", className, methodName);
            if (message != null) {
                if (mqEventListenerList != null && !mqEventListenerList.isEmpty()) {
                    mqEventListenerList.forEach((listener) -> listener.consumerFailure(rabbitListener,
                            mqMessageHelper.parseNoBody(finalMessage), throwable
                    ));
                }
            }
            throw throwable;
        }
        if (message != null) {
            if (mqEventListenerList != null && !mqEventListenerList.isEmpty()) {
                mqEventListenerList.forEach((listener) -> listener.consumerSuccess(rabbitListener,
                        mqMessageHelper.parseNoBody(finalMessage)
                ));
            }
        }
    }
}
