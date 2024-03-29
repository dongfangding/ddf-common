package com.ddf.boot.common.core.helper;

import com.ddf.boot.common.core.enumration.EnvironmentProfileEnum;
import com.google.common.collect.Lists;
import java.util.List;
import java.util.Objects;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.env.Environment;
import org.springframework.stereotype.Component;

/**
 * 系统环境变量帮助类$
 * <p>
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
 * @date 2019/12/24 0024 13:49
 */
@Component
public class EnvironmentHelper {

    @Autowired
    private Environment environment;

    /**
     * 判断目标环境是否存在于当前激活的环境变量中
     * 只要有一个值匹配当前激活的环境变量，就满足该判断
     *
     * @param targetProfile
     * @return boolean
     * @author dongfang.ding
     * @date 2019/12/24 0024 13:52
     **/
    public boolean checkIsExistOr(List<String> targetProfile) {
        String[] profileList;
        if (environment.getActiveProfiles().length > 0) {
            profileList = environment.getActiveProfiles();
        } else {
            profileList = environment.getDefaultProfiles();
        }
        for (String activeProfile : profileList) {
            for (String target : targetProfile) {
                if (activeProfile.equalsIgnoreCase(target)) {
                    return true;
                }
            }
        }
        return false;
    }

    /**
     * 获取当前应用使用的端口号
     *
     * @return
     */
    public int getPort() {
        return Integer.parseInt(Objects.requireNonNull(environment.getProperty("server.port")));
    }

    /**
     * 获取当前应用名称
     *
     * @return
     */
    public String getApplicationName() {
        return environment.getProperty("spring.application.name");
    }

    /**
     * 是否生产环境
     *
     * @return
     */
    public boolean isProdProfile() {
        return checkIsExistOr(Lists.newArrayList(EnvironmentProfileEnum.PRO.getCode()));
    }
}
