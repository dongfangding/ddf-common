package com.ddf.boot.common.governance.config;

import lombok.Getter;
import lombok.Setter;
import org.springframework.boot.context.properties.ConfigurationProperties;

/**
 * Governance starter properties.
 *
 * @author dongfang.ding
 * @since 2026/3/11
 */
@Getter
@Setter
@ConfigurationProperties(prefix = "customizer.governance")
public class GovernanceProperties {

    private final Mail mail = new Mail();

    private final Observability observability = new Observability();

    @Getter
    @Setter
    public static class Mail {

        /**
         * Global governance mail switch.
         *
         * <p>Default true means "allow auto configuration when the underlying
         * mail beans exist", not "force mail to start without config".
         */
        private boolean enabled = true;
    }

    @Getter
    @Setter
    public static class Observability {

        /**
         * Governance observability switch placeholder.
         */
        private boolean enabled = true;
    }
}
