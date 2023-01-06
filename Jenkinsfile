@Library('global-jenkins-library@feature/docker-build-options') _

String repositoryName = 'tee-worker-post-compute'

buildInfo = getBuildInfo()

// buildJavaProject(
//         buildInfo: buildInfo,
//         integrationTestsEnvVars: [],
//         shouldPublishJars: false,
//         shouldPublishDockerImages: true,
//         dockerfileDir: '.',
//         buildContext: '.',
//         dockerImageRepositoryName: repositoryName,
//         preProductionVisibility: 'docker.io',
//         productionVisibility: 'docker.io')

stage('Build Gramine') {
    dockerfileDir = './gramine'
    dockerImageRepositoryName = 'gramine-tee-worker-post-compute'
    visibility = 'iex.ec'
    productionImageName = ''
    stage('Build Gramine production image') {
        productionImageName = buildSimpleDocker_v3(
            buildInfo: buildInfo,
            dockerfileDir: dockerfileDir,
            buildContext: '.',
            dockerImageRepositoryName: dockerImageRepositoryName,
            visibility: visibility
        )
    }
    stage('Build Gramine test CA Gramine image') {
        buildInfo.imageTag = buildInfo.imageTag + '-test-ca'
        buildSimpleDocker_v3(
            buildInfo: buildInfo,
            dockerfileDir: dockerfileDir,
            dockerfileFilename: 'Dockerfile.test-ca',
            dockerBuildOptions: '--build-arg BASE_IMAGE=' + productionImageName,
            dockerImageRepositoryName: dockerImageRepositoryName,
            visibility: visibility
        )
    }
}

// sconeBuildUnlocked(
//         nativeImage:     "docker-regis.iex.ec/$repositoryName:$buildInfo.imageTag",
//         imageName:       repositoryName,
//         imageTag:        buildInfo.imageTag,
//         sconifyArgsPath: './docker/sconify.args',
//         sconifyVersion:  '5.7.1'
// )
