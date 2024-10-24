import com.invisirisk.*

def call(String buildUrl = null) {
    try {
        // Use provided buildUrl or fall back to stored/constructed URL
        def finalBuildUrl = buildUrl ?: env.PSE_BUILD_URL ?: 
                           "${env.JENKINS_URL}job/${env.JOB_NAME}/${env.BUILD_NUMBER}/".replaceAll("//+", "/")
                                                                                       .replaceAll(":/", "://")
        
        // Validate SCAN_ID is still present
        if (!env.SCAN_ID) {
            error "SCAN_ID environment variable is not set"
        }

        // Get build status, defaulting to SUCCESS if not set
        def buildStatus = currentBuild.result ?: 'SUCCESS'
        
        echo "Ending PSE notification with SCAN_ID: ${env.SCAN_ID}, status: ${buildStatus}"
        BuildSteps.notifyEnd(this, finalBuildUrl, buildStatus)
    } catch (Exception e) {
        echo "Warning: Error in pseEnd: ${e.message}"
        // Don't throw the error to prevent pipeline failure
    }
}