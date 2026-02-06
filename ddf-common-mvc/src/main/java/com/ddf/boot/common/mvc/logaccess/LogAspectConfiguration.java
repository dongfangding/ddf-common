package com.ddf.boot.common.mvc.logaccess;

import lombok.Getter;
import lombok.Setter;

/**
 *
 * @author dongfang.ding on 2018/11/7
 * @see LogAspectRegistrar
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
