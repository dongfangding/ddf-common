package com.ddf.boot.common.governance.config;

import java.util.ArrayList;
import java.util.List;
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
         * Global governance observability switch.
         */
        private boolean enabled = true;

        /**
         * Thread pool metrics binding configuration.
         */
        private final ThreadPool threadPool = new ThreadPool();
    }

    @Getter
    @Setter
    public static class ThreadPool {

        /**
         * Enable governance thread pool metrics auto binding.
         */
        private boolean enabled = true;

        /**
         * When true, bind all supported thread pool beans regardless of patterns.
         */
        private boolean scanAll = false;

        /**
         * Micrometer meter name prefix used for executor metrics.
         */
        private String metricName = "custom.thread.pool";

        /**
         * Bean name include patterns. Supports Spring simple wildcard matching.
         */
        private List<String> includeBeanNamePatterns = new ArrayList<>(List.of(
                "*Executor", "*executor", "*Executors", "*executors", "*Pool", "*pool", "*Scheduler", "*scheduler"
        ));

        /**
         * Bean name exclude patterns. Supports Spring simple wildcard matching.
         */
        private List<String> excludeBeanNamePatterns = new ArrayList<>();
    }
}
