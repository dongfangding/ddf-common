package com.ddf.boot.common.ids;

/**
 * 两种模式， 一种是将ids抽成一个单独的服务， 专门用来处理全局id的，
 *
 * 还有一种就是当前项目里的代码，每个项目都来一遍，每个项目都配置相关的属性
 *
 * 但是由于官网接口调用后需要解析，因此使用第二种方式的时候，最好也引入当前项目，只是不要启动这个类， 仅仅当成依赖，这样
 * 方便统一处理解析的代码
 *
 * @author dongfang.ding
 * @date 2020/8/15 0015 17:50
 */
//@SpringBootApplication
//@EnableLeafServer
//public class IdsApplication {
//
//    public static void main(String[] args) {
//        SpringApplication.run(IdsApplication.class, args);
//    }
//}
