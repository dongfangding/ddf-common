package com.ddf.common.security.model.vo;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;
import lombok.experimental.Accessors;

import java.util.Date;

/**
 * 用户展示层对象
 *
 * @author dongfang.ding
 * @date 2019/10/9 11:47
 */
@Data
@ApiModel("用户展示层对象")
@Accessors(chain = true)
public class BootUserVo {

    /** 姓名*/
    @ApiModelProperty("姓名")
    private String userName;

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
