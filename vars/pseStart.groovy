import com.invisirisk.*

def call() {
    IpTablesSetup.setup()
    CertificateGenerator.generate()

    def buildUrl = "${env.JENKINS_URL}job/${env.JOB_NAME}/${env.BUILD_NUMBER}/"
    BuildSteps.notifyStart(buildUrl)

    // Return the build URL for later use
    return buildUrl
}
