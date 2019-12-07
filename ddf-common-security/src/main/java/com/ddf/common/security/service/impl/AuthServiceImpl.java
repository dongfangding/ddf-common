package com.ddf.common.security.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.ddf.common.constant.GlobalConstants;
import com.ddf.common.exception.GlobalCustomizeException;
import com.ddf.common.exception.GlobalExceptionEnum;
import com.ddf.common.jwt.model.UserClaim;
import com.ddf.common.jwt.util.JwtUtil;
import com.ddf.common.security.mapper.UserMapper;
import com.ddf.common.security.model.entity.AuthUser;
import com.ddf.common.security.service.AuthService;
import com.ddf.common.util.SecureUtil;
import com.ddf.common.util.WebUtil;
import com.google.common.base.Preconditions;
import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import javax.validation.constraints.NotNull;

/**
 * @author dongfang.ding on 2018/12/1
 */
@Service
public class AuthServiceImpl extends ServiceImpl<UserMapper, AuthUser> implements AuthService {
    /**
     * 登录
     *
     * @param userName 用户名
     * @param password 密码
     * @return
     */
    @Override
    @Transactional(readOnly = true)
    public String login(@NotNull String userName, @NotNull String password) {
        Preconditions.checkArgument(StringUtils.isNotBlank(userName), "用户名不能为空!");
        Preconditions.checkArgument(StringUtils.isNotBlank(password), "密码不能为空!");
        AuthUser authUser = getByUserNameAndPassword(userName, password);
        if (authUser == null) {
            throw new GlobalCustomizeException(GlobalExceptionEnum.USERNAME_OR_PASSWORD_INVALID);
        }
        if (GlobalConstants.BYTE_FALSE.equals(authUser.getIsEnable())) {
            throw new GlobalCustomizeException(GlobalExceptionEnum.ACCOUNT_NOT_ENABLE);
        }

        UserClaim userClaim = new UserClaim();
        userClaim.setUserId(authUser.getId()).setUsername(authUser.getUserName()).setLastModifyPasswordTime(
                // 默认注册时间
                authUser.getLastModifyPassword()).setCredit(WebUtil.getHost());
        return JwtUtil.defaultJws(userClaim);
    }

    /**
     * 根据用户名查找用户
     *
     * @param userName
     * @return
     */
    @Override
    public AuthUser findByName(String userName) {
        if (StringUtils.isBlank(userName)) {
            return null;
        }
        LambdaQueryWrapper<AuthUser> queryWrapper = Wrappers.lambdaQuery();
        queryWrapper.eq(AuthUser::getUserName, userName);
        return getOne(queryWrapper);
    }

    /**
     * 根据用户名和密码查找用户
     *
     * @param userName
     * @param password
     * @return
     */
    @Override
    public AuthUser getByUserNameAndPassword(@NotNull String userName, @NotNull String password) {
        Preconditions.checkArgument(StringUtils.isNotBlank(userName), "用户名不能为空!");
        Preconditions.checkArgument(StringUtils.isNotBlank(password), "密码不能为空!");
        LambdaQueryWrapper<AuthUser> queryWrapper = Wrappers.lambdaQuery();
        queryWrapper.eq(AuthUser::getUserName, userName);
        queryWrapper.eq(AuthUser::getPassword, SecureUtil.signWithHMac(password));
        return getOne(queryWrapper);
    }
}
