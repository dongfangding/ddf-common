package com.ddf.boot.common.websocket.enumerate;

/**
 * 针对匿名发送时定义的一些发送时机，用来代表指令是什么业务触发的,
 *
 *
 * 但是用的时候记得一定要name,而不是value, 因为真正的发送人的id有可能和value重合，会导致区分不出来！！！！！
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
 * @date 2019/12/23 0023 16:20
 */
public enum AnonymousOperatorId {

    /**
     * 设备重连
     */
    DEVICE_RECONNECTION,


    /**
     * 支付方式认证流程认证
     */
    PAYMENT_METHOD_VERIFY,

    /**
     * 定时
     */
    TASK,

    /**
     * 订单匹配完成
     */
    AFTER_ORDER,

    /**
     * 下单之前
     */
    PRE_ORDER


    ;


    /**
     * 只所以不把value定义成枚举的值， 是怕传参的时候把value直接传进去了，如果传进去了，这个value和实际上的用户id可能会发生
     * 重合，那样就区分不出来，是真正的用户还是这里的类型了
     * @return
     */
    public static Integer value(String name) {
        if (AnonymousOperatorId.DEVICE_RECONNECTION.name().equals(name)) {
            return 0;
        } else if (AnonymousOperatorId.TASK.name().equals(name)) {
            return 1;
        } else if (AnonymousOperatorId.AFTER_ORDER.name().equals(name)) {
            return 2;
        } else if (AnonymousOperatorId.PRE_ORDER.name().equals(name)) {
            return 3;
        }
        return -1;
    }
}
