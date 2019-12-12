package com.ddf.boot.common.log;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.function.Consumer;

/**
 *
 * 接口如果太慢的话，提供一个回调接口，由使用方自己去实现自己的处理机制
 * @see AccessLogAspect#dealSlowTimeHandler(String, String, long)
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
 * @author dongfang.ding on 2019/3/13
 */
public interface SlowEventAction {

    /**
     * 将超时的几个属性放入对象，提供给使用者去实现自己的处理逻辑,该方法在回调的时候已经设置为异步，
     * 自己实现的时候没必要在异步了
     * @param slowEvent
     */
    void doAction(SlowEvent slowEvent);

    /**
     * 提供一个静态方法来完成回调功能
     *
     * @param slowEven
     * @param consumer
     */
    static void doAction(SlowEvent slowEven, Consumer<SlowEvent> consumer) {
        consumer.accept(slowEven);
    }


    /**
     * 超时后相关属性类
     */
    @Getter
    @NoArgsConstructor
    @AllArgsConstructor
    @Setter
    class SlowEvent {
        /** 超时的完整类名 */
        private String className;

        /** 超时的方法名 */
        private String methodName;

        /** 实际耗时，单位毫秒 */
        private Long consumerTime;

        /** 预设的超过该值的毫秒值 */
        private Long slowTime;
    }
}
