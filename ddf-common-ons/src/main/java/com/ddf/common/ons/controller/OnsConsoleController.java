package com.ddf.common.ons.controller;

import com.aliyun.ons20190214.models.OnsGroupCreateResponse;
import com.aliyun.ons20190214.models.OnsGroupDeleteResponse;
import com.aliyun.ons20190214.models.OnsTopicCreateResponse;
import com.aliyun.ons20190214.models.OnsTopicDeleteResponse;
import com.ddf.boot.common.api.model.common.response.PageResult;
import com.ddf.common.ons.console.client.OnsClientOperations;
import com.ddf.common.ons.console.model.ConsoleOnsDLQMessageGetByIdRequest;
import com.ddf.common.ons.console.model.ConsoleOnsDLQMessagePageQueryByGroupIdRequest;
import com.ddf.common.ons.console.model.ConsoleOnsGroupCreateRequest;
import com.ddf.common.ons.console.model.ConsoleOnsGroupDeleteRequest;
import com.ddf.common.ons.console.model.ConsoleOnsGroupListRequest;
import com.ddf.common.ons.console.model.ConsoleOnsTopicCreateRequest;
import com.ddf.common.ons.console.model.ConsoleOnsTopicDeleteRequest;
import com.ddf.common.ons.console.model.ConsoleOnsTopicListRequest;
import com.ddf.common.ons.console.model.ConsoleOnsTopicSubDetailRequest;
import com.ddf.common.ons.console.model.response.ConsoleOnsDLQMessagePageQueryByGroupResponse;
import com.ddf.common.ons.console.model.response.ConsoleOnsGroupListResponse;
import com.ddf.common.ons.console.model.response.ConsoleOnsTopicListResponse;
import com.ddf.common.ons.console.model.response.ConsoleOnsTopicSubListResponse;
import java.util.List;
import java.util.Map;
import java.util.Set;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * <p>ONS控制台</p >
 *
 * @menu ONS控制台
 * @author Snowball
 * @version 1.0
 * @since 2021/05/14 17:50
 */
@RestController
@RequestMapping("/ons/console")
public class OnsConsoleController {

    /**
     * 获取配置的环境列表
     *
     * @return
     */
    @GetMapping("/env")
    public Set<String> getEnvList() {
        return OnsClientOperations.getOnsEnvKeySet();
    }

    /**
     * 创建多环境TOPIC
     *
     * @param request
     * @return
     */
    @PostMapping("topic")
    public static Map<String, OnsTopicCreateResponse> onsTopicCreate(@RequestBody @Validated
    ConsoleOnsTopicCreateRequest request) {
        request.setCurrentUser("");
        return OnsClientOperations.onsTopicCreate(request);
    }


    /**
     * 删除多环境TOPIC
     *
     * @param request
     * @return
     */
    @DeleteMapping("/topic")
    public static Map<String, OnsTopicDeleteResponse> onsTopicDelete(@RequestBody @Validated
    ConsoleOnsTopicDeleteRequest request) {
        request.setCurrentUser("");
        return OnsClientOperations.onsTopicDelete(request);
    }

    /**
     * 多环境查询账号下所有 Topic 的信息列表
     *
     * @param request
     * @return
     */
    @GetMapping("topic")
    public static Map<String, List<ConsoleOnsTopicListResponse>> onsTopicList(@Validated
    ConsoleOnsTopicListRequest request) {
        request.setCurrentUser("");
        return OnsClientOperations.onsTopicList(request);
    }

    /**
     * 查看Topic的在线订阅组
     *
     * @param request
     * @return
     */
    @GetMapping("topic/subs")
    public static List<ConsoleOnsTopicSubListResponse> onsTopicSubDetail(@Validated
    ConsoleOnsTopicSubDetailRequest request) {
        request.setCurrentUser("");
        return OnsClientOperations.onsTopicSubDetail(request);
    }


    /**
     * 创建多环境GROUP
     *
     * @param request
     * @return
     */
    @PostMapping("/group")
    public static Map<String, OnsGroupCreateResponse> onsGroupCreate(@RequestBody @Validated
    ConsoleOnsGroupCreateRequest request) {
        request.setCurrentUser("");
        return OnsClientOperations.onsGroupCreate(request);
    }


    /**
     * 删除多环境GROUP
     *
     * @param request
     * @return
     */
    @DeleteMapping("/group")
    public static Map<String, OnsGroupDeleteResponse> onsGroupDelete(@RequestBody @Validated
    ConsoleOnsGroupDeleteRequest request) {
        request.setCurrentUser("");
        return OnsClientOperations.onsGroupDelete(request);
    }


    /**
     * 多环境获取Group_Id资源列表
     *
     * @param request
     * @return
     */
    @GetMapping("group")
    public static Map<String, List<ConsoleOnsGroupListResponse>> onsGroupList(@Validated
    ConsoleOnsGroupListRequest request) {
        request.setCurrentUser("");
        return OnsClientOperations.onsGroupList(request);
    }


    /**
     * 根据messageId查询死信队列
     *
     * @param request
     * @return
     */
    @GetMapping("DLQ/by_msg_id")
    public ConsoleOnsDLQMessagePageQueryByGroupResponse onsDLQMessageGetByIdRequest(@Validated
    ConsoleOnsDLQMessageGetByIdRequest request) {
        request.setCurrentUser("");
        return OnsClientOperations.onsDLQMessageGetByIdRequest(request);
    }

    /**
     * 根据GroupId下的死信队列消息
     *
     * @param request
     * @return
     */
    @GetMapping("DLQ/by_group_id")
    public PageResult<ConsoleOnsDLQMessagePageQueryByGroupResponse> onsDLQMessagePageQueryByGroupId(
            @Validated ConsoleOnsDLQMessagePageQueryByGroupIdRequest request) {
        request.setCurrentUser("");
        return OnsClientOperations.onsDLQMessagePageQueryByGroupId(request);
    }
}
