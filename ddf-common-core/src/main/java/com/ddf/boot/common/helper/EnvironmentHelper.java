package com.ddf.boot.common.helper;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.env.Environment;
import org.springframework.stereotype.Component;

import java.util.List;

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
     * 只有有一个值匹配当前激活的环境变量，就满足该判断
     *
     * @param targetProfile
     * @return boolean
     * @author dongfang.ding
     * @date 2019/12/24 0024 13:52
     **/
    public boolean checkIsExistOr(List<String> targetProfile) {
        String[] activeProfiles = environment.getActiveProfiles();
        for (String activeProfile : activeProfiles) {
            for (String target : targetProfile) {
                if (activeProfile.equals(target)) {
                    return true;
                }
            }
        }
        return false;
    }
}
