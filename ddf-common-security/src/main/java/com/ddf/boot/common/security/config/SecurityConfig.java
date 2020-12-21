package com.ddf.boot.common.security.config;

import com.ddf.boot.common.jwt.config.JwtProperties;
import com.ddf.boot.common.jwt.config.PathMatch;
import com.ddf.boot.common.jwt.filter.JwtAuthorizationTokenFilter;
import java.util.List;
import java.util.stream.Collectors;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.config.annotation.method.configuration.EnableGlobalMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configuration.WebSecurityConfigurerAdapter;
import org.springframework.security.config.annotation.web.configurers.ExpressionUrlAuthorizationConfigurer;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;

/**
 * 无效用户访问需要认证的资源时响应401，如果前后端未分离，可以转发到登录界面
 *
 * @author dongfang.ding
 * @date 2019/9/16 9:42
 */
@Configuration
@EnableWebSecurity
@EnableGlobalMethodSecurity(prePostEnabled = true)
@ComponentScan(basePackages = "com.ddf.boot.common.security.config")
public class SecurityConfig extends WebSecurityConfigurerAdapter {

    @Autowired
    private JwtAuthenticationEntryPoint unauthorizedHandler;

    /**
     * 自定义基于JWT的安全过滤器
     */
    @Autowired
    JwtAuthorizationTokenFilter authenticationTokenFilter;

    @Autowired
    private JwtProperties jwtProperties;

/*    @Autowired
    public void configureGlobal(AuthenticationManagerBuilder auth) throws Exception {
        auth.userDetailsService(userDetailsServiceImpl).passwordEncoder(passwordEncoderBean());
    }*/

    @Bean
    public PasswordEncoder passwordEncoderBean() {
        return new BCryptPasswordEncoder();
    }

    @Bean
    @Override
    public AuthenticationManager authenticationManagerBean() throws Exception {
        return super.authenticationManagerBean();
    }

    @Override
    protected void configure(HttpSecurity httpSecurity) throws Exception {

        httpSecurity
                // 禁用 CSRF
                .csrf().disable().cors().and()
                // 授权异常
                .exceptionHandling().authenticationEntryPoint(unauthorizedHandler).and()
                // 不创建会话
                .sessionManagement().sessionCreationPolicy(SessionCreationPolicy.STATELESS);

        List<PathMatch> permitAllIgnores = jwtProperties.getIgnores();
        if (permitAllIgnores != null && !permitAllIgnores.isEmpty()) {
            ExpressionUrlAuthorizationConfigurer<HttpSecurity>.ExpressionInterceptUrlRegistry
                    expressionInterceptUrlRegistry = httpSecurity.authorizeRequests();

            String[] noMethodPaths = permitAllIgnores.stream().filter(
                    s -> StringUtils.isBlank(s.getHttpMethod()) || "*".equals(s.getHttpMethod())).map(
                    PathMatch::getPath).toArray(String[]::new);
            if (noMethodPaths.length > 0) {
                expressionInterceptUrlRegistry.antMatchers(noMethodPaths).permitAll();
            }

            List<PathMatch> methodPaths = permitAllIgnores.stream().filter(
                    (s) -> !"*".equals(s.getHttpMethod()) && StringUtils.isNotBlank(s.getHttpMethod())).collect(
                    Collectors.toList());

            if (!methodPaths.isEmpty()) {
                for (PathMatch methodPath : methodPaths) {
                    expressionInterceptUrlRegistry.antMatchers(HttpMethod.valueOf(methodPath.getHttpMethod()),
                            methodPath.getPath()
                    ).permitAll();
                }
            }
        }
        httpSecurity.authorizeRequests().anyRequest().authenticated()
                // 防止iframe 造成跨域
                .and().headers().frameOptions().disable();
        httpSecurity.addFilterBefore(authenticationTokenFilter, UsernamePasswordAuthenticationFilter.class);
    }
}
