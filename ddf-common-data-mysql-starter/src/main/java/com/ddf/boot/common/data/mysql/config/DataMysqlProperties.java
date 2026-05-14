package com.ddf.boot.common.data.mysql.config;

import lombok.Getter;
import lombok.Setter;
import org.springframework.boot.context.properties.ConfigurationProperties;

/**
 * Data mysql starter properties.
 *
 * @author dongfang.ding
 * @since 2026/3/11
 */
@Getter
@Setter
@ConfigurationProperties(prefix = "customizer.data.mysql")
public class DataMysqlProperties {

    /**
     * Global data mysql starter switch.
     */
    private boolean enabled = true;

    private final Druid druid = new Druid();


    @Getter
    @Setter
    public static class Druid {

        /**
         * Keep the current compatibility default unless explicitly disabled.
         */
        private boolean usePingMethod = false;
    }
}
