核心代码来自美团点评的leaf, https://github.com/Meituan-Dianping/Leaf

由于在maven仓库中无法直接下载依赖以及对其中依赖进行了升级和一些代码的整合， 所以将大部分代码抄录了过来。


# 使用
自动配置类`com.ddf.common.ids.service.config.IdsServiceAutoConfiguration`


使用雪花ID， 需要配置zookeeper连接地址, 配置类对应
`com.ddf.common.ids.service.config.properties.SnowflakeProperties`
```yaml
customs:
  ids:
    snowflake:
      zkAddress: ${zk_addr}
      zkPort: 2181
```


号段模式建表语句

```sql
CREATE DATABASE leaf
CREATE TABLE `leaf_alloc` (
`biz_tag` varchar(128)  NOT NULL DEFAULT '',
`max_id` bigint(20) NOT NULL DEFAULT '1',
`step` int(11) NOT NULL,
`description` varchar(256)  DEFAULT NULL,
`update_time` timestamp NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
PRIMARY KEY (`biz_tag`)
) ENGINE=InnoDB;

insert into leaf_alloc(biz_tag, max_id, step, description) values('leaf-segment-test', 1, 2000, 'Test leaf Segment Mode Get Id')
```

无论是雪花id还是号段模式，对外统一暴露请使用api
`com.ddf.common.ids.service.api.IdsApi`
