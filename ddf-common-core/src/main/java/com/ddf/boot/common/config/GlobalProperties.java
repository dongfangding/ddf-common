package com.ddf.boot.common.config;

import com.ddf.boot.common.exception.ErrorAttributesHandler;
import lombok.Getter;
import lombok.Setter;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

import java.util.List;

/**
 *
 * 存放一些全局的自定义属性，根据需要决定是否可配置
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
 * @author dongfang.ding on 2019/1/25
 */
@Component
@ConfigurationProperties(prefix = "customs.global-properties")
@Getter
@Setter
public class GlobalProperties {

    /**
     * 雪花算法的workerId
     * worker Id can't be greater than 31 or less than 0
     */
    private long snowflakeWorkerId = 1;


    /**
     * 雪花算法的数据中心id  5位
     * dataCenterId can't be greater than 31 or less than 0
     */
    private long snowflakeDataCenterId = 1;


    /**
     * 默认异常处理类会返回一个trace字段，将当前错误堆栈信息返回，方便调试时查看错误，提供该参数指定的
     * profile不会返回该字段，如生产环境
     *
     * @see ErrorAttributesHandler#getErrorAttributes(org.springframework.web.context.request.WebRequest, boolean)
     */
    private List<String> ignoreErrorTraceProfile;
}
