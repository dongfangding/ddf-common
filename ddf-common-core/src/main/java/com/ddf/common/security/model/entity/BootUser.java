package com.ddf.common.security.model.entity;

import com.ddf.common.entity.BaseDomain;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.ToString;

import java.util.Date;

/**
 * 
 */
@ToString(callSuper = true)
@NoArgsConstructor
@AllArgsConstructor 
@Data
@ApiModel("用户")
public class BootUser extends BaseDomain {

    /** 姓名*/
    @ApiModelProperty("姓名")
    private String userName;

    /** 密码*/
    @ApiModelProperty("密码")
    private String password;

    /** 邮箱*/
    @ApiModelProperty("邮箱")
    private String email;

    /** 生日*/
    @ApiModelProperty("生日")
    private Date birthday;

    @ApiModelProperty("最后一次修改密码的时间")
    private Long lastModifyPassword;

    @ApiModelProperty("用户是否有效 0 否 1 是")
    private Byte isEnable;

}