@Library('global-jenkins-library@feature/checkout-after-node-setup') _

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

sconeBuildAllTee(
        buildInfo: buildInfo,
        nativeImage: 'nexus.iex.ec/' + repositoryName + ':' + buildInfo.shortCommit,
        targetImageRepositoryName: repositoryName,
        sconifyArgsPath: './docker/sconify.args')
