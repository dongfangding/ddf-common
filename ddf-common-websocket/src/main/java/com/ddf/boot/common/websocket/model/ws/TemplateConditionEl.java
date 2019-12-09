package com.ddf.boot.common.websocket.model.ws;

import lombok.Data;

/**
 * 模板表达式


 */
@Data
public class TemplateConditionEl {

    private ConditionKey condition;

    private String value;

    public enum ConditionKey {
        /** 不包含,所有值都必须不包含才算匹配成功，value的关系是并且 */
        NOT_CONTAINS,
        /** 包含, 包含一个即算匹配成功， value的关系是或 */
        CONTAINS,
        /** 等于 */
        EQUALS
    }
}
