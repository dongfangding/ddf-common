//package com.ddf.boot.common.ext.oss.config;
//
//import com.aliyun.oss.OSS;
//import com.aliyun.oss.OSSClientBuilder;
//import com.aliyuncs.DefaultAcsClient;
//import com.aliyuncs.profile.DefaultProfile;
//import com.aliyuncs.profile.IClientProfile;
//import com.ddf.boot.common.core.util.PreconditionUtil;
//import lombok.extern.slf4j.Slf4j;
//import org.springframework.beans.BeansException;
//import org.springframework.beans.factory.SmartInitializingSingleton;
//import org.springframework.beans.factory.config.BeanDefinition;
//import org.springframework.context.ApplicationContext;
//import org.springframework.context.ApplicationContextAware;
//import org.springframework.context.ConfigurableApplicationContext;
//import org.springframework.context.annotation.Configuration;
//import org.springframework.context.support.GenericApplicationContext;
//import org.springframework.core.Ordered;
//import org.springframework.core.annotation.Order;
//
//import java.util.List;
//
///**
// * <p>description</p >
// *
// * @author Snowball
// * @version 1.0
// * @date 2020/11/14 16:22
// */
//@Slf4j
//@Configuration
//@Order(value = Ordered.HIGHEST_PRECEDENCE)
//public class OssBeanDefinitionRegistrar implements ApplicationContextAware, SmartInitializingSingleton {
//
//    private ConfigurableApplicationContext applicationContext;
//
//    private GenericApplicationContext genericApplicationContext;
//
//    private OssProperties ossProperties;
//
//    /**
//     * 默认注入的Oss Bean的name
//     */
//    public static String DEFAULT_OSS_CLIENT_NAME = "defaultOssClient";
//
//    /**
//     * 默认注入的IAcsClient Bean的name
//     */
//    public static String DEFAULT_ACS_CLIENT_NAME = "defaultAcsClient";
//
//    @Override
//    public void setApplicationContext(ApplicationContext applicationContext) throws BeansException {
//        this.applicationContext = (ConfigurableApplicationContext) applicationContext;
//        this.ossProperties = applicationContext.getBean(OssProperties.class);
//        this.genericApplicationContext = (GenericApplicationContext) applicationContext;
//    }
//
//
//    @Override
//    public void afterSingletonsInstantiated() {
//        check();
//        registryOssBean();
//        registryStsBean();
//    }
//
//    /**
//     * 额外参数检查
//     */
//    private void check() {
//        final List<BucketProperty> buckets = ossProperties.getBuckets();
//        // 由于当前设计的允许存在多个bucket, 那么存在多个的时候就会给使用方注入时带来不方便， 所以需要一个默认bean用来作为Primary, 这样使用注入的时候才不会报错
//        boolean includePrimary = buckets.size() == 1 || buckets.stream().filter(BucketProperty::isPrimary).count() == 1;
//        PreconditionUtil.checkArgument(includePrimary, "请且只能配置一个主存储桶， 参考属性primary");
//    }
//
//    /**
//     * 注册OSS bean
//     */
//    private void registryOssBean() {
//        // 这个是和bucket无关的，所以先注册一个全局的OSS客户端Bean
//        final OSS defaultOssClient = new OSSClientBuilder()
//                .build(ossProperties.getEndpoint(), ossProperties.getAccessKeyId(), ossProperties.getAccessKeySecret());
//        // 注册bean 并重新定义一些行为
//        genericApplicationContext.registerBean(DEFAULT_OSS_CLIENT_NAME, OSS.class, () -> defaultOssClient, (bd) -> {
//            bd.setScope(BeanDefinition.SCOPE_SINGLETON);
//            // com.aliyun.oss.OSS.shutdown
//            bd.setDestroyMethodName("shutdown");
//            bd.setPrimary(true);
//        });
//    }
//
//    /**
//     * 注册STS Bean
//     */
//    private void registryStsBean() {
//        DefaultProfile.addEndpoint("", "Sts", ossProperties.getStsEndpoint());
//        IClientProfile profile = DefaultProfile.getProfile("", ossProperties.getAccessKeyId(), ossProperties.getAccessKeySecret());
//        // 用profile构造client
//        DefaultAcsClient defaultAcsClient = new DefaultAcsClient(profile);
//        // 注册bean 并重新定义一些行为
//        genericApplicationContext.registerBean(DEFAULT_ACS_CLIENT_NAME, DefaultAcsClient.class, () -> defaultAcsClient, (bd) -> {
//            bd.setScope(BeanDefinition.SCOPE_SINGLETON);
//            // com.aliyun.oss.OSS.shutdown
//            bd.setDestroyMethodName("shutdown");
//            bd.setPrimary(true);
//        });
//    }
//}
