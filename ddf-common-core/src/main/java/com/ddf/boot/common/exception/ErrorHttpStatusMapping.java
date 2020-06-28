package com.ddf.boot.common.exception;

/**
 * 提供一个接口，用户自定义的异常实现该接口返回自己异常类对应的http状态码，这样无需修改源码即可$
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
 * @date 2019/12/24 0024 13:27
 */
public interface ErrorHttpStatusMapping {


     /**
     *
     * 实现该接口，返回自定义异常对应的http状态码，{@link ErrorAttributesHandler}会去执行这个接口判断，然后获取
     * 实现方自己的状态码
      *
      * 处理异常与http状态码的对应映射关系
     * @param error
     * @see ErrorAttributesHandler
     * @return java.lang.Integer
     * @author dongfang.ding
     * @date 2019/12/24 0024 13:51
     **/
    Integer getHttpStatus(Throwable error);
}
