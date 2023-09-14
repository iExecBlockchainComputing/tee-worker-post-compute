# Changelog

All notable changes to this project will be documented in this file.

## [[NEXT]](https://github.com/iExecBlockchainComputing/tee-worker-post-compute/releases/tag/vNEXT) 2023

### New Features
- Check result files name length before zipping. (#83)
### Quality
- Remove `nexus.intra.iex.ec` repository. (#84)
- Parameterize build of TEE applications while PR is not started. This allows faster builds. (#85 #86)
- Update `sconify.sh` script and rename `buildTeeImage` task to `buildSconeImage`. (#87)
- Upgrade to Gradle 8.2.1 with up-to-date plugins. (#89)
### Dependency Upgrades
- Upgrade to `iexec-common` 8.2.1-NEXT-SNAPSHOT. (#83)
- Upgrade to `jenkins-library` 2.7.3. (#85 #91)
- Upgrade to `eclipse-temurin` 11.0.20. (#88)
- Upgrade to `testcontainers` 1.19.0. (#90)

## [[8.1.1]](https://github.com/iExecBlockchainComputing/tee-worker-post-compute/releases/tag/v8.1.1) 2023-06-23

### Dependency Upgrades
- Upgrade to `iexec-common` 8.2.1. (#81)
- Upgrade to `iexec-commons-poco` 3.0.4. (#81)

## [[8.1.0]](https://github.com/iExecBlockchainComputing/tee-worker-post-compute/releases/tag/v8.1.0) 2023-06-06

### Bug Fixes
- Run IT on native code instead of already-built image. (#71)
### Dependency Upgrades
- Remove `log4j-slf4j-impl` dependency. (#76)
- Remove `lombok` dependencies provided by lombok gradle plugin. (#76)
- Upgrade to `iexec-common` 8.2.0. (#77 #78)
- Add new `iexec-commons-poco` 3.0.0 dependency. (#77 #78)

## [[8.0.0]](https://github.com/iExecBlockchainComputing/tee-worker-post-compute/releases/tag/v8.0.0) 2023-03-08

### New Features
* #69 #68 #63 #61 #54 Enable Gramine framework for TEE tasks.
* #60 Set scone heap to 3G.
* #59 Upgrade to scone 5.7.
### Quality
* #67 Remove deprecated Palantir Docker Gradle plugin.
### Dependency Upgrades
* #73 Upgrade to `iexec-common` 7.0.0.
* #72 #65 #64 #62 #58 Upgrade to `jenkins-library` 2.5.0.
* #70 Replace the deprecated `openjdk` Docker base image with `eclipse-temurin` and upgrade to Java 11.0.16 patch.
* #66 Upgrade to Gradle 7.6.
