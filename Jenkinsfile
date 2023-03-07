@Library('global-jenkins-library@2.5.0') _

String repositoryName = 'tee-worker-post-compute'

buildInfo = getBuildInfo()

buildJavaProject(
        buildInfo: buildInfo,
        integrationTestsEnvVars: [],
        shouldPublishJars: false,
        shouldPublishDockerImages: true,
        dockerfileDir: '.',
        buildContext: '.',
        dockerImageRepositoryName: repositoryName,
        preProductionVisibility: 'docker.io',
        productionVisibility: Registries.EXTERNAL_DOCKERIO_HOST
)

buildGramine(
    buildInfo: buildInfo,
    dockerfileDir: 'gramine'
)

sconeBuildUnlocked(
        nativeImage:     "docker-regis.iex.ec/$repositoryName:$buildInfo.imageTag",
        imageName:       repositoryName,
        imageTag:        buildInfo.imageTag,
        sconifyArgsPath: './docker/sconify.args',
        sconifyVersion:  '5.7.1'
)
