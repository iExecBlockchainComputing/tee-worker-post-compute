# Changelog

All notable changes to this project will be documented in this file.

## [[NEXT]](https://github.com/iExecBlockchainComputing/tee-worker-post-compute/releases/tag/vNEXT) 2024

### New Features

- Replace RestTemplate with Feign Client for Result Proxy Upload. (#120)

### Deprecated

- Stop building Gramine TEE image in Jenkins Pipeline. (#118)

### Dependency Upgrades

- Upgrade to `eclipse-temurin:11.0.24_8-jre-focal`. (#116)
- Upgrade to Gradle 8.10.2. (#117)
- Upgrade to `testcontainers` 1.20.4. (#119)

## [[8.5.0]](https://github.com/iExecBlockchainComputing/tee-worker-post-compute/releases/tag/v8.5.0) 2024-06-18

### Bug Fixes

- Handle malformed result encryption public key. (#112)

### Quality

- Configure Gradle JVM Test Suite Plugin. (#109)

### Dependency Upgrades

- Upgrade to Gradle 8.7. (#110)
- Upgrade to `eclipse-temurin:11.0.22_7-jre-focal`. (#111)
- Upgrade to `iexec-commons-poco` 4.1.0. (#113)
- Upgrade to `iexec-common` 8.5.0. (#113)

## [[8.4.0]](https://github.com/iExecBlockchainComputing/tee-worker-post-compute/releases/tag/v8.4.0) 2024-02-29

### New Features

- Upload results on IPFS with a `ResultModel` containing the `enclaveSignature`. (#105)

### Quality

- Rename `worflow` package to `workflow`. (#102)
- Rework classes to use `ComputedFile` in methods parameters. (#103)

### Dependency Upgrades

- Upgrade to scone 5.7.6. (#104)
- Upgrade to `iexec-common` 8.4.0. (#106)

## [[8.3.0]](https://github.com/iExecBlockchainComputing/tee-worker-post-compute/releases/tag/v8.3.0) 2024-01-12

### Dependency Upgrade

- Upgrade to `eclipse-temurin:11.0.21_9-jre-focal`. (#98)
- Upgrade to `jenkins-library` 2.7.4. (#96 #97)
- Upgrade to `iexec-commons-poco` 3.2.0. (#99)
- Upgrade to `iexec-common` 8.3.1. (#99)

## [[8.2.0]](https://github.com/iExecBlockchainComputing/tee-worker-post-compute/releases/tag/v8.2.0) 2023-09-28

### New Features

- Check result files name length before zipping. (#83)

### Quality

- Remove `nexus.intra.iex.ec` repository. (#84)
- Parameterize build of TEE applications while PR is not started. This allows faster builds. (#85 #86)
- Update `sconify.sh` script and rename `buildTeeImage` task to `buildSconeImage`. (#87)
- Upgrade to Gradle 8.2.1 with up-to-date plugins. (#89)
- Rename base package to `com.iexec.worker.compute.post`. (#92)
- Rename worker REST api package to `com.iexec.worker.api`. (#92)

### Dependency Upgrades

- Upgrade to `iexec-common` 8.2.1-NEXT-SNAPSHOT. (#83)
- Upgrade to `jenkins-library` 2.7.3. (#85 #91)
- Upgrade to `eclipse-temurin` 11.0.20. (#88)
- Upgrade to `testcontainers` 1.19.0. (#90)
- Upgrade to `iexec-commons-poco` 3.1.0. (#93)
- Upgrade to `iexec-common` 8.3.0. (#93)

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

- Enable Gramine framework for TEE tasks. (#54 #61 #63 #68 #69)
- Set scone heap to 3G. (#60)
- Upgrade to scone 5.7. (#59)

### Quality

- Remove deprecated Palantir Docker Gradle plugin. (#67)

### Dependency Upgrades

- Upgrade to `iexec-common` 7.0.0. (#73)
- Upgrade to `jenkins-library` 2.5.0. (#58 #62 #64 #65 #72)
- Replace the deprecated `openjdk` Docker base image with `eclipse-temurin` and upgrade to Java 11.0.16 patch. (#70)
- Upgrade to Gradle 7.6. (#66)
