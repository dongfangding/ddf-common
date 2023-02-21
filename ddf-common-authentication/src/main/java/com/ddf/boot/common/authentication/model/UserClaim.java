package com.ddf.boot.common.authentication.model;


import cn.hutool.core.util.ReflectUtil;
import java.io.Serializable;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.experimental.Accessors;

/**
 *
 * 生成token的用户信息对象
 *
 * @author dongfang.ding
 * @date 2019-12-07 16:45
 */
@Data
@NoArgsConstructor
@Accessors(chain = true)
@Builder
@AllArgsConstructor
public class UserClaim implements Serializable {
    private static final long serialVersionUID = -6557510720376811244L;

    public static final String CLAIM_USER_ID = "userId";
    public static final String CLAIM_USER_NAME = "userName";
    private static final String HEADER_CHARSET = "UTF-8";

    /**
     * 默认用户信息
     * 判断是否默认用户方法{@link UserClaim#isDefaultUser(UserClaim)}
     */
    private static final UserClaim DEFAULT_USER = UserClaim.builder().userId("0").username("SYSTEM").build();

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
     * 这个标识的规则，最好是服务端不论客户端是什么环境，都能获取到的一个值, 可以使用设备号， 如果是web端，则本地按照规则生成一个存储下来使用，
     * 最好能前缀以web-开头，避免和真实重复也能看出来是web自己生成的
     */
    private String credit;

    /**
     * 预留备注字段
     */
    private String remarks;

    /**
     * 是否禁用用户
     */
    private boolean disabled;

    /**
     * 预留的详细信息字段，使用方可以将自己想要放置的数据放到这个字段中；到时候想使用的时候可以自行解析回来
     * 仅支持字符格式，如果要放入复杂对象，请自行序列化
     */
    private String detail;

    public UserClaim(String userId, String username) {
        this.userId = userId;
        this.username = username;
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
     * 创建mock用户
     *
     * @param userId
     * @return
     */
    public static UserClaim mockUser(String userId) {
        final UserClaim claim = new UserClaim();
        claim.setUserId(userId);
        claim.setUsername("mock");
        claim.setCredit("*");
        return claim;
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

    /**
     * 是否默认用户
     *
     * @param userClaim
     * @return
     */
    public boolean isDefaultUser(UserClaim userClaim) {
        return Objects.equals(DEFAULT_USER.getUserId(), userClaim.getUserId());
    }
}
