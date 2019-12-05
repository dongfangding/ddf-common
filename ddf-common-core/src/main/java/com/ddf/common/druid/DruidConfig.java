package com.ddf.common.druid;

import com.alibaba.druid.pool.DruidDataSource;
import com.alibaba.druid.support.http.StatViewServlet;
import com.alibaba.druid.support.http.WebStatFilter;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.boot.web.servlet.FilterRegistrationBean;
import org.springframework.boot.web.servlet.ServletRegistrationBean;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.stereotype.Component;

import javax.sql.DataSource;
import java.sql.SQLException;
import java.util.Properties;

/**
 * @author dongfang.ding on 2019/1/21
 */
@ConfigurationProperties(prefix = "spring.datasource")
@Configuration
@Component
public class DruidConfig {

    private Logger logger = LoggerFactory.getLogger(DruidConfig.class);
    @Getter
    @Setter
    private Config druidProperties = new Config();

    /**
     * http://ip:port/druid/login.html
     * 使用数据库用户名和密码登录可以查看SQL监控
     * @return
     */
    @Bean
    public ServletRegistrationBean druidServlet() {
        ServletRegistrationBean<StatViewServlet> reg = new ServletRegistrationBean<>();
        reg.setServlet(new StatViewServlet());
        reg.addUrlMappings("/druid/*");
        reg.addInitParameter("loginUsername", druidProperties.getUsername());
        reg.addInitParameter("loginPassword", druidProperties.getPassword());
        reg.addInitParameter("logSlowSql", druidProperties.getLogSlowSql());
        return reg;
    }

    @Bean
    public FilterRegistrationBean filterRegistrationBean() {
        FilterRegistrationBean<WebStatFilter> filterRegistrationBean = new FilterRegistrationBean<>();
        filterRegistrationBean.setFilter(new WebStatFilter());
        filterRegistrationBean.addUrlPatterns("/*");
        filterRegistrationBean.addInitParameter("exclusions", "*.js,*.gif,*.jpg,*.png,*.css,*.ico,/druid/*");
        filterRegistrationBean.addInitParameter("profileEnable", "true");
        return filterRegistrationBean;
    }

    @Bean
    public DataSource druidDataSource() {
        DruidDataSource datasource = new DruidDataSource();
        datasource.setUrl(druidProperties.getUrl());
        datasource.setUsername(druidProperties.getUsername());
        datasource.setPassword(druidProperties.getPassword());
        datasource.setInitialSize(druidProperties.getInitialSize());
        datasource.setMinIdle(druidProperties.getMinIdle());
        datasource.setMaxActive(druidProperties.getMaxActive());
        datasource.setMaxWait(druidProperties.getMaxWait());
        datasource.setTimeBetweenEvictionRunsMillis(druidProperties.getTimeBetweenEvictionRunsMillis());
        datasource.setMinEvictableIdleTimeMillis(druidProperties.getMinEvictableIdleTimeMillis());
        datasource.setValidationQuery(druidProperties.getValidationQuery());
        datasource.setTestWhileIdle(druidProperties.isTestWhileIdle());
        datasource.setTestOnBorrow(druidProperties.isTestOnBorrow());
        datasource.setTestOnReturn(druidProperties.isTestOnReturn());
        datasource.setPoolPreparedStatements(druidProperties.isPoolPreparedStatements());
        datasource.setMaxOpenPreparedStatements(druidProperties.getMaxOpenPreparedStatements());
        datasource.setAsyncInit(druidProperties.isAsyncInit());
        datasource.setConnectProperties(druidProperties.getConnectionProperties());
        try {
            datasource.setFilters(druidProperties.getFilters());
        } catch (SQLException e) {
            logger.error("druid configuration initialization filter", e);
        }
        return datasource;
    }

    @Getter
    @Setter
    @AllArgsConstructor
    @NoArgsConstructor
    private class Config {
        private String url;

        private String username;

        private String password;

        private int initialSize;

        private int minIdle;

        private int maxActive;

        private int maxWait;

        private int timeBetweenEvictionRunsMillis;

        private int minEvictableIdleTimeMillis;

        private String validationQuery;

        private boolean testWhileIdle;

        private boolean testOnBorrow;

        private boolean testOnReturn;

        private String filters;

        private String logSlowSql;

        private boolean poolPreparedStatements;

        private int maxOpenPreparedStatements;

        private boolean asyncInit;

        private Properties connectionProperties;

    }
}
