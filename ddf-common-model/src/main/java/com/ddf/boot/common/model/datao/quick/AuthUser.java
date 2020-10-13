package com.ddf.boot.common.model.datao.quick;

import com.ddf.boot.common.core.entity.BaseDomain;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.ToString;
import lombok.experimental.Accessors;

import java.io.Serializable;
import java.util.Date;

/**
 * 认证用户实体
 */
@ToString(callSuper = true)
@NoArgsConstructor
@AllArgsConstructor 
@Data
@ApiModel("用户")
@Accessors(chain = true)
public class AuthUser extends BaseDomain implements Serializable {

    static final long serialVersionUID = -5091699981026819031L;

    @ApiModelProperty("姓名")
    private String username;

    @ApiModelProperty("用户随机码，生成密码的盐，注册时生成且不可变！")
	private String userToken;

    @ApiModelProperty("密码")
    private String password;

    @ApiModelProperty("邮箱")
    private String email;

    @ApiModelProperty("生日")
    private Date birthday;

    @ApiModelProperty("头像地址， 非全路径")
    private String avatar;

    @ApiModelProperty("头像缩略地址， 非全路径")
    private String shortAvatar;

    @ApiModelProperty("最后一次修改密码的时间")
    private Long lastModifyPassword;

    @ApiModelProperty("上次登录密码的时间")
    private Long lastLoginTime;

}