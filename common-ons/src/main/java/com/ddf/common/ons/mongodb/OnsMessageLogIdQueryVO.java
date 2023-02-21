package com.ddf.common.ons.mongodb;

import java.io.Serializable;
import javax.validation.constraints.NotNull;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.util.Assert;

/**
 * ONS消息日志查询参数
 *
 * @author snowball
 * @date 2021/8/26 15:28
 **/
@Data
@NoArgsConstructor
public class OnsMessageLogIdQueryVO implements Serializable {

    private static final long serialVersionUID = -4247056597207376212L;

    public OnsMessageLogIdQueryVO(String objectId, CollectionName collectionName) {
        Assert.hasText(objectId, "对象ID不能为空");
        Assert.notNull(collectionName,"集合名称不能为空");
        this.objectId = objectId;
        this.collectionName = collectionName;
    }

    /**
     * id
     */
    private String objectId;

    /**
     * 集合名称
     */
    @NotNull(message = "集合名称不能为空")
    private CollectionName collectionName;

}
