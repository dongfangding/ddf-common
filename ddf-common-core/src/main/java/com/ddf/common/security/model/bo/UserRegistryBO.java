package com.ddf.common.security.model.bo;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

import java.io.Serializable;
import java.util.Date;

/**
 * 用户注册参数类
 *
 * @author dongfang.ding
 * @date 2019/9/16 13:57
 */
@Data
@ApiModel("用户注册参数类")
public class UserRegistryBO implements Serializable {

    private static final long serialVersionUID = -7636506343515002132L;

    @ApiModelProperty("用户名")
    private String userName;

    @ApiModelProperty("密码")
    private String password;

    @ApiModelProperty("邮箱")
    private String email;

    @ApiModelProperty("生日")
    private Date birthday;
}
