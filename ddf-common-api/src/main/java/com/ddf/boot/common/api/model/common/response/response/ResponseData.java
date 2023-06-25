package com.ddf.boot.common.api.model.common.response.response;

import com.ddf.boot.common.api.exception.BaseCallbackCode;
import com.ddf.boot.common.api.exception.BaseErrorCallbackCode;
import com.ddf.boot.common.api.exception.BusinessException;
import java.util.Objects;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.experimental.Accessors;
import lombok.extern.slf4j.Slf4j;

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
@Slf4j
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

    /**
     * 扩展字段
     * 比如在某些情况下正常逻辑返回的是data
     * 某些异常逻辑下返回的是另外一套数据, 比如现在用户余额不足， 需要返回到底缺多少， 然后还提供一系列数据来说明怎么来购买
     */
    private Object extra;


    public ResponseData(String code, String message, String stack, long timestamp, T data) {
        this.code = code;
        this.message = message;
        this.stack = stack;
        this.timestamp = timestamp;
        this.data = data;
    }

    public ResponseData(String code, String message, String stack, long timestamp, T data, Object extra) {
        this.code = code;
        this.message = message;
        this.stack = stack;
        this.timestamp = timestamp;
        this.data = data;
        this.extra = extra;
    }

    /**
     * 成功返回数据方法
     *
     * @param data
     * @param <T>
     * @return
     */
    public static <T> ResponseData<T> success(T data) {
        return new ResponseData<>(
                BaseErrorCallbackCode.COMPLETE.getCode(),
                BaseErrorCallbackCode.COMPLETE.getDescription(), "", System.currentTimeMillis(), data
        );
    }

    /**
     * 返回空数据
     *
     * @return
     */
    public static ResponseData<Void> empty() {
        return new ResponseData<>(
                BaseErrorCallbackCode.COMPLETE.getCode(),
                BaseErrorCallbackCode.COMPLETE.getDescription(), "", System.currentTimeMillis(), null
        );
    }

    /**
     * 失败返回消息方法
     *
     * @param baseCallbackCode
     * @param <T>
     * @return
     */
    public static <T> ResponseData<T> failure(BaseCallbackCode baseCallbackCode) {
        return new ResponseData<>(baseCallbackCode.getCode(), baseCallbackCode.getBizMessage(), "", System.currentTimeMillis(), null);
    }

    /**
     * 失败返回消息方法
     *
     * @param code
     * @param message
     * @param stack
     * @param <T>
     * @return
     */
    public static <T> ResponseData<T> failure(String code, String message, String stack) {
        return new ResponseData<>(code, message, stack, System.currentTimeMillis(), null);
    }

    /**
     * 失败返回消息方法
     *
     * @param code
     * @param message
     * @param stack
     * @param <T>
     * @return
     */
    public static <T> ResponseData<T> failure(String code, String message, String stack, Object extra) {
        return new ResponseData<>(code, message, stack, System.currentTimeMillis(), null, extra);
    }


    /**
     * 判断返回结果是否是成功
     *
     * @return
     */
    public boolean isSuccess() {
        return Objects.equals(code, BaseErrorCallbackCode.COMPLETE.getCode());
    }


    /**
     * 获取返回数据， 如果响应码非成功，则抛出异常
     *
     * @return
     */
    public T requiredSuccess() {
        if (isSuccess()) {
            return data;
        }
        throw new BusinessException(code, message);
    }


    /**
     * 获取返回数据， 如果响应码非成功，返回指定默认值
     *
     * @param defaultValue
     * @return
     */
    public T failureDefault(T defaultValue) {
        if (!isSuccess()) {
            return defaultValue;
        }
        return data;
    }

}
