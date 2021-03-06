package com.ddf.boot.common.core.model;


import cn.hutool.core.util.ReflectUtil;
import java.io.Serializable;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.util.HashMap;
import java.util.Map;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.experimental.Accessors;

/**
 * 自定义jws的payload部分的用户信息类
 * 生成Jwt的时候调用参数为该类的方法即可，会把这个类中的所有有get方法的字段都生成到payload中
 * <p>
 * Jwt本身虽然是无状态的，不能直接将某个用户T掉过期掉，但是我们可以通过另外一种方式去实现；
 * 由于我们在生成token的时候会去校验最后登录时间是否和token中的一致，如果要过期某个用户的token,则我们直接修改该用户的最后登录时间即可
 *
 * @author dongfang.ding
 * @date 2019-12-07 16:45
 */
@Data
@NoArgsConstructor
@Accessors(chain = true)
public class UserClaim implements Serializable {
    private static final long serialVersionUID = -6557510720376811244L;

    public static final String CLAIM_USER_ID = "userId";
    public static final String CLAIM_USER_NAME = "userName";
    private static final String HEADER_CHARSET = "UTF-8";

    /**
     * 默认用户信息
     * 判断是否是默认用户
     * <pre class="code">
     *     UserClaim userClaim = WebUtil.getUserClaim();
     *     if (userClaim == UserClaim.DEFAULT_USER) {
     *         // 说明没有用户信息
     *     }
     * </pre>
     */
    private static final UserClaim DEFAULT_USER = new UserClaim("0", "SYSTEM");

    /**
     * 用户id
     */
    private String userId;
    /**
     * 用户名
     */
    private String username;
    /**
     * 用户登录的授信设备唯一标识符
     * 每一次签发token 都必须包含当前登录的设备标识，需要维护每个用户已签发的设备标识,如果
     * 用未签发过token的设备标识发送认证信息，服务器会拒绝认证
     * <p>
     * 这个标识的规则，最好是服务端不论客户端是什么环境，都能获取到的一个值，比如ip，当然ip只是
     * 符合条件，但却不是最好的方案，暂时还没想到更好的方案
     */
    private String credit;

    /**
     * 最后一次修改密码的时间，签发token时设置值；解析token时如果这个值与数据库最后一次修改密码的时间不匹配，
     * 证明密码已被修改，则该token校验不通过
     * 注意： 如果有其它字段也代表用户有效性变化的字段，那么当这些字段变化的时候也应该修改这个值；比如用户被删除，或者用户账号被修改之类的
     */
    private Long lastModifyPasswordTime;

    /**
     * 最后一次登录的时间，生成token时将该值放入其中，然后认证的时候判断是否和数据库中一致；不一致不允许再登录；
     * 通过该字段，如果我们想要过期某个用户的token,可以将该用户在数据库中的该字段的值修改掉即可
     */
    private Long lastLoginTime;

    /**
     * 预留备注字段
     */
    private String remarks;

    /**
     * 预留的详细信息字段，使用方可以将自己想要放置的数据放到这个字段中；到时候想使用的时候可以自行解析回来
     * 仅支持字符格式，如果要放入复杂对象，请自行序列化
     */
    private String detail;

    public UserClaim(String userId, String username) {
        this.userId = userId;
        this.username = username;
    }

    public UserClaim(String userId, String username, String credit, Long lastModifyPasswordTime, Long lastLoginTime) {
        this.userId = userId;
        this.username = username;
        this.credit = credit;
        this.lastModifyPasswordTime = lastModifyPasswordTime;
        this.lastLoginTime = lastLoginTime;
    }

    /**
     * 返回默认用户，无用户信息
     *
     * @return
     * @see UserClaim#defaultUser
     */
    public static UserClaim defaultUser() {
        return DEFAULT_USER;
    }

    /**
     * 将用户信息生成map，用以放到jwt的payload中
     *
     * @return
     */
    public Map<String, Object> toMap() {
        Map<String, Object> claimMap = new HashMap<>(16);
        Class<? extends UserClaim> aClass = this.getClass();
        Field[] fields = ReflectUtil.getFields(aClass);
        if (fields.length > 0) {
            for (Field field : fields) {
                try {
                    Method method = ReflectUtil.getMethod(
                            aClass,
                            "get" + field.getName().substring(0, 1).toUpperCase() + field.getName().substring(1)
                    );
                    if (method != null) {
                        // 如果能找到方法就设置
                        claimMap.put(field.getName(), method.invoke(this));
                    }
                } catch (Exception ignored) {
                    // 没有方法的直接忽略掉
                }
            }
        }
        return claimMap;
    }
}
