@Library('global-jenkins-library@feature/standalone-scone-build') _

String repositoryName = 'tee-worker-post-compute'

buildInfo = getBuildInfo()

buildJavaProject(
        buildInfo: buildInfo,
        integrationTestsEnvVars: [],
        shouldPublishJars: false,
        shouldPublishDockerImages: true,
        dockerfileDir: 'docker',
        buildContext: '.',
        dockerImageRepositoryName: repositoryName,
        preProductionVisibility: 'docker.io',
        productionVisibility: 'docker.io')

sconeBuildUnlocked(
        nativeImage:     "nexus.iex.ec/$repositoryName:$buildInfo.shortCommit",
        imageName:       repositoryName,
        imageTag:        buildInfo.imageTag,
        sconifyArgsPath: './docker/sconify.args')
