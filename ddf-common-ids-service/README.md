核心代码来自美团点评的leaf, https://github.com/Meituan-Dianping/Leaf

由于在maven仓库中无法直接下载依赖以及对其中依赖进行了升级和一些代码的整合， 所以将大部分代码抄录了过来。


# 使用
自动配置类`com.ddf.common.ids.service.config.IdsServiceAutoConfiguration`

可以直接作为依赖，添加到具体服务中，也可以使用单独的服务对外提供，默认使用当前服务的数据源注入， 如果要自定义其它数据源
就重新注入`com.ddf.common.ids.service.service.impl.segment.dao.IDAllocDao`

无论是雪花id还是号段模式，对外统一暴露请使用api
`com.ddf.common.ids.service.api.IdsApi`

## 雪花id
使用雪花ID， 需要配置zookeeper连接地址, 配置类对应
`com.ddf.common.ids.service.config.properties.IdsProperties`

```yaml
customs:
  ids:
    name: ids_demo # 雪花id名称，会作为zk和本地目录存储workId路径中的一部分
    beginTimestamp: 1609430400000 # 相对的一个起始时间戳，能够用来混淆生产出来的雪花ID的时间戳部分的数据
    zkAddress: ${zk_addr} # zookeeper连接地址
    zkPort: 2181 # 端口，注意不是zookeeper的连接端口，是客户端上报数据时的节点组成的一部分，使用ip+port, 可以解决同一台机器多个服务问题
```

## 号段模式
配置类首先开启号段模式
```yaml
customs:
  ids:
    segmentEnable: true
```

号段模式建表语句

与leaf相比，增加了对id固定长度的处理，如果位数不足，会去做填充

```sql
CREATE TABLE `leaf_alloc` (
`biz_tag` varchar(128)  NOT NULL comment '业务tag',
`max_id` bigint(20) NOT NULL DEFAULT '1' comment '起始id',
`step` int(11) NOT NULL comment '步长， 即每次本地没有缓存或缓存用完时下一个阶段的id起始段, 对于频繁重启对应用，步长太大，会造成ID大量浪费',
`fill_length` int(11) NOT NULL DEFAULT 0 comment '期望的id的长度， 如果id长度小于这个长度，则会返回填充， 为0时不处理',
`description` varchar(256)  DEFAULT NULL,
`update_time` timestamp NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
PRIMARY KEY (`biz_tag`)
) ENGINE=InnoDB;

insert into leaf_alloc(biz_tag, max_id, step, fill_length, description) values('IDS', 1, 2000, 8, '测试号段模式')
```


