package com.ddf.boot.common.core.logaccess;

import java.lang.annotation.Documented;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;
import org.springframework.context.annotation.Import;

/**
 * <p>是否开启对当前应用控制层的方法的日志记录功能，目前没有细分哪些方法需要记录，哪些不需要记录，一旦开启，被拦截的
 * 控制层的代码都会被记录,如果以后需要，会看情况添加</p>
 * <p>需要在配置类上加上注解{@code @EnableLogAspect}使用如下:</>
 * <pre class="code">
 *     &#064;Configuration
 *     &#064;EnableLogAspect
 *     public class Config {
 *
 *     }
 * </pre>
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
 * @author dongfang.ding on 2018/11/7
 * @see AccessLogAspect
 */
@Target({ElementType.TYPE})
@Retention(RetentionPolicy.RUNTIME)
@Documented
@Import(value = {LogAspectRegistrar.class})
public @interface EnableLogAspect {

    /**
     * 默认属性，一旦使用该注解，则默认开启功能
     *
     * @return
     */
    boolean enableLogAspect() default true;

    /**
     * 被拦截的方法从入参到结束执行多久算是执行慢的方法，单位为毫秒;
     * 可以设置为自己需要的时间，一旦一个方法最终结束的时间超过阈值，则会被捕捉到并提供一个回调接口给使用者处理逻辑
     *
     * @return
     */
    long slowTime() default 1000;

    /**
     * 忽略的完整类名#方法名，支持包名
     * 如： com.test.controller.TestController#test // 忽略TestController的test方法不计入计算slowTime
     * 如： com.test.controller // 忽略整个com.test.controller包下的所有方法
     *
     * @return
     */
    String[] ignore() default {};
}
