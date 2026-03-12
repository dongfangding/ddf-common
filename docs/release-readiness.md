# Release Readiness

[English](./release-readiness.md) | [中文](./release-readiness.zh-CN.md)

Last updated: 2026-03-12

## Summary

Current status for a first public Maven Central release:

- Candidate release version: `boot3.5-2026.1`
- Root release profile verification: passed
- Root compile verification: passed
- Maven Central metadata: prepared
- Sources / Javadocs / signing workflow: prepared
- Public module publishing boundary: documented
- README and major module READMEs: substantially aligned

## Auto-configuration Smoke Test Coverage

| Module | Auto Configuration | Test Files | Smoke Test |
| --- | --- | ---: | --- |
| ddf-common-alarm | Yes | 0 | - |
| ddf-common-api | Yes | 4 | - |
| ddf-common-authentication | Yes | 1 | AuthenticationAutoConfigurationTest |
| ddf-common-canal | No | 0 | - |
| ddf-common-captcha | Yes | 0 | - |
| ddf-common-core | Yes | 12 | - |
| ddf-common-data-mysql-starter | Yes | 1 | DataMysqlAutoConfigurationTest |
| ddf-common-dependency | No | 0 | - |
| ddf-common-distributed-lock | Yes | 0 | - |
| ddf-common-es | No | 0 | - |
| ddf-common-governance-starter | Yes | 1 | GovernanceAutoConfigurationTest |
| ddf-common-ids-service | Yes | 0 | - |
| ddf-common-limit | Yes | 0 | - |
| ddf-common-log4j | No | 0 | - |
| ddf-common-mongo | Yes | 1 | MongoAutoConfigurationTest |
| ddf-common-mqtt | Yes | 0 | - |
| ddf-common-mqtt-client | Yes | 1 | MqttClientAutoConfigurationTest |
| ddf-common-mvc | Yes | 0 | - |
| ddf-common-netty-broker | Yes | 0 | - |
| ddf-common-ons | Yes | 0 | - |
| ddf-common-redis | Yes | 1 | RedisCustomizeAutoConfigurationTest |
| ddf-common-rocketmq | Yes | 0 | - |
| ddf-common-s3 | Yes | 0 | - |
| ddf-common-script | No | 0 | - |
| ddf-common-sharding | Yes | 0 | - |
| ddf-common-starter-default | No | 0 | - |
| ddf-common-starter-web | No | 0 | - |
| ddf-common-third-party | Yes | 0 | - |
| ddf-common-vps | Yes | 0 | - |
| ddf-common-websocket | Yes | 0 | - |
| ddf-common-xxl-executor | Yes | 0 | - |
| ddf-common-zookeeper | Yes | 0 | - |

## Release Strengths

- The root README explains module layering, starters, release flow, and public or private publishing boundaries.
- Child module Maven metadata is consistent enough for repository index display.
- Several key auto-configuration modules already have minimal smoke tests:
  - authentication
  - data mysql starter
  - governance starter
  - mongo
  - mqtt-client
  - redis
- Non-public modules are explicitly excluded from default Central publishing:
  - `ddf-common-script`
  - `ddf-common-netty-broker`

## Remaining Gaps

- Many auto-configuration modules still have no smoke tests.
- `ddf-common-mvc` is not a good candidate for ultra-light `ApplicationContextRunner` testing and needs a fuller web slice strategy.
- `ddf-common-limit` currently exposes a lightweight auto-configuration entry but still relies on explicit enable annotations for actual feature activation.
- Some modules still need deeper API-level review before being treated as highly stable public contracts.

## Recommended Next Release Steps

1. Run `mvn -Prelease clean deploy` in the real publishing environment with credentials and GPG configured.
2. Create the Git tag for `boot3.5-2026.1` after successful publication.
3. Verify public coordinates and README rendering on Maven Central.
4. Switch the repository back to the next development `SNAPSHOT` version after the release branch or tag is finalized.
