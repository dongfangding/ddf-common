package com.ddf.boot.common.core.model;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import com.fasterxml.jackson.datatype.jsr310.deser.LocalDateTimeDeserializer;
import com.fasterxml.jackson.datatype.jsr310.ser.LocalDateTimeSerializer;
import java.time.LocalDateTime;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.experimental.SuperBuilder;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.Id;
import org.springframework.data.annotation.LastModifiedDate;
import org.springframework.data.annotation.Version;

/**
 * 通用基类
 * <p>
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
 * @author dongfang.ding
 */
@Data
@SuperBuilder(toBuilder = true)
@NoArgsConstructor
public class BaseDomain {

    /**
     * 代表删除的字段含义，为空，方便处理唯一索引问题
     *
     */
    public static final Integer IS_DEL_LOGIC_DELETE_VALUE = null;

    /**
     * 代表有效的字段含义
     *
     */
    public static final Integer IS_DEL_LOGIC_VALID_VALUE = 0;


//    @TableId(type = IdType.AUTO)
    @Id
    protected Long id;

//    @TableField(value = "create_by", fill = FieldFill.INSERT)
    protected String createBy;

//    @TableField(value = "create_time", fill = FieldFill.INSERT)
    @CreatedDate
    @JsonSerialize(using = LocalDateTimeSerializer.class)
    @JsonDeserialize(using = LocalDateTimeDeserializer.class)
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss", locale = "zh", timezone = "GMT+8")
    protected LocalDateTime createTime;

//    @TableField(value = "modify_by", fill = FieldFill.INSERT_UPDATE)
    protected String modifyBy;

//    @TableField(value = "modify_time", fill = FieldFill.INSERT_UPDATE)
    @LastModifiedDate
    @JsonSerialize(using = LocalDateTimeSerializer.class)
    @JsonDeserialize(using = LocalDateTimeDeserializer.class)
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss", locale = "zh", timezone = "GMT+8")
    protected LocalDateTime modifyTime;

    /**
     * https://github.com/baomidou/mybatis-plus/issues/707
     * https://github.com/baomidou/mybatis-plus/issues/1387
     * 由于以上问题的存在，禁止使用框架生成的逻辑删除功能
     */
    /**
     * 是否删除 0保留 删除时，将当前记录的id赋值给这个字段代表删除，然后数据库的唯一索引约束要带上这个字段， 为了解决简单的0和1带来的唯一索引问题
     */
    protected Integer isDel = 0;

    @Version
//    @TableField(fill = FieldFill.INSERT)
//    @org.springframework.data.annotation.Version
    protected Integer version = 1;

}
