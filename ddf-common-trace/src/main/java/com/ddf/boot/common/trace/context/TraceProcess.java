package com.ddf.boot.common.trace.context;

import java.io.Serializable;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * <p>保存上下文处理trace过程中用到的一些属性</p >
 *
 * @author Snowball
 * @version 1.0
 * @date 2021/08/24 15:46
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class TraceProcess implements Serializable {

    private static final long serialVersionUID = 2594628982105304423L;

    /**
     * 记录当前请求最新trace深度
     */
    private Integer traceDepthNum = 0;

    /**
     * 记录当前请求是否包含{@link cn.ibobei.framework.trace.annotation.QlTrace}注解
     * 只有存在这个注解才会处理参数
     */
    private boolean traceFlag = false;

    public static TraceProcess defaultInstance() {
        return new TraceProcess(0, false);
    }

    public Integer incrementAndGetDepth() {
        traceDepthNum ++;
        return traceDepthNum;
    }
}
