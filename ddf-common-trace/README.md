Trace 使用步骤

## 功能特性

- 集成SkyWalking工具，获取traceId，以及提供可扩展接口实现获取用户信息，通过拦截器将信息存储在线程上下文中
- 提供上下文对象，可以在同一线程内任意地方获取上述信息。
- 支持Dubbo隐式传参， 在消费端负责将上下文参数传递到提供者， 在接口提供者端负责将参数解析回上下文中，即使跨应用调用，使用时依然同2一样方便
- 支持线程上下文参数放入日志`MDC`中， 可通过修改日志格式将traceId和用户信息将所有服务请求串联起来
- 支持请求入参和返回值打印，同一个请求方法多次调用其它实现或者dubbo接口也可以正常工作。但为了避免返回值序列化内容过大，返回值默认不打印。需要通过注解`QlTrace`来实现高级特性， 一个是控制返回值打印，还有一个即当前请求调用次数多次拦截的问题， 也可以通过该注解指定最大处理次数，同一个请求每次拦截参数处理即算一次处理，到达执行次数后，不再进行参数拦截处理。

[点击查看效果图](https://devimage.91banban.com/Fj-YcWiBZKk2E9pVQ8LQ_qenEdeE)


## 引入步骤

### 引入依赖

```xml
		<dependency>
			<groupId>cn.ibobei.framework</groupId>
			<artifactId>qile-trace</artifactId>
			<version>1.0.0-SNAPSHOT</version>
			<exclusions>
				<exclusion>
					<artifactId>jackson-datatype-jsr310</artifactId>
					<groupId>com.fasterxml.jackson.datatype</groupId>
				</exclusion>
				<exclusion>
					<artifactId>qile-core</artifactId>
					<groupId>cn.ibobei.framework</groupId>
				</exclusion>
			</exclusions>
		</dependency>
```



### 实现获取用户信息的接口

实现接口`com.ddf.boot.common.trace.extra.IdentityCollectService`， 根据应用方自己特性，从当前请求中获取用户信息返回。则Trace模块可正常使用这部分值，否则无法获取用户相关信息, 然后将接口实现暴露成`bean`即可

```java
public interface IdentityCollectService {

    /**
     * 获取用户信息
     *
     * @param request
     * @return
     */
    Identity get(HttpServletRequest request);
}

```

已实现admin和zbapp

需要在admin增加如下接口实现

```java
@Service
public class AdminIdentityCollectServiceImpl implements IdentityCollectService {

    /**
     * 获取用户信息
     *
     * @param request
     * @return
     */
    @Override
    public Identity get(HttpServletRequest request) {
        Object principal = SecurityContextHolder.getContext()
                .getAuthentication()
                .getPrincipal();
        if (principal instanceof String) {
            return Identity.empty();
        }
        AuthUser authUser = (AuthUser) principal;
        return Identity.builder()
                .uid(authUser.getId().intValue())
                .os("admin")
                .build();
    }
}
```

需要在zbapp增加如下实现类

```java
@Service
public class AppIdentityCollectServiceImpl implements IdentityCollectService {

    @Resource
    private AuthService authService;
    
    /**
     * 获取用户信息
     *
     * @param request
     * @return
     */
    @Override
    public Identity get(HttpServletRequest request) {
        final String token = request.getParameter("token");
        String os = StringUtils.defaultIfBlank(request.getParameter("os"), "app");
        String imei = StringUtils.defaultIfBlank(request.getParameter("imei"), "");
        if (StringUtils.isBlank(token)) {
            return Identity.empty();
        }
        final int uid = authService.decryptionToken(token, new ReturnModel());
        return Identity.builder()
                .uid(uid)
                .os(os)
                .imei(imei)
                .build();
    }
}
```

## 特殊处理

非`springb-boot`应用，手动导入配置类，代码如下

```java
@Configuration
@Import(value = {TraceContextConfig.class})
public class QlTraceConfig {
  
}
```

在mvc配置`servlet-context.xml`中添加拦截器

```xml
	<mvc:interceptors>
		<mvc:interceptor>
			<mvc:mapping path="/**"/>
			<bean class="com.ddf.boot.common.trace.aop.IdentityInterceptor"/>
		</mvc:interceptor>
	</mvc:interceptors>
```



## 详细使用

### 使用上下文对象

```java
// 获取当前请求traceId
final String traceId = QlTraceContext.getTraceId();
// 获取当前请求uid
final Integer uid = QlTraceContext.getUid();
final ContextDomain domain = QlTraceContext.getContextDomain();
final Identity identity = domain.getIdentity();
// 获取当前请求os
final String os = identity.getOs();
// 获取当前请求imei
final String imei = identity.getImei();
```

### MDC日志串联

可以在日志格式中配置如下参数， 则通过`traceId`可以看到同一个请求的所有相关日志，跨服务依然如此。

支持参数

| 参数名  | 参数含义         | 配置格式    |
| ------- | ---------------- | ----------- |
| traceId | 请求对应的唯一id | %X{traceId} |
| uid     | 请求对应的用户id | %X{uid}     |

### 自定义Trace

由于默认参数aop是通过指定包名或者注解使用，在部分情况下是无法满足的。在不满足拦截规则的方法上通过注解`com.ddf.boot.common.trace.annotation.QlTrace`也可以强制完成拦截。

注意： 所有需要拦截打印的必须添加注解`QlTrace`，虽然拦截规则中包含了`dubbo`的注解还有`Spring`的`Controller`注解，但为了避免日志过多， 还必须添加自定义注解才会真正的处理参数拦截逻辑。

如下。则当前方法会被拦截，并且会额外打印返回值，如果方法内部多次调用其它满足拦截规则的方法，最大只会处理10次。

拦截深度兼容了跨服务问题， 同一个请求内只会在第一个有`QlTrace`注解的地方才会开始处理参数解析， 后续同一个请求内的其它方法则不需要加该注解， 即使是跨服务，参数也会传递下去，同理拦截深度的累加也是跨服务计数的。

```java
@QlTrace(traceReturn = true, traceReturn = 10)
public String test() {

}
```




