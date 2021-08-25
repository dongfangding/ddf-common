package com.ddf.boot.common.trace.context;

import java.io.Serializable;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * <p>上下文存储数据专用</p >
 *
 * @author Snowball
 * @version 1.0
 * @date 2021/08/24 10:30
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ContextDomain implements Serializable {

    private static final long serialVersionUID = -1216370986425976772L;

    /**
     * 请求串联的traceId
     */
    private String traceId;

    /**
     * 身份信息
     */
    private Identity identity = Identity.empty();


    // ===========辅助类参数===========

    /**
     * 保存trace参数
     */
    private TraceProcess traceProcess = new TraceProcess();
}
