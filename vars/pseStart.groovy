
import com.invisirisk.*

def call() {
    try {
        // Initialize network and security setup
        IpTablesSetup.setup(this)
        CertificateGenerator.generate(this)

        // Validate SCAN_ID is present
        if (!env.SCAN_ID) {
            error "SCAN_ID environment variable is not set"
        }

        // Construct the build URL using Jenkins environment variables
        def buildUrl = "${env.JENKINS_URL}job/${env.JOB_NAME}/${env.BUILD_NUMBER}/".replaceAll("//+", "/")
                                                                                    .replaceAll(":/", "://")
        
        // Store build URL in environment for later use
        env.PSE_BUILD_URL = buildUrl
        
        echo "Starting PSE notification with SCAN_ID: ${env.SCAN_ID}"
        BuildSteps.notifyStart(this, buildUrl)

        return buildUrl
    } catch (Exception e) {
        echo "Warning: Error in pseStart: ${e.message}"
        // Don't throw the error to prevent pipeline failure
        return "${env.JENKINS_URL}job/${env.JOB_NAME}/${env.BUILD_NUMBER}/"
    }
}

