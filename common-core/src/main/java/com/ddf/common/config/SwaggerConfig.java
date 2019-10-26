package com.ddf.common.config;

import com.ddf.common.constant.GlobalConstants;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import springfox.documentation.builders.ApiInfoBuilder;
import springfox.documentation.builders.ParameterBuilder;
import springfox.documentation.builders.PathSelectors;
import springfox.documentation.builders.RequestHandlerSelectors;
import springfox.documentation.schema.ModelRef;
import springfox.documentation.service.ApiInfo;
import springfox.documentation.service.Parameter;
import springfox.documentation.spi.DocumentationType;
import springfox.documentation.spring.web.plugins.Docket;
import springfox.documentation.swagger2.annotations.EnableSwagger2;

import java.util.ArrayList;
import java.util.List;

/**
 * swagger的配置类
 *
 * 项目启动后文档访问地址为[ip]:[port]/[context-path]/swagger-ui.html
 *
 * @author dongfang.ding
 * @date 2019/5/28 14:09
 */
@Configuration
@EnableSwagger2
public class SwaggerConfig {

    @Bean
    public Docket createRestApi() {

        ParameterBuilder authorizationPar = new ParameterBuilder();
        List<Parameter> pars = new ArrayList<>();
        pars.add(authorizationPar.name("Authorization").description("token信息")
                .modelRef(new ModelRef("string")).parameterType("header").defaultValue("Bearer ")
                .required(false).build());
        pars.add(authorizationPar.name("Content-Type").description("Content-Type")
                .modelRef(new ModelRef("string")).parameterType("header").defaultValue("application/json")
                .required(true).build());

        return new Docket(DocumentationType.SWAGGER_2)
                .apiInfo(apiInfo())
                .select()
                .apis(RequestHandlerSelectors.basePackage(GlobalConstants.BASE_PACKAGE))
                .paths(PathSelectors.any())
                .build().globalOperationParameters(pars);
    }

    private ApiInfo apiInfo() {
        return new ApiInfoBuilder()
                .title("spring-boot的脚手架工程")
                .description("使用spring-boot集成一些常用框架，来完成一个基本框架的搭建，可以用来作为实际项目开发的基准")
                .version("1.0")
                .build();
    }
}
