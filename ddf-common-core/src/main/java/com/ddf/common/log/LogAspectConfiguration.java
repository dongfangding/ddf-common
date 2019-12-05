package com.ddf.common.log;

import lombok.Getter;
import lombok.Setter;

/**
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
