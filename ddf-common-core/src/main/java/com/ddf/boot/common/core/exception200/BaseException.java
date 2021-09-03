package com.ddf.boot.common.core.exception200;

import com.google.common.base.Objects;
import java.text.MessageFormat;
import lombok.Getter;
import org.springframework.context.MessageSource;

/**
 * <p>基准异常类</p >
 *
 * @author dongfang.ding
 * @version 1.0
 * @date 2020/06/17 15:55
 */
public abstract class BaseException extends RuntimeException {

    /**
     * 异常code码
     */
    @Getter
    private String code;

    /**
     * 异常消息
     */
    @Getter
    private String description;

    /**
     * 某些消息需要提供占位符希望运行时填充数据，这里可以传入占位符对应的参数
     * 注意格式化参数使用的是{@link MessageSource}， 所以请注意原展位参数需使用{0} {1} 方式
     */
    @Getter
    private Object[] params;

    /**
     * 保存业务异常相关信息的类
     */
    @Getter
    private BaseCallbackCode baseCallbackCode;


    /**
     * 用来包装其它异常来转换为自定义异常
     *
     * @param throwable
     */
    public BaseException(Throwable throwable) {
        super(throwable);
        initCallback(defaultCallback());
    }

    /**
     * 推荐使用的系统自定义的一套体系的异常使用方式，传入异常错误码类
     *
     * @param baseCallbackCode
     */
    public BaseException(BaseCallbackCode baseCallbackCode) {
        super(baseCallbackCode.getDescription());
        initCallback(baseCallbackCode);
    }

    /**
     * 同上，但是额外提供一种消息占位符的方式， baseCallbackCode中的message包含占位符， 使用的时候格式化参数后作为最终异常消息
     * 占位字符串采用{0} {1}这种角标方式
     *
     * @param baseCallbackCode
     * @param params
     */
    public BaseException(BaseCallbackCode baseCallbackCode, Object... params) {
        super(MessageFormat.format(baseCallbackCode.getDescription(), params));
        initCallback(baseCallbackCode, params);
    }

    /**
     * 只简单抛出消息异常
     *
     * @param description
     */
    public BaseException(String description) {
        super(description);
        this.description = description;
        initCallback(defaultCallback());
    }

    /**
     * 不走系统定义的错误码定义体系， 但是使用错误码和消息体系
     *
     * @param code
     * @param description
     */
    public BaseException(String code, String description) {
        super(description);
        initCallback(code, description);
    }

    /**
     * 同上，但是支持占位符
     *
     * @param code
     * @param description
     * @param params
     */
    public BaseException(String code, String description, Object... params) {
        super(MessageFormat.format(description, params));
        initCallback(code, description, params);
    }


    private void initCallback(BaseCallbackCode baseCallbackCode, Object... params) {
        this.baseCallbackCode = baseCallbackCode;
        initCallback(
                baseCallbackCode.getCode() == null ? defaultCallback().getCode() : baseCallbackCode.getCode(),
                // 如果是默认状态码生效， 基本上说明使用方没有按照错误码体系走， 那么就不去解析， 直接使用传入的description
                Objects.equal(baseCallbackCode.getCode(), defaultCallback().getCode()) ? description : baseCallbackCode.getDescription(),
                params
        );
    }


    /**
     * 初始化状态码
     *
     * @param code
     * @param description
     */
    private void initCallback(String code, String description, Object... params) {
        this.code = code == null ? defaultCallback().getCode() : code;
        this.params = params;
        this.description = MessageFormat.format(description, params);
    }


    /**
     * 当前异常默认响应状态码
     *
     * @return
     */
    public abstract BaseCallbackCode defaultCallback();
}
