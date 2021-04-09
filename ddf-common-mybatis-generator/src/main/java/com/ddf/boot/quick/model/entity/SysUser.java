package com.ddf.boot.quick.model.entity;

import com.baomidou.mybatisplus.annotation.TableName;
import com.ddf.boot.common.core.model.BaseDomain;
import java.time.LocalDateTime;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
import lombok.experimental.SuperBuilder;

/**
 * <p>
 * 系统用户表
 * </p>
 *
 * @author mybatis-plus-generator
 * @since 2021-01-27
 */
@Data
@NoArgsConstructor
@SuperBuilder(toBuilder = true)
@EqualsAndHashCode(callSuper = true)
@TableName("sys_user" )
public class SysUser extends BaseDomain {

    private static final long serialVersionUID = 1L;

    /**
     * 用户id, 系统内部关联使用
     */
    private String userId;

    /**
     * 登陆名
     */
    private String loginName;

    /**
     * 昵称
     */
    private String nickname;

    /**
     * 密码
     */
    private String password;

    /**
     * 用户状态 0 正常 1 停用
     */
    private Integer status;

    /**
     * 最后登陆ip
     */
    private String lastLoginIp;

    /**
     * 最后登陆时间
     */
    private LocalDateTime lastLoginTime;

    /**
     * 最后一次修改密码时间
     */
    private LocalDateTime lastPwdResetTime;

    /**
     * 邮箱
     */
    private String email;

    /**
     * 手机号
     */
    private String mobile;

    /**
     * 性别 0 未知  1 男性 2 女性
     */
    private Integer sex;

    /**
     * 头像地址
     */
    private String avatarUrl;

    /**
     * 头像缩略图
     */
    private String avatarShortUrl;

    /**
     * 出生日期
     */
    private LocalDateTime birthday;

    /**
     * 身高，单位cm
     */
    private Integer height;

    /**
     * 体重，单位kg
     */
    private Integer weight;

    /**
     * 微信号
     */
    private String weiXin;

    /**
     * QQ号
     */
    private String qq;


}