package com.ddf.boot.common.exception;

/**
 * @author dongfang.ding on 2019/2/26
 * 如不想使用国际化异常，又想统一项目中定义的异常，可定义在该类中使用
 */
public enum CNMessage implements GlobalExceptionCodeResolver {
    /**
     *
     */
    USER_LOGIN_NAME_REPEAT("登录名存在重复，数据不合法！"),


    ;
    private String message;

    CNMessage(String message) {
        this.message = message;
    }

    @Override
    public String get() {
        return this.message;
    }
}

