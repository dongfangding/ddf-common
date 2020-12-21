package com.ddf.boot.common.core.response;

import com.ddf.boot.common.core.exception200.BaseErrorCallbackCode;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.experimental.Accessors;

/**
 * 统一响应内容类
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
 * @date 2019/6/27 11:17
 */
@Data
@NoArgsConstructor
@Accessors(chain = true)
public class ResponseData<T> {

    /**
     * 返回消息代码
     */
    private String code;
    /**
     * 返回消息
     */
    private String message;
    /**
     * 错误堆栈信息
     */
    private String stack;
    /**
     * 响应时间
     */
    private long timestamp;
    /**
     * 返回数据
     */
    private T data;


    public ResponseData(String code, String message, String stack, long timestamp, T data) {
        this.code = code;
        this.message = message;
        this.stack = stack;
        this.timestamp = timestamp;
        this.data = data;
    }

    public static <T> ResponseData<T> success(T data) {
        return new ResponseData<>(BaseErrorCallbackCode.COMPLETE.getCode(),
                BaseErrorCallbackCode.COMPLETE.getDescription(), "", System.currentTimeMillis(), data
        );
    }

    public static <T> ResponseData<T> failure(String code, String message, String stack) {
        return new ResponseData<>(code, message, stack, System.currentTimeMillis(), null);
    }

}
