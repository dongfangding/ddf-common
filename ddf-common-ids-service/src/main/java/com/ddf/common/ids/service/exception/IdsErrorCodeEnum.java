package com.ddf.common.ids.service.exception;

import com.ddf.boot.common.core.exception200.BaseCallbackCode;
import lombok.Getter;

/**
 * <p>ids异常状态码</p >
 *
 * @author Snowball
 * @version 1.0
 * @date 2021/07/21 14:58
 */
public enum IdsErrorCodeEnum implements BaseCallbackCode {

    /**
     * ====
     */
    CLOCK_BACK("IDS_001", "时钟回拨异常"),
    INTERRUPTED_EXCEPTION("IDS_002", "中断异常"),
    CLOCK_BACK_RETRY_FAILURE("IDS_003", "时钟回拨异常间隔较短，重试后依然异常"),
    BATCH_NUMBER_IS_VALID("IDS_004", "批量操作时的数量参数无效"),
    SEGMENT_IS_DISABLED("IDS_005", "号段模式已关闭"),
    ID_CACHE_INIT_FALSE("IDS_006", "IDCache未初始化成功"),
    KEY_NOT_EXISTS("IDS_007", "业务代码不存在"),
    TWO_SEGMENTS_ARE_NULL("IDS_008", "SegmentBuffer中的两个Segment均未从DB中装载"),

    ;

    @Getter
    private final String code;

    @Getter
    private final String description;

    @Getter
    private final String bizMessage;

    IdsErrorCodeEnum(String code, String description) {
        this.code = code;
        this.description = description;
        this.bizMessage = "id获取异常";
    }

    /**
     * 响应状态码
     *
     * @return
     */
    @Override
    public String getCode() {
        return code;
    }

    /**
     * 响应消息
     *
     * @return
     */
    @Override
    public String getDescription() {
        return description;
    }

    @Override
    public String getBizMessage() {
        return bizMessage;
    }
}
