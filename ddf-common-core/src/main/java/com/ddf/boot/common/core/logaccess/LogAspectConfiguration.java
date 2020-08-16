package com.ddf.boot.common.core.logaccess;

import lombok.Getter;
import lombok.Setter;

/**
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
 * @see LogAspectRegistrar
 * @author dongfang.ding on 2018/11/7
 */
@Getter
@Setter
public class LogAspectConfiguration {
    public static final String BEAN_NAME = "logAspectConfiguration";

    /**
     * 是否使用了注解开启了功能{@link EnableLogAspect}
     */
    private boolean enableLogAspect;

    /**
     * @see EnableLogAspect#slowTime()
     * @see LogAspectRegistrar
     */
    private long slowTime;


    /**
     * @see EnableLogAspect#ignore()
     * @see LogAspectRegistrar
     */
    private String[] ignore;
}
