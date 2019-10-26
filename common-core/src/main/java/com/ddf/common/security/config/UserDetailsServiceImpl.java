package com.ddf.common.security.config;

import com.ddf.common.exception.GlobalCustomizeException;
import com.ddf.common.exception.GlobalExceptionEnum;
import com.ddf.common.security.model.entity.BootUser;
import com.ddf.common.security.service.UserService;
import com.ddf.common.util.WebUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.stereotype.Service;

/**
 * 加载UserDetails用户信息
 *
 * @author dongfang.ding
 * @date 2019/9/16 10:23
 */
@Service
public class UserDetailsServiceImpl implements UserDetailsService {

    @Autowired
    private UserService userService;

    @Override
    public UserDetails loadUserByUsername(String username){
        BootUser bootUser = userService.findByName(username);
        if (bootUser == null) {
            throw new GlobalCustomizeException(GlobalExceptionEnum.INVALID_ACCOUNT);
        } else {
            return createJwtUser(bootUser);
        }
    }

    private UserDetails createJwtUser(BootUser bootUser) {
        return new UserClaim(
                bootUser.getId(),
                bootUser.getUserName(),
                WebUtil.getHost(),
                bootUser.getLastModifyPassword(),
                bootUser.getIsEnable() == 1,
                null
        );
    }
}
