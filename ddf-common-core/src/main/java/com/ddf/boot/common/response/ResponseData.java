package com.ddf.boot.common.response;

import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.experimental.Accessors;
import org.springframework.http.HttpStatus;

/**
 * 统一响应内容类
 *
 * @author dongfang.ding
 * @date 2019/6/27 11:17
 */
@Data
@NoArgsConstructor
@Accessors(chain = true)
public class ResponseData<T> {
    /** 异常状态码 */
    private Integer status;
    /** 返回消息代码 */
    private String code;
    /** 返回消息 */
    private String message;
    /** 响应时间 */
    private long timestamp;
    /** 请求路径 */
    private String path;
    /** 返回数据 */
    private T data;


    public ResponseData(Integer status, String code, String message, long timestamp, String path, T data) {
        this.status = status;
        this.code = code;
        this.message = message;
        this.timestamp = timestamp;
        this.path = path;
        this.data = data;
    }

    public static <T> ResponseData<T> success(T data, String path) {
        return new ResponseData<>(200, "success", "处理成功", System.currentTimeMillis(), path, data);
    }

    /**
     * 构建鉴权失败响应类
     * @param message
     * @return
     */
    public static ResponseData<String> unauthorized(String message) {
        ResponseData<String> responseData = new ResponseData<>();
        responseData.setCode(HttpStatus.UNAUTHORIZED.value() + "");
        responseData.setMessage("授权失败: " + message);
        responseData.setTimestamp(System.currentTimeMillis());
        responseData.setData(null);
        return responseData;
    }
}
