[TOC]

**持续开发中**

**持续开发中**

本项目结合了个人开发生涯中的经验，基于SpringBoot整合大部分常用开发功能，并尽量讲模块做成依赖包的方式， 基本都可以做到直接引入依赖，就完成实际项目开发的多种功能，代码中基本没有包含了各种demo的东西，而全部是直接贴近生产项目开发代码内容。当然至于实现方式的好坏，这个就是个人能力问题了。

# 依赖问题
1. Leaf和elastic对zookeeper和guava使用的版本都不一致， guava由于兼容性很烂， 在各自模块中使用了不同版本的guava。
2. 至于zk框架curator由于elastic-job中使用的版本为5.1.0， 该版本不再支持3.4.x版本的z, 因此需要提高zk的安装版本

# 模块

## 总览

| 模块                           | 功能                                                                        |
|------------------------------|---------------------------------------------------------------------------|
| ddf-common-core              | 核心功能包, 包含web项目核心配置、通用数据对象封装、全局异常、线程池优雅关闭注册、跨域处理、全局访问日志                    |
| ddf-common-captcha           | 整合谷歌kaptcha完成验证码的生成，支持验证码类型，普通字符、数学计算、滑动图片、点选文字                           |
| ddf-common-distributed-lock  | 分布式锁模块， 同时支持zk分布式锁和基于redisson客户端的redis的分布式锁                               |
| ddf-common-redis             | redis依赖、key规则生成工具、常用lua脚本整理、redisson集成                                    |
| ddf-common-authentication    | 自实现的简单清晰的用户认证处理模块， 提供一整套认证token的生成、校验、刷新等机制。提供上下文登录用户获取、通用请求头获取、日志MDC数据预埋 |
| ddf-common-limit             | 自实现基于redis的分布式限流模块，包含防表单重复提交和全局限流和接口限流                                    |
| ddf-common-ids-service       | 基于leaf重新封装的常用id生成方案，提供单个/批量获取雪花id/自定义业务code功能                             |
| ddf-common-mq                | 针对rabbitmq交换器绑定使用过于复杂封装的基于枚举定义自动初始化定义队列配置绑定和消费监听                          |
| ddf-common-rocketmq          | rocketmq依赖和消息定义规则生成                                                       |
| ddf-common-xxl-executor      | 基于xxl-job提供的执行器自动配置类                                                      |
| ddf-common-mybatis-generator | 自用的基于mybatis-plus调试的代码生成器                                                 |
| ddf-common-mybatis-plus      | mybatis-plus常用配置模块                                                        |
| ddf-common-log4j             | 使用log4j2的方式， 但是依赖不能传递，没搞清楚原因                                              |
| ddf-common-netty-broker      | 基于Netty实现的自定义协议实现，提供报文定义、编解码、加密传输、配置类；待实现，连接管理、集群转发                       |
| ddf-common-websocket         | websocket快速集成模块，可引入后快速集成，提供一整套报文定义、发送、连接管理、加密传输、集群消息转发、异步阻塞消息接收等          |
| ddf-common-mqtt              | 提供基于EMQ X 实现的mqtt协议的推送服务模块， 尚未完成                                          |
| ddf-common-vps               | 提供自搭建FastDFS服务的文件上传、图片压缩功能                                                |
| ddf-common-mongo             | mongo-db模块，如分页工具类、依赖等                                                     |
| ddf-common-es                | 目前仅提供elasticsearch依赖管理                                                    |
| ddf-common-zookeeper         | 基于zookeeper封装的一套基于服务上下线节点的自动化配置监听和回调                                      |
| ddf-common-third-party       | 第三方集成， 如oss, sms                                                          |
| ddf-common-security          | 未整理，目前属于废弃状态                                                              |
| ddf-common-trace             | 基于dubbo环境下适配已存在应用的简单上下文追踪工具， 未整理成通用，做demo备用                               |
| ddf-common-script            | 开发常用脚本收集                                                                  |
| ddf-common-sentinel          | spring-boot集成sentinel自动配置类                                                |
| ddf-common-jwt               | 不建议使用，原功能为快速集成jwt模块，提供了jwt通用生成参数类， 注解开启全局验证、白名单等功能，只需实现一个接口即可             |
| ddf-common-swagger           | 不建议使用的东西，提供swagger的核心配置和依赖                                                |

## ddf-common-core
核心功能包模块，具体介绍[移步到具体模块](https://github.com/dongfangding/ddf-common/tree/dev/ddf-common-core)

