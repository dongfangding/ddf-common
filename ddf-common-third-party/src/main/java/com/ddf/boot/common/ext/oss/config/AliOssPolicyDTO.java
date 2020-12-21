package com.ddf.boot.common.ext.oss.config;

import java.util.List;
import lombok.Data;

/**
 * <p>STS授权policy信息</p >
 * <p>
 * https://help.aliyun.com/document_detail/100624.html?spm=5176.13910061.sslink.1.17134c67YQlXaj
 *
 * @author Administrator
 */
@Data
public class AliOssPolicyDTO {

    private transient String bucket;

    private transient String path;

    private String Version;

    private List<StatementBean> Statement;


    @Data
    public static class StatementBean {

        private String Effect;

        private List<String> Action;

        private List<String> Resource;

    }

}
