# Releasing To Maven Central

[English](./releasing-to-maven-central.md) | [中文](./releasing-to-maven-central.zh-CN.md)

This project is prepared for publishing through Sonatype Central Publisher Portal.

## Prerequisites

- Java 17
- Maven 3.9.6 or later
- A Sonatype Central account with permission for `io.github.dongfangding`
- GPG installed and configured for artifact signing
- `settings.xml` configured with the publishing server credentials referenced by the root `pom.xml`

## Local Verification

Run a compile check before release:

```bash
mvn -q -DskipTests compile
```

If you want a fuller verification pass:

```bash
mvn clean verify
```

## Release Build

Use the `release` profile to attach sources, Javadocs, signatures, and invoke the central publishing plugin:

```bash
mvn -Prelease clean deploy
```

Current first public release candidate:

- `boot3.5-2026.1`

## GitHub Actions Secrets

If you use the repository workflow `.github/workflows/release-central.yml`, define these repository secrets:

- `MAVEN_USERNAME`
- `MAVEN_PASSWORD`
- `MAVEN_GPG_PRIVATE_KEY`
- `MAVEN_GPG_PASSPHRASE`

## Notes

- The root POM already declares the required project metadata for Maven Central.
- Child modules should keep clear `name` and `description` values because they are visible in repository indexes.
- `ddf-common-script` is more suitable as an internal utility module than a public runtime dependency.
- `ddf-common-netty-broker` is currently treated as a demo or specialized module and is excluded from the default Central publishing set.
- If publishing credentials or GPG setup changes, update local `settings.xml` rather than committing secrets into the repository.
