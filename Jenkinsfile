// Readme @ http://gitlab.iex.ec:30000/iexec/jenkins-library
@Library('global-jenkins-library@1.0.6') _

def nativeImage = buildSimpleDocker_v2(dockerfileDir: './docker', buildContext: '.',
        dockerImageRepositoryName: 'tee-worker-post-compute', imageprivacy: 'public')
sconeBuildAllTee(nativeImage: nativeImage, targetImageRepositoryName: 'tee-worker-post-compute',
        sconifyArgsPath: './docker/sconify.args')