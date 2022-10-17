[TOC]

**持续开发中**

**持续开发中**

本项目结合了个人开发生涯中的经验，基于SpringBoot整合大部分常用开发功能，并尽量讲模块做成依赖包的方式， 基本都可以做到直接引入依赖，就完成实际项目开发的多种功能，代码中基本没有包含了各种demo的东西，而全部是直接贴近生产项目开发代码内容。当然至于实现方式的好坏，这个就是个人能力问题了。

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

