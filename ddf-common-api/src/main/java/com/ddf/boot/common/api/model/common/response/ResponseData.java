package com.ddf.boot.common.api.model.common.response;

import com.ddf.boot.common.api.exception.BaseCallbackCode;
import com.ddf.boot.common.api.exception.BaseErrorCallbackCode;
import com.ddf.boot.common.api.exception.BusinessException;
import com.ddf.boot.common.api.jackson.JsonIgnoreProfile;
import java.util.Objects;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.experimental.Accessors;
import lombok.extern.slf4j.Slf4j;

/**
 * 统一响应内容类
 *
 * @author dongfang.ding
 * @since 2019/6/27 11:17
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
     * 返回消息（很多场景下是被模糊化的提示，主要用来做toast）
     */
    private String message;

    /**
     * 实际消息（不需要做toast用，只是联调时能够知道具体消息）
     */
    @JsonIgnoreProfile(profile = {"pro", "prod"})
    private String subMessage;

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

    /**
     * 这个是异常消息模板里的占位符的填充内容，
     * 比如你今日发送短信已超过{0}次， 那么{0}就是占位符， 对应的就是params， 这个是异常消息模型里的字段。
     * 但是现在一般是跨服务调用， a调用b， b抛出异常，被包装出来后，a服务根据返回值调用com.boot.common.api.model.common.response.response.ResponseData#requiredSuccess()
     * 之后， 再次抛出异常，会丢失这个填充内容，因此需要再全局异常里把这个内容放进来，这样上层才可以再次抛出这个异常并填充这个内容
     */
    private Object[] formatParams;
    public ResponseData(String code, String message, String subMessage, long timestamp, T data) {
        this.code = code;
        this.message = message;
        this.subMessage = subMessage;
        this.timestamp = timestamp;
        this.data = data;
    }
    /**
     * @param code 参数
     * @param message 参数
     * @param subMessage 参数
     * @param timestamp 参数
     * @param data 待处理数据
     * @param extra 参数
     */
    public ResponseData(String code, String message, String subMessage, long timestamp, T data, Object extra) {
        this.code = code;
        this.message = message;
        this.subMessage = subMessage;
        this.timestamp = timestamp;
        this.data = data;
        this.extra = extra;
    }
    /**
     * @param code 参数
     * @param message 参数
     * @param subMessage 参数
     * @param timestamp 参数
     * @param data 待处理数据
     * @param extra 参数
     * @param formatParams 参数
     */
    public ResponseData(String code, String message, String subMessage, long timestamp, T data, Object extra, Object[] formatParams) {
        this.code = code;
        this.message = message;
        this.subMessage = subMessage;
        this.timestamp = timestamp;
        this.data = data;
        this.extra = extra;
        this.formatParams = formatParams;
    }

    /**
     * 成功返回数据方法
     *
     * @param data 待处理数据
     * @param <T> 泛型类型
     * @return
     */
    public static <T> ResponseData<T> success(T data) {
        return new ResponseData<>(
                BaseErrorCallbackCode.COMPLETE.getCode(), BaseErrorCallbackCode.COMPLETE.getCode(),
                BaseErrorCallbackCode.COMPLETE.getDescription(), System.currentTimeMillis(), data
        );
    }

    /**
     * 成功返回数据方法 - 自定义成功提示语
     *
     * @param data 待处理数据
     * @param desc 提示语
     */
    public static <T> ResponseData<T> success(T data, String desc) {
        return new ResponseData<>(BaseErrorCallbackCode.COMPLETE.getCode(), BaseErrorCallbackCode.COMPLETE.getCode(),
                desc, System.currentTimeMillis(), data
        );
    }

    /**
     * 成功返回数据方法
     *
     * @param data 待处理数据
     * @param <T> 泛型类型
     * @param extra 参数
     * @return
     */
    public static <T> ResponseData<T> success(T data, Object extra) {
        return new ResponseData<>(BaseErrorCallbackCode.COMPLETE.getCode(), BaseErrorCallbackCode.COMPLETE.getCode(),
                BaseErrorCallbackCode.COMPLETE.getDescription(), System.currentTimeMillis(), data, extra
        );
    }

    /**
     * 返回空数据
     *
     * @return
     */
    public static ResponseData<Void> empty() {
        return new ResponseData<>(BaseErrorCallbackCode.COMPLETE.getCode(), BaseErrorCallbackCode.COMPLETE.getCode(),
                BaseErrorCallbackCode.COMPLETE.getDescription(), System.currentTimeMillis(), null
        );
    }

    /**
     * 失败返回消息方法
     *
     * @param baseCallbackCode 回调码对象
     * @param <T> 泛型类型
     * @return
     */
    public static <T> ResponseData<T> failure(BaseCallbackCode baseCallbackCode) {
        return new ResponseData<>(baseCallbackCode.getCode(), baseCallbackCode.getBizMessage(),
                baseCallbackCode.getDescription(), System.currentTimeMillis(), null
        );
    }

    /**
     * 失败返回消息方法
     *
     * @param baseCallbackCode 回调码对象
     * @param <T> 泛型类型
     * @param extra 参数
     * @return
     */
    public static <T> ResponseData<T> failure(BaseCallbackCode baseCallbackCode, Object extra) {
        return new ResponseData<>(baseCallbackCode.getCode(), baseCallbackCode.getBizMessage(),
                baseCallbackCode.getDescription(), System.currentTimeMillis(), null, extra
        );
    }

    /**
     * 失败返回消息方法
     *
     * @param code 编码值
     * @param message 消息内容
     * @param <T> 泛型类型
     * @return
     */
    public static <T> ResponseData<T> failure(String code, String message) {
        return new ResponseData<>(code, message, message, System.currentTimeMillis(), null);
    }

    /**
     * 失败返回消息方法
     *
     * @param code 编码值
     * @param message 消息内容
     * @param <T> 泛型类型
     * @param subMessage 参数
     * @return
     */
    public static <T> ResponseData<T> failure(String code, String message, String subMessage) {
        return new ResponseData<>(code, message, subMessage, System.currentTimeMillis(), null);
    }

    /**
     * 失败返回消息方法
     *
     * @param code 编码值
     * @param message 消息内容
     * @param <T> 泛型类型
     * @param extra 参数
     * @return
     */
    public static <T> ResponseData<T> failure(String code, String message, Object extra) {
        return new ResponseData<>(code, message, message, System.currentTimeMillis(), null, extra);
    }

    /**
     * 失败返回消息方法
     *
     * @param code 编码值
     * @param message 消息内容
     * @param <T> 泛型类型
     * @param subMessage 参数
     * @param extra 参数
     * @return
     */
    public static <T> ResponseData<T> failure(String code, String message, String subMessage, Object extra) {
        return new ResponseData<>(code, message, subMessage, System.currentTimeMillis(), null, extra);
    }

    /**
     * 失败返回消息方法
     *
     * @param code 编码值
     * @param message 消息内容
     * @param <T> 泛型类型
     * @param subMessage 参数
     * @param extra 参数
     * @param formatParams 参数
     * @return
     */
    public static <T> ResponseData<T> failure(String code, String message, String subMessage, Object extra, Object[] formatParams) {
        return new ResponseData<>(code, message, subMessage, System.currentTimeMillis(), null, extra, formatParams);
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
        throw new BusinessException(code, message, formatParams);
    }


    /**
     * 获取返回数据， 如果响应码非成功，返回指定默认值
     *
     * @param defaultValue 默认值
     * @return
     */
    public T failureDefault(T defaultValue) {
        if (!isSuccess()) {
            return defaultValue;
        }
        return data;
    }

}