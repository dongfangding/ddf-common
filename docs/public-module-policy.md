# Public Module Policy

[English](./public-module-policy.md) | [中文](./public-module-policy.zh-CN.md)

This document defines which modules are intended to be part of the default public Maven Central publishing set.

## Public By Default

The following types of modules are suitable for public publishing by default:

- Shared API and core support modules
- Spring Boot starter modules
- Infrastructure integration modules with stable coordinates
- Reusable middleware integration modules

## Excluded By Default

These modules are currently excluded from the default Maven Central publishing set:

- `ddf-common-script`
- `ddf-common-netty-broker`

## Rationale

### `ddf-common-script`

- Primarily contains internal scripts and offline tooling resources
- Not a typical runtime dependency for consumers
- Better treated as repository tooling than as a public library artifact

### `ddf-common-netty-broker`

- More demo-oriented and scenario-specific than the rest of the published core modules
- Needs stronger API boundary definition before being treated as a stable public artifact

## Revisit Criteria

An excluded module can be moved into the public publishing set once:

- Its API surface is intentionally designed and documented
- It has a clear public use case
- It has at least minimal automated verification
- Its README reflects supported usage rather than internal experimentation
