import com.invisirisk.*

def call(String buildUrl) {
    def buildStatus = currentBuild.result ?: 'SUCCESS'
    BuildSteps.notifyEnd(buildUrl, buildStatus)
    DockerSetup.cleanup(env.PSE_CONTAINER_NAME)
}
