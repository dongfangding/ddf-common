package com.ddf.boot.quick.service.impl;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import com.ddf.boot.quick.model.entity.SysUser;
import com.ddf.boot.quick.mapper.SysUserMapper;
import com.ddf.boot.quick.service.ISysUserService;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import org.springframework.stereotype.Service;
import org.springframework.beans.factory.annotation.Autowired;

/**
 * <p>
 * 系统用户表 服务实现类
 * </p>
 *
 * @author mybatis-plus-generator
 * @since 2021-01-27
 */
@Service
@RequiredArgsConstructor(onConstructor_={@Autowired})
@Slf4j
public class SysUserServiceImpl extends ServiceImpl<SysUserMapper, SysUser> implements ISysUserService {

}
