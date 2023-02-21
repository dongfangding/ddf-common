
## 依赖
```xml
<!-- 日志组件 -->
<dependency>
    <groupId>org.springframework.boot</groupId>
    <artifactId>spring-boot-starter-log4j2</artifactId>
</dependency>
<!-- 日志组件依赖，异步打印日志 -->
<dependency>
    <groupId>com.lmax</groupId>
    <artifactId>disruptor</artifactId>
</dependency>

<!-- 演示排除logback依赖 -->
<dependency>
    <groupId>org.springframework.boot</groupId>
    <artifactId>spring-boot-starter-web</artifactId>
    <exclusions>
        <exclusion>
            <artifactId>spring-boot-starter-logging</artifactId>
            <groupId>org.springframework.boot</groupId>
        </exclusion>
    </exclusions>
</dependency>
```
## 注意事项

### 日志级别隔离
`ThresholdFilter`的匹配规则是配置的级别以以上都会满足条件， 这么一个特性，如果在做不同级别日志隔离的时候，如`INFO`级别， 那么
其实`INFO`.`WARN`, `ERROR`都会输出
```xml
<!--控制台只输出level及以上级别的信息（onMatch），其他的直接拒绝（onMismatch）-->
<ThresholdFilter level="INFO" onMatch="ACCEPT" onMismatch="DENY"/>
```
如果不喜欢这种方式，就是一定要`INFO`里只打印`INFO`, 就需要用到多个`Filters`，需要注意每个不同日志级别的文件都要替换

INFO文件日志内容如下
```xml
<!--控制台只输出level级别的信息（onMatch），其他的直接拒绝（onMismatch）-->
<Filters>
    <ThresholdFilter level="WARN" onMatch="DENY" onMismatch="NEUTRAL"/>
    <ThresholdFilter level="INFO" onMatch="ACCEPT" onMismatch="DENY" />
</Filters>
```

WARN文件日志内容
```xml
<Filters>
    <ThresholdFilter level="ERROR" onMatch="DENY" onMismatch="NEUTRAL"/>
    <ThresholdFilter level="WARN" onMatch="ACCEPT" onMismatch="DENY"/>
</Filters>
```

ERROR文件日志内容
```xml
<Filters>
    <ThresholdFilter level="ERROR" onMatch="ACCEPT" onMismatch="DENY"/>
</Filters>
```

### 异步使用
关于异步分为全异步和异步和同步混合使用， 全异步的话，可以通过启动脚本直接指定
```shell
# Don't forget to set system property to make all loggers asynchronous.
-Dlog4j2.contextSelector=org.apache.logging.log4j.core.async.AsyncLoggerContextSelector
```
