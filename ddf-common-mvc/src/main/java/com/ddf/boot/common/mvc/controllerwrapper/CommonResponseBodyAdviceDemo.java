package com.ddf.boot.common.mvc.controllerwrapper;

/**
 * 这里提供一个demo, 想要自己服务中生效的话，直接继承AbstractCommonResponseBodyAdvice，
 * 然后补充自己的@RestControllerAdvice(basePackages = {"com"})，通用包中默认不再生效包范围
 *
 * @author dongfang.ding
 * @date 2019/6/27 11:15
 */
//@RestControllerAdvice(basePackages = {"com"})
//@Order
public class CommonResponseBodyAdviceDemo extends AbstractCommonResponseBodyAdvice {

}
