package com.ddf.boot.common.core.exception200;

/**
 * <p>服务端异常</p >
 *
 * @author dongfang.ding
 * @version 1.0
 * @date 2020/06/28 13:30
 */
public class ServerErrorException extends BaseException {

    public ServerErrorException(Throwable throwable) {
        super(throwable);
    }

    public ServerErrorException(BaseCallbackCode baseCallbackCode) {
        super(baseCallbackCode);
    }


    public ServerErrorException(String description) {
        super(description);
    }

    public ServerErrorException(String code, String description) {
        super(code, description);
    }

    public ServerErrorException(String code, String description, Object... params) {
        super(code, description, params);
    }

    /**
     * 提供一种消息占位符的方式， baseCallbackCode中的message包含占位符， 使用的时候格式化参数后作为最终异常消息
     * @param baseCallbackCode
     * @param params
     */
    public ServerErrorException(BaseCallbackCode baseCallbackCode, Object... params) {
        super(baseCallbackCode, params);
    }

    /**
     * 当前异常默认响应状态码
     *
     * @return
     */
    @Override
    public BaseCallbackCode defaultCallback() {
        return BaseErrorCallbackCode.SERVER_ERROR;
    }
}
