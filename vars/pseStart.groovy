import com.invisirisk.*

def call() {
    IpTablesSetup.setup(this)
    CertificateGenerator.generate(this)

    def buildUrl = "${env.JENKINS_URL}job/${env.JOB_NAME}/${env.BUILD_NUMBER}/"
    BuildSteps.notifyStart(this, buildUrl)

    // Return the build URL for later use
    return buildUrl
}
