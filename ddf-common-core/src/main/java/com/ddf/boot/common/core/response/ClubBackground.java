package com.ddf.boot.common.core.response;

import lombok.Data;

/**
 * <p>俱乐部背景</p >
 *
 * @author Snowball
 * @version 1.0
 * @date 2021/01/04 11:26
 */
@Data
public class ClubBackground {

    /**
     * 俱乐部推荐-图片背景
     */
    private String recommendPicUrl;

    /**
     * 邀请加入俱乐部-图片背景
     */
    private String invitePicUrl;

    /**
     * 个人中心俱乐部图片背景
     */
    private String personalCenterPicUrl;

    /**
     * 我的俱乐部图片背景
     */
    private String myClubPicUrl;

}
