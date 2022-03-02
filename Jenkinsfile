@Library('global-jenkins-library@feature/build-info') _

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

node('docker') { //any node would be fine
    String gitShortCommit =
            sh(script: 'git rev-parse --short=8 HEAD', returnStdout: true).trim()
    def nativeImage = 'nexus.iex.ec/' + repositoryName + ':' + gitShortCommit

    sconeBuildAllTee(nativeImage: nativeImage, targetImageRepositoryName: repositoryName,
            sconifyArgsPath: './docker/sconify.args')
}
