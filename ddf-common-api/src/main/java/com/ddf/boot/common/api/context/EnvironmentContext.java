package com.ddf.boot.common.api.context;

import java.util.List;
import java.util.Objects;
import lombok.Getter;
import org.springframework.context.EnvironmentAware;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.env.Environment;

/**
 * <p>description</p >
 *
 * @author Snowball
 * @version 1.0
 * @date 2025/02/27 11:39
 */
@Configuration
public class EnvironmentContext implements EnvironmentAware {

    @Getter
    private static Environment environment;

    @Override
    public void setEnvironment(Environment environment) {
        EnvironmentContext.environment = environment;
    }


    /**
     * 检查提供的Profile是否在当前激活的Profile列表中
     *
     * @param targetProfile
     * @return
     */
    public static boolean checkIsExistOr(List<String> targetProfile) {
        String[] profileList;
        final Environment environment = EnvironmentContext.getEnvironment();
        if (Objects.isNull(environment)) {
            return false;
        }
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
}
