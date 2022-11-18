@Library('global-jenkins-library@2.2.0') _

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

buildSimpleDocker_v3(
        buildInfo: buildInfo,
        dockerfileDir: './gramine',
        buildContext: '.',
        dockerImageRepositoryName: 'gramine-tee-worker-post-compute',
        visibility: 'iex.ec'
)

sconeBuildUnlocked(
        nativeImage:     "docker-regis.iex.ec/$repositoryName:$buildInfo.imageTag",
        imageName:       repositoryName,
        imageTag:        buildInfo.imageTag,
        sconifyArgsPath: './docker/sconify.args',
        sconifyVersion:  '5.7.1'
)
