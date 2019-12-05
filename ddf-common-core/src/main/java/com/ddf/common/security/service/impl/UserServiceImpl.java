package com.ddf.common.security.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.ddf.common.constant.GlobalConstants;
import com.ddf.common.exception.GlobalCustomizeException;
import com.ddf.common.exception.GlobalExceptionEnum;
import com.ddf.common.security.config.JwtProperties;
import com.ddf.common.security.config.JwtUtil;
import com.ddf.common.security.config.UserClaim;
import com.ddf.common.security.mapper.UserMapper;
import com.ddf.common.security.model.bo.UserRegistryBO;
import com.ddf.common.security.model.entity.BootUser;
import com.ddf.common.security.model.vo.BootUserVo;
import com.ddf.common.security.service.UserService;
import com.ddf.common.util.BeanUtil;
import com.ddf.common.util.SecureUtil;
import com.ddf.common.util.WebUtil;
import com.google.common.base.Preconditions;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import javax.validation.constraints.NotNull;

/**
 * @author dongfang.ding on 2018/12/1
 */
@Service
public class UserServiceImpl extends ServiceImpl<UserMapper, BootUser> implements UserService {
    @Autowired
    private JwtProperties jwtProperties;

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
        BootUser bootUser = getByUserNameAndPassword(userName, password);
        if (bootUser == null) {
            throw new GlobalCustomizeException(GlobalExceptionEnum.USERNAME_OR_PASSWORD_INVALID);
        }
        if (GlobalConstants.BYTE_FALSE.equals(bootUser.getIsEnable())) {
            throw new GlobalCustomizeException(GlobalExceptionEnum.ACCOUNT_NOT_ENABLE);
        }

        UserClaim userClaim = new UserClaim();
        userClaim.setUserId(bootUser.getId()).setUsername(bootUser.getUserName()).setLastModifyPasswordTime(
                // 默认注册时间
                bootUser.getLastModifyPassword()).setCredit(WebUtil.getHost());
        return JwtUtil.defaultJws(userClaim, jwtProperties.getExpiredMinute());
    }

    /**
     * 注册用户，用户名或邮箱已存在，不能注册
     *
     * @param userRegistryBo 新增用户对象
     * @return
     */
    @Override
    @Transactional(rollbackFor = Exception.class)
    public BootUserVo registry(UserRegistryBO userRegistryBo) {
        Preconditions.checkNotNull(userRegistryBo);
        Preconditions.checkArgument(!StringUtils.isAnyBlank(userRegistryBo.getUserName(), userRegistryBo.getPassword(),
                userRegistryBo.getEmail()), "用户名、密码、邮箱都不能为空！");
        LambdaQueryWrapper<BootUser> queryWrapper = Wrappers.lambdaQuery();
        queryWrapper.eq(BootUser::getUserName, userRegistryBo.getUserName());
        if (count(queryWrapper) > 0) {
            throw new GlobalCustomizeException(GlobalExceptionEnum.USERNAME_EXIST);
        }
        queryWrapper = Wrappers.lambdaQuery();
        queryWrapper.eq(BootUser::getEmail, userRegistryBo.getEmail());
        if (count(queryWrapper) > 0) {
            throw new GlobalCustomizeException(GlobalExceptionEnum.EMAIL_HAD_REGISTERED);
        }
        BootUser bootUser = BeanUtil.copy(userRegistryBo, BootUser.class);
        if (bootUser == null) {
            throw new GlobalCustomizeException(GlobalExceptionEnum.LOGIN_ERROR);
        }
        bootUser.setPassword(SecureUtil.signWithHMac(bootUser.getPassword()));
        bootUser.setLastModifyPassword(System.currentTimeMillis());
        save(bootUser);
        BootUserVo bootUserVo = new BootUserVo();
        BeanUtil.copy(bootUser, bootUserVo);
        return bootUserVo;
    }


    /**
     * 根据用户名查找用户
     *
     * @param userName
     * @return
     */
    @Override
    public BootUser findByName(String userName) {
        if (StringUtils.isBlank(userName)) {
            return null;
        }
        LambdaQueryWrapper<BootUser> queryWrapper = Wrappers.lambdaQuery();
        queryWrapper.eq(BootUser::getUserName, userName);
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
    public BootUser getByUserNameAndPassword(@NotNull String userName, @NotNull String password) {
        Preconditions.checkArgument(StringUtils.isNotBlank(userName), "用户名不能为空!");
        Preconditions.checkArgument(StringUtils.isNotBlank(password), "密码不能为空!");
        LambdaQueryWrapper<BootUser> queryWrapper = Wrappers.lambdaQuery();
        queryWrapper.eq(BootUser::getUserName, userName);
        queryWrapper.eq(BootUser::getPassword, SecureUtil.signWithHMac(password));
        return getOne(queryWrapper);
    }
}
