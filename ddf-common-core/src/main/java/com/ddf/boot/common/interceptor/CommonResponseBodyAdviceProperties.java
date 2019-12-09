package com.ddf.boot.common.interceptor;

import lombok.Getter;
import lombok.Setter;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;

/**
 * 针对统一返回格式封装的一些属性类
 *
 * @author dongfang.ding
 * @date 2019/9/2 17:08
 */
@Component
@ConfigurationProperties(prefix = "customs.response-body-advice")
@Getter
@Setter
public class CommonResponseBodyAdviceProperties {

    /** 返回值类型的全类名 */
    private List<String> ignoreReturnType = new ArrayList<>();

}
