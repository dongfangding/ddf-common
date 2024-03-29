package com.ddf.boot.common.core.resolver;

import cn.hutool.core.collection.CollectionUtil;
import cn.hutool.core.util.StrUtil;
import com.ddf.boot.common.api.model.common.QueryParam;
import com.ddf.boot.common.api.util.JsonUtil;
import com.ddf.boot.common.core.util.ContextKey;
import com.fasterxml.jackson.core.type.TypeReference;
import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;
import java.util.Collections;
import java.util.List;
import org.springframework.core.MethodParameter;
import org.springframework.web.bind.support.WebDataBinderFactory;
import org.springframework.web.context.request.NativeWebRequest;
import org.springframework.web.method.support.HandlerMethodArgumentResolver;
import org.springframework.web.method.support.ModelAndViewContainer;

/**
 * 将将前端参数名称为{@link ContextKey#queryParams}的字符串值解析为List<QueryParam>，用于查询；
 * 事实上使用@RequestBody的话，SpringMvc也是可以处理的，但是为了方便多个参数一同使用GET传输，因此加了
 * 这个参数处理器，也为了尽量保留住Body，给其它更重要的数据留用
 * <p>
 * _ooOoo_
 * o8888888o
 * 88" . "88
 * (| -_- |)
 * O\ = /O
 * ___/`---'\____
 * .   ' \\| |// `.
 * / \\||| : |||// \
 * / _||||| -:- |||||- \
 * | | \\\ - /// | |
 * | \_| ''\---/'' | |
 * \ .-\__ `-` ___/-. /
 * ___`. .' /--.--\ `. . __
 * ."" '< `.___\_<|>_/___.' >'"".
 * | | : `- \`.;`\ _ /`;.`/ - ` : | |
 * \ \ `-. \_ __\ /__ _/ .-` / /
 * ======`-.____`-.___\_____/___.-`____.-'======
 * `=---='
 * .............................................
 * 佛曰：bug泛滥，我已瘫痪！
 *
 * @author dongfang.ding on 2019/1/16
 */
public class QueryParamArgumentResolver implements HandlerMethodArgumentResolver {

    /**
     * 判断当前参数是否需要解析，该解析器用来解析参数类型为List<QueryParam>
     *
     * @param parameter
     * @return
     */
    @Override
    public boolean supportsParameter(MethodParameter parameter) {
        boolean isList = List.class.equals(parameter.getParameterType());
        if (isList) {
            Type genericType = parameter.getGenericParameterType();
            if (genericType instanceof ParameterizedType) {
                ParameterizedType parameterizedType = (ParameterizedType) genericType;
                Type actualTypeArgument = parameterizedType.getActualTypeArguments()[0];
                return actualTypeArgument.getTypeName().equals(QueryParam.class.getName());
            }
        }
        return false;
    }


    /**
     * 将前端参数名称为{@link ContextKey#queryParams}的字符串值解析为List<QueryParam>，用于查询
     *
     * @param parameter
     * @param mavContainer
     * @param webRequest
     * @param binderFactory
     * @return
     * @throws Exception
     */
    @Override
    public List<QueryParam> resolveArgument(MethodParameter parameter, ModelAndViewContainer mavContainer,
            NativeWebRequest webRequest, WebDataBinderFactory binderFactory) throws Exception {
        String queryParamsStr = webRequest.getParameter(ContextKey.queryParams.name());
        if (StrUtil.isBlank(queryParamsStr)) {
            return Collections.emptyList();
        }
        final List<QueryParam> params = JsonUtil.getInstance()
                .readValue(queryParamsStr, new TypeReference<List<QueryParam>>() {
                    @Override
                    public Type getType() {
                        return super.getType();
                    }
                });
        if (CollectionUtil.isNotEmpty(params)) {
            for (QueryParam param : params) {
                if (param.getRelative() == null) {
                    param.setRelative(QueryParam.Relative.AND);
                }
                if (param.getOp() == null) {
                    param.setOp(QueryParam.Op.EQ);
                }
            }
        }
        return params;
    }
}
