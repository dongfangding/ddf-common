[TOC]

**持续开发中**

**持续开发中**

**持续开发中**

**持续开发中**

**持续开发中**

TODO

1.  以来的第三方组件中依赖的开源库的版本低于系统中自己使用的， 如leaf的guava和curator， guava.version版本暂时为了兼容leaf降级了
2.  RestControllerAdvice实现的方式，如何使用配置实现，外部传入包路径

# 依赖问题
1. Leaf和elastic对zookeeper和guava使用的版本都不一致， guava由于兼容性很烂， 在各自模块中使用了不同版本的guava。
2. 至于zk框架curator由于elastic-job中使用的版本为5.1.0， 该版本不再支持3.4.x版本的z, 因此需要提高zk的安装版本


# 模块

## 总览

| 模块                        | 功能                                                         |
| --------------------------- | ------------------------------------------------------------ |
| ddf-common-core             | 核心功能包                                                  |
| ddf-common-captcha             | 整合谷歌kaptcha完成验证码的生成，包括计算型验证码                                             |
| ddf-common-distributed-lock | 分布式锁模块                                                 |
| ddf-common-ids              | id模块                                                   |
| ddf-common-jwt              | 快速集成jwt模块，提供了jwt通用生成参数类， 注解开启全局验证、白名单等功能，只需实现一个接口即可 |
| ddf-common-limit              | 自实现接口级别的分布式限流模块，包含防表单重复提交和接口限流 |
| ddf-common-mongo            | mongo-db模块，如分页工具类、依赖等                           |
| ddf-common-mq               | 基于rabbitmq的初始化定义队列配置和消费监听                   |
| ddf-common-mybatis-generator               | 自用的基于mybatis-plus调试的代码生成器                   |
| ddf-common-mybatis-plus     | mybatis-plud常用配置模块                                     |
| ddf-common-netty-broker     | 基于Netty实现的自定义协议实现，提供报文定义、编解码、加密传输、配置类；待实现，连接管理、集群转发 |
| ddf-common-websocket        | websocket快速集成模块，可引入后快速集成，提供一整套报文定义、发送、连接管理、加密传输、集群消息转发、异步阻塞消息接收等 |
| ddf-common-redis                         |            redis依赖、key规则生成工具、常用lua脚本整理、redisson集成                                                |
| ddf-common-security         | 未整理                                                       |
| ddf-common-rocketmq      | rocketmq依赖和消息定义规则生成                                  |
| ddf-common-script      | 开发常用脚本收集                                  |
| ddf-common-sentinel      | spring-boot集成sentinel自动配置类                                  |
| ddf-common-third-party      | 第三方集成， 如oss, sms                                      |
| ddf-common-xxl-executor        | 基于xxl-job提供的执行器自动配置类 |
| ddf-common-zookeeper        | 基于zookeeper封装的一套基于服务上下线节点的自动化配置监听和回调 |

## ddf-common-core
核心功能包

### 基本配置

位于包`com.ddf.boot.common.core.config`路径下

包含swagger基本配置， `mvc`跨域基本配置，默认线程池和任务调度线程池

### 基类实体

* com.ddf.boot.common.core.model.BaseDomain

  提供了一个实体层的基类，并使用了mybatis和spring-data等注解进行修饰， **id必须自己生成**

* com.ddf.boot.common.core.model.BaseQuery

  提供了所有查询对象的基类，提供了分页和排序参数， 查询对象继承该对象，根据自己使用框架，该对象提供了

  `ofSpringData`和`ofMybatis`方法，将与分页和排序相关属性抽成对象框架所需要的分页对象

### 异常处理

位于包`com.ddf.boot.common.core.exception200`路径下

#### 基本使用

默认为将所有的错误都映射为http状态码200，通过对象参数将业务异常包含的异常状态吗返回给前端

* com.ddf.boot.common.core.exception200.ExceptionHandlerAdvice

  异常处理类

* com.ddf.boot.common.core.exception200.BaseException

  异常基类， 建议自定义实现的异常要继承该类，该类基类提供了异常状态码和异常消息的处理

* 可直接使用的异常类及状态码

  | 异常类                | 状态码 | 建议使用场景                         |
  | --------------------- | ------ | ------------------------------------ |
  | AccessDeniedException | 403    | 可以获取用户身份，但对请求无权限     |
  | BadRequestException   | 400    | 请求参数不合法                       |
  | BusinessException     | 500    | 用户开发人员自身抛出的业务方面的异常 |
  | ServerErrorException  | 500    | 服务本身异常                         |
  | UnauthorizedException | 401    | 一般用于登录，用户身份校验不通过     |

#### 扩展

1.  如果想要出现异常时，接管异常处理，可以实现接口`com.ddf.boot.common.core.exception200.ExceptionHandlerMapping`

   接管异常大致有两个方面的需求

   * 仅仅是希望异常时做一些额外的处理，那么做自己想做的，但是最终返回null， 程序依然会去执行异常逻辑
   * 有一些异常需要额外的处理，就是不想用默认的异常封装处理，那么返回自己想要的即可，只要不为null，就会以实现为准

2. 出现异常了，就是不想要http的状态码为200

   由于目前设计的异常基类都必须包含一个code字段， 因此系统在`com.ddf.boot.common.core.config.GlobalProperties#exceptionCodeToResponseStatus`提供了一个属性，可以在出现异常时，将异常的状态码作为http的状态码返回，你只需要定义自己的异常，将异常的状态码定义成想要返回的http状态码即可

   ```yaml
   customs:  # 自定义的属性最好都写在custom前缀下，方便辨认
     global-properties:
       exceptionCodeToResponseStatus: true
   ```

3. 异常时我想要返回给前端详细的异常堆栈，方便前端出现问题，直接将异常堆栈抛出来，而不用每次查询日志怎么办？

   这个参数其实默认是开启的，出现异常时会有一个stack返回；那么如果想要在生产环境时关闭该敏感信息怎么处理，

   同样系统也提供了也给属性`com.ddf.boot.common.core.config.GlobalProperties#ignoreErrorTraceProfile`， 由于默认时开启状态，因此该属性的含义是允许你配置某些环境不要返回这个信息。

   如下为当profile为pre或prod时，该异常堆栈不会返回

   ```yaml
   customs:  # 自定义的属性最好都写在custom前缀下，方便辨认
     global-properties:
       ignoreErrorTraceProfile:  # 过滤将异常堆栈信息输出打前端接口返回值的环境
         - pre
         - prod
   ```

### 统一返回对象

希望能够返回给前端一个通用对象，这样方便整个对接以及数据的统一。该实现方式由于是基于`@RestControllerAdvice`和`@ControllerAdvice`， **因此使用时在控制器层返回自己的原始数据对象即可**

该类结构如下，实际上异常返回是遵循的即是此类。自己返回的数据即包含在属性data下， 程序内部以处理控制器直接返回`String`时会出现的问题

```java
public class ResponseData<T> {
    /** 返回消息代码 */
    private String code;
    /** 返回消息 */
    private String message;
    /**
     * 错误堆栈信息
     */
    private String stack;
    /** 响应时间 */
    private long timestamp;
    /** 返回数据 */
    private T data;
}
```

**问题列表**

* 如果某个返回对象，我就是不想再被统一对象包装怎么办？

  提供了一个属性`com.ddf.boot.common.core.interceptor.CommonResponseBodyAdviceProperties#ignoreReturnType`，

  当控制器层返回的对象全类名包含在配置列表中时， 统一返回对象将不会继续执行包装逻辑

  ```yaml
  customs:
    response-body-advice:
      ignoreReturnType:
        -- 要忽略的类的全类名
  ```

 

### 访问日志打印和慢接口事件回调

该功能位于包`com.ddf.boot.common.core.logaccess`路径下

默认关闭，如需开启需在配置类上使用注解`@EnableLogAspect`打开该功能的支持

**提供功能**

* 程序运行正常时，打印入参和出参对象，以及接口耗时

* 程序运行异常时，打印入参对象信息和异常信息

* 提供慢接口统计，使用注解中的`slowTime`方法来指定慢接口的时间界定值，一旦方法运行时间超过这个值，则提供一个接口触发事件，实现接口`com.ddf.boot.common.core.logaccess.SlowEventAction`即可完成对该接口的自定义处理

* 忽略某个接口的慢接口统计，可能慢接口我们更希望的是一种绝大部分的一个值，但是有个别接口由于业务比较复杂，预先已经能够预料到，所以我们可能希望这个接口就不需要触发慢接口事件了。

  使用注解中的属性`ignore`类指定类名，目前仅支持到类名，则该类不会被统计满接口

### 关键信息修改日志回调

该功能位于包`com.ddf.boot.common.core.logbool`下

注意该类不是广义上的接口修改日志，而是针对特定关键信息的修改；

由于修改失败存在两种情况，一是对应的数据不存在，则修改失败；二是对应的数据存在，但由于数据库中的值已经是要修改的值；

如果我们要针对某些特定功能做修改日志，自己实现时就会面临上面那个情况，需要自己写判断；但其实最终我们只关心结果，有没有修改成功，存在不存在还是其它的我都不关心；

而且可能系统不止一个功能需要做修改日志，那么每个都要自己写一遍就会很麻烦。

如果是上面这种情况，那么当前功能则能够很好的支持，并且减少开发量

**使用步骤**

* 使用注解`@LogBoolReturn`标识方法

* 方法返回对象必须为`com.ddf.boot.common.core.logbool.BoolReturn`

  该类包含的信息如下：

  需要告知系统，你有没有修改成功，这个是很关键的；以及修改人相关信息

  ```java
      /**
       * 执行结果，因为有的接口操作，一旦到达某个状态就直接return true，没有执行业务，
       * 日志需要知道调用接口时到底有没有对数据进行修改，结果如何
       */
      private boolean modifySuccess;
 
      /**
       * 不在这个包里融合进业务系统对用户上下文获取的方式
       */
      private String userId;
 
      private String userName;
  ```

* 最终拦截类会生成日志对象`com.ddf.boot.common.core.logbool.LogBoolReturnResult`

  ```java
  @Data
  @Accessors(chain = true)
  public class LogBoolReturnResult {
 
      private BoolReturn boolReturn;
 
      /**
       * 日志名称
       */
      private String logName;
 
      /**
       * 执行类名
       */
      private String className;
 
      /**
       * 执行方法名
       */
      private String methodName;
 
      /**
       * 参数json格式
       */
      private String param;
  }
  ```

* 实现接口`com.ddf.boot.common.core.logbool.LogBoolReturnAction`

  最后一步，当前工具并不负责持久化日志，遵循了约定之后，会返回上述关键日志信息，通过该接口回调实现；你只需要实现该接口，然后是直接持久化这部分信息还是扩展一些其它字段再持久化，看你需要

* 最后一个吐槽，你可能会觉得这一切似乎并没有什么鸟用；但当修改日志多了之后，就会发现，自己只需要实现一个接口去实现日志落库以及遵循一些小的约定，这一切看起来还是值得的

### 工具包和帮助类

- 常用工具包
  首选Hutool

- com.ddf.boot.common.core.util.BeanUtil

  提供bean拷贝工具

- com.ddf.boot.common.core.util.IdsUtil
  基于Hutool单机版直接使用的雪花id

- com.ddf.boot.common.core.util.JsonUtil
  Json相关序列化方法

- com.ddf.boot.common.core.util.SpringContextHolder
  提供在非Spring容器中静态获取Spring bean的功能

- com.ddf.boot.common.core.helper.EnvironmentHelper

  与环境变量相关的帮助类，如判断某个环境是否包含在当前应用激活的profile中，这个会经常经常用户代码在不同环境中的逻辑隔离

- com.ddf.boot.common.core.helper.ThreadBuilderHelper

  快速构建线程池的帮助类，默认拒绝策略为`CallerRunsPolicy`

- com.ddf.boot.common.core.util.WebUtil
  提供提供获取HttpServlet对象及常用方法

- com.ddf.boot.common.core.util.VerifyCodeUtils
  网上摘录的验证码生成工具

## ddf-common-distributed-lock

分布式锁实现包， 目前仅仅提供基于zookeeper的分布式锁实现

**配置属性**

```yaml
distributed:
  lock: 
    zookeeper: 
      connectString: 127.0.0.1:2181 # 配置zk的连接地址
      root: "/ddf" # 配置分布式所产生的文件所在的跟目录，注意必须遵循zk文件路径以/开头
```
