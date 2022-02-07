@Library('global-jenkins-library@feature/sconifier-5.3.10') _

String repositoryName = 'tee-worker-post-compute'

buildJavaProject(
        integrationTestsEnvVars: [],
        shouldPublishJars: true,
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