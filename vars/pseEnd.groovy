import com.invisirisk.*

def call(String buildUrl) {
    def buildStatus = currentBuild.result ?: 'SUCCESS'
    BuildSteps.notifyEnd(this, buildUrl, buildStatus)
}
