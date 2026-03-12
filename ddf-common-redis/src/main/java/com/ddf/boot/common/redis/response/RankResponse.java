package com.ddf.boot.common.redis.response;

import java.io.Serializable;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * <p>description</p >
 *
 * @author Snowball
 * @version 1.0
 * @since 2025/09/16 16:35
 */
@Data
@NoArgsConstructor
@AllArgsConstructor(staticName = "of")
public class RankResponse implements Serializable {

    private String element;

    private Double score;

    private Long rank;
}
