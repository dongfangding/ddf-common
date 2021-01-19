package com.ddf.boot.common.model.datao.quick;

import com.ddf.boot.common.core.model.BaseDomain;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import java.io.Serializable;
import java.util.Date;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.ToString;
import lombok.experimental.Accessors;

/**
 * 认证用户实体
 *
 * @author dongfang.ding
 */
@ToString(callSuper = true)
@NoArgsConstructor
@AllArgsConstructor
@Data
@ApiModel("用户")
@Accessors(chain = true)
public class AuthUser extends BaseDomain implements Serializable {

    static final long serialVersionUID = -5091699981026819031L;

    /**
     * 姓名
     */
    @ApiModelProperty("姓名")
    private String username;

    /**
     * 用户随机码，生成密码的盐，注册时生成且不可变！
     */
    @ApiModelProperty("用户随机码，生成密码的盐，注册时生成且不可变！")
    private String userToken;

    /**
     * 密码
     */
    @ApiModelProperty("密码")
    private String password;

    /**
     * 邮箱
     */
    @ApiModelProperty("邮箱")
    private String email;

    /**
     * 生日
     */
    @ApiModelProperty("生日")
    private Date birthday;

    /**
     * 头像地址， 非全路径
     */
    @ApiModelProperty("头像地址， 非全路径")
    private String avatar;

    /**
     * 头像缩略地址， 非全路径
     */
    @ApiModelProperty("头像缩略地址， 非全路径")
    private String shortAvatar;

    /**
     * 最后一次修改密码的时间
     */
    @ApiModelProperty("最后一次修改密码的时间")
    private Long lastModifyPassword;

    /**
     * 上次登录密码的时间
     */
    @ApiModelProperty("上次登录密码的时间")
    private Long lastLoginTime;

}