package com.ddf.common.security.service;


import com.baomidou.mybatisplus.extension.service.IService;
import com.ddf.common.security.model.bo.UserRegistryBO;
import com.ddf.common.security.model.entity.BootUser;
import com.ddf.common.security.model.vo.BootUserVo;
import org.springframework.transaction.annotation.Transactional;

import javax.validation.constraints.NotNull;

/**
 * @author dongfang.ding on 2018/12/1
 */
public interface UserService extends IService<BootUser> {
    /**
     * 登录
     *
     * @param userName 用户名
     * @param password 密码
     * @return
     */
    String login(String userName, String password);


    @Transactional(rollbackFor = Exception.class)
    BootUserVo registry(UserRegistryBO userRegistryBo);

    /**
     * 根据用户名查找用户
     *
     * @param userName
     * @return
     */
    BootUser findByName(String userName);


    /**
     * 根据用户名和密码查找用户
     *
     * @param userName
     * @param password
     * @return
     */
    BootUser getByUserNameAndPassword(@NotNull String userName, @NotNull String password);

}
