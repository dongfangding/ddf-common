package com.ddf.boot.common.mvc.logbool;

import lombok.Data;
import lombok.experimental.Accessors;

/**
 * <p>description</p >
 *
 * @author dongfang.ding
 * @version 1.0
 * @date 2020/06/12 15:11
 */
@Data
@Accessors(chain = true)
public class BoolReturn {

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

    public BoolReturn resultOf(boolean isModify, String userId, String userName) {
        return new BoolReturn().setModifySuccess(isModify).setUserId(userId).setUserName(userName);
    }

}
