package com.ddf.boot.common.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;

import java.util.Date;

/**
 *
 * 通用基类

 * _ooOoo_
 * o8888888o
 * 88" . "88
 * (| -_- |)
 * O\ = /O
 * ___/`---'\____
 * .   ' \\| |// `.
 * / \\||| : |||// \
 * / _||||| -:- |||||- \
 * | | \\\ - /// | |
 * | \_| ''\---/'' | |
 * \ .-\__ `-` ___/-. /
 * ___`. .' /--.--\ `. . __
 * ."" '< `.___\_<|>_/___.' >'"".
 * | | : `- \`.;`\ _ /`;.`/ - ` : | |
 * \ \ `-. \_ __\ /__ _/ .-` / /
 * ======`-.____`-.___\_____/___.-`____.-'======
 * `=---='
 * .............................................
 * 佛曰：bug泛滥，我已瘫痪！
 *
 */
@Data
public class BaseDomain {

    @TableId(type = IdType.AUTO)
    protected Long id;

    @TableField(value = "CREATE_BY", fill = FieldFill.INSERT)
    protected String createBy;

    @TableField(value = "CREATE_TIME", fill = FieldFill.INSERT)
    protected Date createTime;

    @TableField(value = "MODIFY_BY", fill = FieldFill.INSERT_UPDATE)
    protected String modifyBy;

    @TableField(value = "MODIFY_TIME", fill = FieldFill.INSERT_UPDATE)
    protected Date modifyTime;

    /**
     * https://github.com/baomidou/mybatis-plus/issues/707
     * https://github.com/baomidou/mybatis-plus/issues/1387
     * 由于以上问题的存在，禁止使用框架生成的逻辑删除功能
     */
    /** 是否删除 0保留 1删除 */
//    @TableLogic
    protected Integer removed = 0;

    @Version
    @TableField(fill = FieldFill.INSERT)
    protected Integer version;

}