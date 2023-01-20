@Library('global-jenkins-library@2.3.1') _

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

stage('Build Gramine') {
    gramineBuildInfo = buildInfo.clone()
    dockerfileDir = './gramine'
    gramineBuildInfo.imageTag += '-gramine'
    visibility = Registries.EXTERNAL_IEXEC_HOST
    productionImageName = ''
    stage('Build Gramine production image') {
        productionImageName = buildSimpleDocker_v3(
            buildInfo: gramineBuildInfo,
            dockerfileDir: dockerfileDir,
            buildContext: '.',
            dockerImageRepositoryName: repositoryName,
            visibility: visibility
        )
    }
    stage('Build Gramine test CA image') {
        testCaSuffix = 'test-ca'
        gramineBuildInfo.imageTag += '-' + testCaSuffix
        buildSimpleDocker_v3(
            buildInfo: gramineBuildInfo,
            dockerfileDir: dockerfileDir,
            dockerfileFilename: 'Dockerfile.' + testCaSuffix,
            dockerBuildOptions: '--build-arg BASE_IMAGE=' + productionImageName,
            dockerImageRepositoryName: repositoryName,
            visibility: visibility
        )
    }
}

sconeBuildUnlocked(
        nativeImage:     "docker-regis.iex.ec/$repositoryName:$buildInfo.imageTag",
        imageName:       repositoryName,
        imageTag:        buildInfo.imageTag,
        sconifyArgsPath: './docker/sconify.args',
        sconifyVersion:  '5.7.1'
)
