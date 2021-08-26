package com.ddf.common.ons.console.client;

import com.aliyun.ons20190214.Client;
import com.aliyun.ons20190214.models.OnsConsumerGetConnectionRequest;
import com.aliyun.ons20190214.models.OnsConsumerGetConnectionResponse;
import com.aliyun.ons20190214.models.OnsConsumerGetConnectionResponseBody;
import com.aliyun.ons20190214.models.OnsDLQMessageGetByIdResponse;
import com.aliyun.ons20190214.models.OnsDLQMessageGetByIdResponseBody;
import com.aliyun.ons20190214.models.OnsDLQMessagePageQueryByGroupIdResponse;
import com.aliyun.ons20190214.models.OnsDLQMessagePageQueryByGroupIdResponseBody;
import com.aliyun.ons20190214.models.OnsDLQMessageResendByIdRequest;
import com.aliyun.ons20190214.models.OnsDLQMessageResendByIdResponse;
import com.aliyun.ons20190214.models.OnsGroupCreateResponse;
import com.aliyun.ons20190214.models.OnsGroupDeleteResponse;
import com.aliyun.ons20190214.models.OnsGroupListResponse;
import com.aliyun.ons20190214.models.OnsMessageGetByMsgIdRequest;
import com.aliyun.ons20190214.models.OnsMessageGetByMsgIdResponse;
import com.aliyun.ons20190214.models.OnsMessagePushRequest;
import com.aliyun.ons20190214.models.OnsTopicCreateResponse;
import com.aliyun.ons20190214.models.OnsTopicDeleteResponse;
import com.aliyun.ons20190214.models.OnsTopicListResponse;
import com.ddf.boot.common.core.helper.SpringContextHolder;
import com.ddf.boot.common.core.model.PageResult;
import com.ddf.boot.common.core.util.JsonUtil;
import com.ddf.boot.common.core.util.PreconditionUtil;
import com.ddf.common.ons.console.config.EnvClientProperties;
import com.ddf.common.ons.console.constant.ConsoleConstants;
import com.ddf.common.ons.console.model.ConsoleOnsDLQMessageGetByIdRequest;
import com.ddf.common.ons.console.model.ConsoleOnsDLQMessagePageQueryByGroupIdRequest;
import com.ddf.common.ons.console.model.ConsoleOnsGroupCreateRequest;
import com.ddf.common.ons.console.model.ConsoleOnsGroupDeleteRequest;
import com.ddf.common.ons.console.model.ConsoleOnsGroupListRequest;
import com.ddf.common.ons.console.model.ConsoleOnsMessagePushRequest;
import com.ddf.common.ons.console.model.ConsoleOnsTopicCreateRequest;
import com.ddf.common.ons.console.model.ConsoleOnsTopicDeleteRequest;
import com.ddf.common.ons.console.model.ConsoleOnsTopicListRequest;
import com.ddf.common.ons.console.model.ConsoleOnsTopicSubDetailRequest;
import com.ddf.common.ons.console.model.EnvRequest;
import com.ddf.common.ons.console.model.UserRequest;
import com.ddf.common.ons.console.model.response.ConsoleOnsDLQMessagePageQueryByGroupResponse;
import com.ddf.common.ons.console.model.response.ConsoleOnsGroupListResponse;
import com.ddf.common.ons.console.model.response.ConsoleOnsTopicListResponse;
import com.ddf.common.ons.console.model.response.ConsoleOnsTopicSubListResponse;
import com.google.common.base.Preconditions;
import com.google.common.cache.Cache;
import com.google.common.cache.CacheBuilder;
import com.google.common.collect.Maps;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.concurrent.TimeUnit;
import java.util.function.BiFunction;
import java.util.stream.Collectors;
import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.util.CollectionUtils;

/**
 * <p>description</p >
 *
 * @author Snowball
 * @version 1.0
 * @date 2021/05/17 15:49
 */
@Slf4j
public class OnsClientOperations {

    /**
     * 不同环境对应的客户端集合
     */
    private static final Map<String, Client> ENV_CLIENT_MAP;
    /**
     * 当前环境
     */
    private static String CURRENT_ENV = System.getProperty("env");
    /**
     * 当前环境对应的客户端
     */
    private static final Client CURRENT_ENV_CLIENT;

    /**
     * 多环境配置信息
     */
    private static final EnvClientProperties ENV_CLIENT_PROPERTIES;

    /**
     * 当前环境对应的客户端配置信息
     */
    private static final EnvClientProperties.ClientProperties CURRENT_CLIENT_PROPERTIES;

    /**
     * 当前环境对应的ONS的InstanceId， 取自当前环境对应的客户端配置信息
     */
    private static final String CURRENT_ENV_INSTANCE_ID;

    /**
     * ons消费者连接信息缓存， 主要用在短时间内多次向指定消费者推送消息时，避免多次调用api， 失效时间很短，因为主要是用在循环中调用。
     * key   InstanceId-GroupId
     * value ClientId集合
     */
    private static final Cache<String, List<String>> ONS_CONSUMER_CONNECTIONS_MAP = CacheBuilder.newBuilder()
            .expireAfterWrite(10, TimeUnit.SECONDS)
            .softValues()
            .build();

    /*
      初始化属性
     */
    static {
        PreconditionUtil.checkArgument(Objects.nonNull(CURRENT_ENV), new IllegalArgumentException("当前环境信息获取失败......"));
        CURRENT_ENV = CURRENT_ENV.toUpperCase();
        ENV_CLIENT_MAP = SpringContextHolder.getBeansOfType(Client.class);
        Preconditions.checkArgument(!CollectionUtils.isEmpty(ENV_CLIENT_MAP), new OnsClientExecuteException("多环境ONS客户端初始化失败"));

        CURRENT_ENV_CLIENT = ENV_CLIENT_MAP.get(ConsoleConstants.getOnsClientBeanName(CURRENT_ENV));
        Preconditions.checkArgument(Objects.nonNull(CURRENT_ENV_CLIENT), new OnsClientExecuteException("当前环境ONS客户端初始化失败【" + CURRENT_ENV + "】"));

        ENV_CLIENT_PROPERTIES = SpringContextHolder.getBean(EnvClientProperties.class);
        Preconditions.checkArgument(Objects.nonNull(ENV_CLIENT_PROPERTIES), new OnsClientExecuteException("获取环境配置信息失败"));

        CURRENT_CLIENT_PROPERTIES = ENV_CLIENT_PROPERTIES.getClients().get(CURRENT_ENV);
        CURRENT_ENV_INSTANCE_ID = CURRENT_CLIENT_PROPERTIES.getInstanceId();
    }

    /**
     * 创建多环境topic
     *
     * @param request
     * @return
     */
    public static Map<String, OnsTopicCreateResponse> onsTopicCreate(ConsoleOnsTopicCreateRequest request) {
        checkCurrentUser(request);
        return envAction(1, request, (client, env) -> {
            try {
                return client.onsTopicCreate(request.toSdkRequest(ENV_CLIENT_PROPERTIES.getClients().get(env).getInstanceId()));
            } catch (Exception e) {
                log.error("Topic创建失败， Topic: {}, env: {}", request.getTopic(), env, e);
                throw OnsClientExecuteException.convertTeaException(e, env);
            }
        });
    }


    /**
     * 多环境删除Topic
     *
     * @param request
     * @return
     */
    public static Map<String, OnsTopicDeleteResponse> onsTopicDelete(ConsoleOnsTopicDeleteRequest request) {
        checkCurrentUser(request);
        checkSystemTopic(request.getTopic());
        return envAction(1, request, (client, env) -> {
            try {
                return client.onsTopicDelete(request.toSdkRequest(ENV_CLIENT_PROPERTIES.getClients().get(env).getInstanceId()));
            } catch (Exception e) {
                log.error("Topic删除失败， Topic: {}, env: {}", request.getTopic(), env, e);
                throw OnsClientExecuteException.convertTeaException(e, env);
            }
        });
    }


    /**
     * 多环境创建GROUP_ID
     *
     * @param request
     * @return
     */
    public static Map<String, OnsGroupCreateResponse> onsGroupCreate(ConsoleOnsGroupCreateRequest request) {
        checkCurrentUser(request);
        return envAction(1, request, (client, env) -> {
            try {
                return client.onsGroupCreate(request.toSdkRequest(ENV_CLIENT_PROPERTIES.getClients().get(env).getInstanceId()));
            } catch (Exception e) {
                log.error("Group创建失败， GroupId: {}, env: {}", request.getGroupId(), env, e);
                throw OnsClientExecuteException.convertTeaException(e, env);
            }
        });
    }

    /**
     * 多环境删除GROUP_ID
     *
     * @param request
     * @return
     */
    public static Map<String, OnsGroupDeleteResponse> onsGroupDelete(ConsoleOnsGroupDeleteRequest request) {
        checkCurrentUser(request);
        checkSystemGroup(request.getGroupId());
        return envAction(1, request, (client, env) -> {
            try {
                return client.onsGroupDelete(request.toSdkRequest(ENV_CLIENT_PROPERTIES.getClients().get(env).getInstanceId()));
            } catch (Exception e) {
                log.error("Group删除失败， GroupId: {}, env: {}", request.getGroupId(), env, e);
                throw OnsClientExecuteException.convertTeaException(e, env);
            }
        });
    }


    /**
     * 执行多环境操作模板
     *
     * @param qps
     * @param request
     * @param function
     * @param <R>
     * @return
     */
    @SneakyThrows
    private static <R> Map<String, R> envAction(int qps, EnvRequest request, BiFunction<Client, String, R> function) {
        PreconditionUtil.requiredParamCheck(request);
        Set<String> envList = request.getEnvList();
        if (CollectionUtils.isEmpty(envList)) {
            envList = ENV_CLIENT_PROPERTIES.getClients().keySet();
        }
        Map<String, R> returnMap = Maps.newHashMapWithExpectedSize(envList.size());
        int lastLoop = 0;
        for (String env : envList) {
            lastLoop ++;
            final Client client = ENV_CLIENT_MAP.get(ConsoleConstants.getOnsClientBeanName(env));
            if (Objects.isNull(client)) {
                log.warn("[{}]未找到对应的客户端环境配置， 无法执行，跳过处理>>>>", env);
                continue;
            }
            returnMap.put(env, function.apply(client, env));
            // 阿里云OPENAPI QPS限制， 限流规则
            if (lastLoop < envList.size() && qps != 0) {
                Thread.sleep(1000 / qps + 1);
            }
        }
        return returnMap;
    }

    /**
     * 查询消息记录
     *
     * @param topicId
     * @param msgId
     * @return
     */
    public static OnsMessageGetByMsgIdResponse onsMessageGetByMsgId(String topicId, String msgId) {
        OnsMessageGetByMsgIdRequest onsMessageGetByMsgIdRequest = new OnsMessageGetByMsgIdRequest()
                .setMsgId(msgId)
                .setTopic(topicId)
                .setInstanceId(CURRENT_ENV_INSTANCE_ID);
        try {
            OnsMessageGetByMsgIdResponse response = CURRENT_ENV_CLIENT.onsMessageGetByMsgId(onsMessageGetByMsgIdRequest);
            log.info("查询到消息记录>>>>>>>:{}", JsonUtil.asString(response.getBody().getData()));
            return response;
        } catch (Exception e) {
            log.error("查询消息失败>>>>> topicId: {}, msgId: {}", topicId, msgId, e);
            throw OnsClientExecuteException.convertTeaException(e);
        }
    }

    /**
     * 获取指定GroupId和InstanceId下的客户端连接
     *
     * @param instanceId
     * @param groupId
     * @return
     */
    public static OnsConsumerGetConnectionResponse onsConsumerGetConnection(String instanceId, String groupId) {
        try {
            final OnsConsumerGetConnectionRequest request = new OnsConsumerGetConnectionRequest()
                    .setInstanceId(instanceId)
                    .setGroupId(groupId);
            return CURRENT_ENV_CLIENT.onsConsumerGetConnection(request);
        } catch (Exception e) {
            log.error("获取GroupId下消费者连接失败，GroupId: {}, InstanceId: {}", instanceId, groupId, e);
            throw OnsClientExecuteException.convertTeaException(e);
        }
    }

    /**
     * 从缓存中获取ONS消费者连接信息ClientId集合
     *
     * @param groupId
     * @return
     */
    public static List<String> getOnsConsumerClientIdListFromCache(String groupId) {
        final List<String> clientIdList = ONS_CONSUMER_CONNECTIONS_MAP.getIfPresent(
                getOnsConsumerConnectionsKey(CURRENT_ENV_INSTANCE_ID, groupId));
        if (!CollectionUtils.isEmpty(clientIdList)) {
            return clientIdList;
        }
        putOnsConsumerConnections(CURRENT_ENV_INSTANCE_ID, groupId, onsConsumerGetConnection(CURRENT_ENV_INSTANCE_ID, groupId));
        return ONS_CONSUMER_CONNECTIONS_MAP.getIfPresent(
                getOnsConsumerConnectionsKey(CURRENT_ENV_INSTANCE_ID, groupId));
    }

    /**
     * 向指定的消费者推送消息, MsgId会重新生成， 但是用重新生成的MsgId获取消息记录，指向的还是之前的MsgId
     *
     * @throws Exception
     */
    public static void onsMessagePush(ConsoleOnsMessagePushRequest request) {
        checkCurrentUser(request);
        final List<String> cacheClientIdList = getOnsConsumerClientIdListFromCache(request.getGroupId());
        if (CollectionUtils.isEmpty(cacheClientIdList)) {
            throw new OnsClientExecuteException(
                    String.format("无法获取消费者ClientId, 暂时不可推送！InstanceId: %s, GroupId: %s", CURRENT_ENV_INSTANCE_ID,
                            request.getGroupId()
                    ));
        }
        onsMessageGetByMsgId(request.getTopic(), request.getMsgId());
        // 暂时不考虑广播模式
        final OnsMessagePushRequest onsMessagePushRequest = request.toSdkRequest(cacheClientIdList.get(0), CURRENT_ENV_INSTANCE_ID);
        try {
            CURRENT_ENV_CLIENT.onsMessagePush(onsMessagePushRequest);
        } catch (Exception e) {
            log.error("消息推送失败！MsdId = {}, InstanceId: {}, GroupId: {}", request.getMsgId(), CURRENT_ENV_INSTANCE_ID,
                    request.getGroupId(), e);
            throw OnsClientExecuteException.convertTeaException(e);
        }
    }

    /**
     * 重发指定MessageId的死信消息, 如果消息未到达最大重试次数，即消息未进入死信，则该方法会出现异常
     *
     * @param groupId
     * @param msgId
     */
    public static OnsDLQMessageResendByIdResponse onsDLQMessageResendByIdRequest(String groupId, String msgId) {
        OnsDLQMessageResendByIdRequest onsDLQMessageResendByIdRequest = new OnsDLQMessageResendByIdRequest()
                .setMsgId(msgId)
                .setGroupId(groupId)
                .setInstanceId(CURRENT_ENV_INSTANCE_ID);
        try {
            return CURRENT_ENV_CLIENT.onsDLQMessageResendById(onsDLQMessageResendByIdRequest);
        } catch (Exception e) {
            log.error("重发死信消息失败， InstanceId: {}, GroupId: {}, MsgId: {}", CURRENT_ENV_INSTANCE_ID, groupId, msgId, e);
            throw OnsClientExecuteException.convertTeaException(e);
        }
    }

    /**
     * 根据MsgId查询死信消息
     *
     * @param request
     * @return
     */
    public static ConsoleOnsDLQMessagePageQueryByGroupResponse onsDLQMessageGetByIdRequest(
            ConsoleOnsDLQMessageGetByIdRequest request) {
        checkCurrentUser(request);
        final String groupId = request.getGroupId();
        final String msgId = request.getMessageId();
        try {
            final OnsDLQMessageGetByIdResponse response = CURRENT_ENV_CLIENT.onsDLQMessageGetById(
                    request.toSdkRequest(CURRENT_ENV_INSTANCE_ID));
            if (Objects.nonNull(response) && Objects.nonNull(response.getBody()) && Objects.nonNull(response.getBody().getData())) {
                final OnsDLQMessageGetByIdResponseBody.OnsDLQMessageGetByIdResponseBodyData data = response.getBody()
                        .getData();
                final Map<String, String> propertiesMap = data.getPropertyList()
                        .getMessageProperty()
                        .stream()
                        .collect(Collectors.toMap(
                                OnsDLQMessageGetByIdResponseBody.OnsDLQMessageGetByIdResponseBodyDataPropertyListMessageProperty::getName,
                                OnsDLQMessageGetByIdResponseBody.OnsDLQMessageGetByIdResponseBodyDataPropertyListMessageProperty::getValue
                        ));
                return ConsoleOnsDLQMessagePageQueryByGroupResponse.builder()
                        .storeSize(data.getStoreSize())
                        .reconsumeTimes(data.getReconsumeTimes())
                        .storeTimestamp(data.getStoreTimestamp())
                        .instanceId(data.getInstanceId())
                        .msgId(data.getMsgId())
                        .storeHost(data.getStoreHost())
                        .topic(data.getTopic())
                        .bornTimestamp(data.getBornTimestamp())
                        .bodyCRC(data.getBodyCRC())
                        .bornHost(data.getBornHost())
                        .realTopic(propertiesMap.get("REAL_TOPIC"))
                        .originMessageId(propertiesMap.get("ORIGIN_MESSAGE_ID"))
                        .keys(propertiesMap.get("KEYS"))
                        .tags(propertiesMap.get("TAGS"))
                        .build();
            }
        } catch (Exception e) {
            log.error("查询死信消息失败， InstanceId: {}, GroupId: {}, MsgId: {}", CURRENT_ENV_INSTANCE_ID, groupId, msgId, e);
            throw OnsClientExecuteException.convertTeaException(e);
        }
        return null;
    }


    /**
     * 查询GroupId下所有死信消息
     *
     * @param request
     * @return
     */
    public static PageResult<ConsoleOnsDLQMessagePageQueryByGroupResponse> onsDLQMessagePageQueryByGroupId(
            ConsoleOnsDLQMessagePageQueryByGroupIdRequest request) {
        checkCurrentUser(request);
        try {
            final OnsDLQMessagePageQueryByGroupIdResponse response = CURRENT_ENV_CLIENT.onsDLQMessagePageQueryByGroupId(
                    request.toSdkRequest(CURRENT_ENV_INSTANCE_ID));
            final List<OnsDLQMessagePageQueryByGroupIdResponseBody.OnsDLQMessagePageQueryByGroupIdResponseBodyMsgFoundDoMsgFoundListOnsRestMessageDo>
                    list = response.getBody()
                    .getMsgFoundDo()
                    .getMsgFoundList()
                    .getOnsRestMessageDo();
            if (CollectionUtils.isEmpty(list)) {
                return new PageResult<>(request.getCurrentPage(), request.getPageSize(), 0);
            }
            final List<ConsoleOnsDLQMessagePageQueryByGroupResponse> content = list.stream()
                    .map(val -> {
                        final Map<String, String> propertiesMap = val.getPropertyList()
                                .getMessageProperty()
                                .stream()
                                .collect(Collectors.toMap(
                                        OnsDLQMessagePageQueryByGroupIdResponseBody.OnsDLQMessagePageQueryByGroupIdResponseBodyMsgFoundDoMsgFoundListOnsRestMessageDoPropertyListMessageProperty::getName,
                                        OnsDLQMessagePageQueryByGroupIdResponseBody.OnsDLQMessagePageQueryByGroupIdResponseBodyMsgFoundDoMsgFoundListOnsRestMessageDoPropertyListMessageProperty::getValue
                                ));
                        return ConsoleOnsDLQMessagePageQueryByGroupResponse.builder()
                                .storeSize(val.getStoreSize())
                                .reconsumeTimes(val.getReconsumeTimes())
                                .storeTimestamp(val.getStoreTimestamp())
                                .instanceId(val.getInstanceId())
                                .msgId(val.getMsgId())
                                .storeHost(val.getStoreHost())
                                .topic(val.getTopic())
                                .bornTimestamp(val.getBornTimestamp())
                                .bodyCRC(val.getBodyCRC())
                                .bornHost(val.getBornHost())
                                .realTopic(propertiesMap.get("REAL_TOPIC"))
                                .originMessageId(propertiesMap.get("ORIGIN_MESSAGE_ID"))
                                .keys(propertiesMap.get("KEYS"))
                                .tags(propertiesMap.get("TAGS"))
                                .build();
                    })
                    .collect(Collectors.toList());
            // 这个总数是假的，没返回这里也没法知道，这里给最大值是方便页数计算
            return new PageResult<>(request.getCurrentPage(), request.getPageSize(), response.getBody()
                    .getMsgFoundDo()
                    .getMaxPageCount() * request.getPageSize(), content);
        } catch (Exception e) {
            log.error(
                    "查询GroupId下所有死信消息失败， InstanceId: {}, GroupId: {}, json: {}", CURRENT_ENV_INSTANCE_ID,
                    request.getGroupId(), JsonUtil.asString(request), e
            );
            throw OnsClientExecuteException.convertTeaException(e);
        }
    }


    /**
     * 多环境查询账号下所有 Topic 的信息列表
     *
     * @param request
     * @return
     */
    public static Map<String, List<ConsoleOnsTopicListResponse>> onsTopicList(ConsoleOnsTopicListRequest request) {
        checkCurrentUser(request);
        String env = StringUtils.defaultIfBlank(request.getEnv(), CURRENT_ENV);
        final Client client = ENV_CLIENT_MAP.get(ConsoleConstants.getOnsClientBeanName(env));
        Preconditions.checkArgument(Objects.nonNull(client), new IllegalArgumentException("env不存在"));
        final Map<String, List<ConsoleOnsTopicListResponse>> map = initEnvResponseMap();
        try {
            final OnsTopicListResponse response = client.onsTopicList(
                    request.toSdkRequest(ENV_CLIENT_PROPERTIES.getClients().get(env).getInstanceId()));
            map.put(env, ConsoleOnsTopicListResponse.convertFromSdk(response));
            return map;
        } catch (Exception e) {
            log.error("获取Topic列表失败, env: {}, request: {}", env, request);
            throw OnsClientExecuteException.convertTeaException(e, env);
        }
    }

    /**
     * 多环境获取Group_Id资源列表
     *
     * @param request
     * @return
     */
    public static Map<String, List<ConsoleOnsGroupListResponse>> onsGroupList(ConsoleOnsGroupListRequest request) {
        checkCurrentUser(request);
        String env = StringUtils.defaultIfBlank(request.getEnv(), CURRENT_ENV);
        final Client client = ENV_CLIENT_MAP.get(ConsoleConstants.getOnsClientBeanName(env));
        Preconditions.checkArgument(Objects.nonNull(client), new IllegalArgumentException("env不存在"));
        final Map<String, List<ConsoleOnsGroupListResponse>> map = initEnvResponseMap();
        try {
            final OnsGroupListResponse response = client.onsGroupList(
                    request.toSdkRequest(ENV_CLIENT_PROPERTIES.getClients().get(env).getInstanceId()));
            map.put(env, ConsoleOnsGroupListResponse.convertFromSdk(response));
            return map;
        } catch (Exception e) {
            log.error("获取Group列表失败, env: {}, request: {}", env, request);
            throw OnsClientExecuteException.convertTeaException(e, env);
        }
    }


    /**
     * 查看Topic的在线订阅组
     *
     * @param request
     * @return
     */
    public static List<ConsoleOnsTopicSubListResponse> onsTopicSubDetail(ConsoleOnsTopicSubDetailRequest request) {
        checkCurrentUser(request);
        String env = StringUtils.defaultIfBlank(request.getEnv(), CURRENT_ENV);
        final Client client = ENV_CLIENT_MAP.get(ConsoleConstants.getOnsClientBeanName(env));
        Preconditions.checkArgument(Objects.nonNull(client), new IllegalArgumentException("env不存在"));
        try {
            return ConsoleOnsTopicSubListResponse.convertFromSdk(client.onsTopicSubDetail(request.toSdkRequest(
                    ENV_CLIENT_PROPERTIES.getClients().get(env).getInstanceId())));
        } catch (Exception e) {
            log.error("查看Topic的在线订阅组失败>>>", e);
            throw OnsClientExecuteException.convertTeaException(e, env);
        }
    }

    /**
     * 获取当前配置的ONS环境列表
     *
     * @return
     */
    public static Set<String> getOnsEnvKeySet() {
        return ENV_CLIENT_PROPERTIES.getClients().keySet();
    }


    /**
     * 初始化多环境请求响应Map
     *
     * @return
     */
    public static <T> Map<String, T> initEnvResponseMap() {
        final Set<String> set = getOnsEnvKeySet();
        Map<String, T> map = Maps.newHashMapWithExpectedSize(set.size());
        set.forEach(env -> map.put(env, null));
        return map;
    }

    /**
     * ons消费客户端连接信息缓存key
     *
     * @param instanceId
     * @param groupId
     * @return
     */
    public static String getOnsConsumerConnectionsKey(String instanceId, String groupId) {
        return instanceId + "-" + groupId;
    }

    /**
     * 放入ons消费客户端连接信息缓存, 仅需要ClientId
     *
     * @param instanceId
     * @param groupId
     */
    private static void putOnsConsumerConnections(String instanceId, String groupId,
            OnsConsumerGetConnectionResponse response) {
        ONS_CONSUMER_CONNECTIONS_MAP.put(getOnsConsumerConnectionsKey(instanceId, groupId), response.getBody()
                .getData()
                .getConnectionList()
                .getConnectionDo()
                .stream()
                .map(OnsConsumerGetConnectionResponseBody.OnsConsumerGetConnectionResponseBodyDataConnectionListConnectionDo::getClientId)
                .collect(Collectors.toList()));
    }


    /**
     * 校验用户名
     *
     * @param userRequest
     * @return
     */
    private static void checkCurrentUser(UserRequest userRequest) {
        final String user = userRequest.getCurrentUser();
        boolean bool = StringUtils.isNotBlank(user) && Objects.nonNull(ENV_CLIENT_PROPERTIES.getAdminUserName())
                && ENV_CLIENT_PROPERTIES.getAdminUserName()
                .stream()
                .anyMatch(name -> name.equals(user));
        if (!bool) {
            throw new OnsClientExecuteException("当前用户非ONS控台管理员，无法操作！");
        }
    }

    /**
     * 系统Topic不可操作
     *
     * @param topic
     */
    private static void checkSystemTopic(String topic) {
        boolean isSystemTopic = !CollectionUtils.isEmpty(ENV_CLIENT_PROPERTIES.getSystemTopic()) && ENV_CLIENT_PROPERTIES.getSystemTopic().contains(topic);
        if (isSystemTopic) {
            throw new OnsClientExecuteException("当前Topic[" + topic + "]为系统Topic, 不可操作!");
        }
    }


    /**
     * 系统Group不可操作
     *
     * @param group
     */
    private static void checkSystemGroup(String group) {
        boolean isSystemGroup= !CollectionUtils.isEmpty(ENV_CLIENT_PROPERTIES.getSystemGroup()) && ENV_CLIENT_PROPERTIES.getSystemGroup().contains(group);
        if (isSystemGroup) {
            throw new OnsClientExecuteException("当前Group[" + group + "]为系统Group, 不可操作!");
        }
    }
}
