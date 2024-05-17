# 介绍

该模块主要用户单点项目使用， 无网关， 模块不仅负责校验处理请求、还负责解析。

因此该模块就是一个用来认证和处理用户上下文的东西。

# 使用

## 开启认证

在配置类或者启动类上使用注解`@EnableAuthenticate` 即可接入，
详细配置等在`com.ddf.boot.common.authentication.config.AuthenticationProperties` 类中

```java

@SpringBootApplication
@EnableAuthenticate
public class AppApplication {

    public static void main(String[] args) {
        SpringApplication.run(AppApplication.class);
    }
}
```

## 获取解析后的上下文数据

更多信息请参考类`com.banma.sunshine.authentication.annotation.util.UserContextUtil`
```java

public class ClassA {

    public void test() {
        // 获取当前请求用户信息
        final String userId = UserContextUtil.getUserId();
        final Long longUserId = UserContextUtil.getLongUserId();
    }
}
```


