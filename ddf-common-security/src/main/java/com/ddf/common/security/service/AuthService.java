package com.ddf.common.security.service;


import com.baomidou.mybatisplus.extension.service.IService;
import com.ddf.common.security.model.entity.AuthUser;

import javax.validation.constraints.NotNull;

/**
 * @author dongfang.ding on 2018/12/1
 */
public interface AuthService extends IService<AuthUser> {
    /**
     * 登录
     *
     * @param userName 用户名
     * @param password 密码
     * @return
     */
    String login(String userName, String password);


    /**
     * 根据用户名查找用户
     *
     * @param userName
     * @return
     */
    AuthUser findByName(String userName);


    /**
     * 根据用户名和密码查找用户
     *
     * @param userName
     * @param password
     * @return
     */
    AuthUser getByUserNameAndPassword(@NotNull String userName, @NotNull String password);

}
