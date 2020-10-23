package com.ddf.boot.common.core.enumration;


import com.ddf.boot.common.core.exception200.GlobalCallbackCode;
import com.ddf.boot.common.core.util.PreconditionUtil;

import java.util.Arrays;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * <p>通用逻辑状态</p >
 *
 * @author dongfang.ding
 * @version 1.0
 * @date 2020/10/23 11:24
 */
public enum CommonLogic {

    /**
     * 无业务意义，前端对接使用，传入时代表全部
     */
    ALL(-1),

    /**
     * 否
     */
    FALSE(0),

    /**
     * 是
     */
    TRUE(1)

    ;

    private final Integer logic;

    static Map<Integer, CommonLogic> valueMappings;

    static {
        valueMappings = Arrays.stream(values()).collect(Collectors.toMap(CommonLogic::getLogic, value -> value));
    }

    CommonLogic(Integer logic) {
        this.logic = logic;
    }

    public Integer getLogic() {
        return logic;
    }

    public static CommonLogic getByLogic(Integer logic) {
        final CommonLogic contentLibraryStatus = valueMappings.get(logic);
        PreconditionUtil.checkArgument(contentLibraryStatus != null, GlobalCallbackCode.COMMON_LOGIC_ERROR);
        return contentLibraryStatus;
    }
}
