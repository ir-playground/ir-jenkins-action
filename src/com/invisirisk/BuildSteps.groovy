package com.invisirisk

class BuildSteps {
    private static final String BASE_URL = 'https://pse.invisirisk.com'
    
    static void notifyStart(script, buildUrl) {
        try {
            validateInputs(script)
            
            // Get git information if available
            def gitInfo = getGitInfo(script)
            
            def queryParams = [
                'builder': 'jenkins',
                'id': script.env.SCAN_ID,
                'build_id': script.env.BUILD_NUMBER,
                'build_url': buildUrl,
                'project': gitInfo.repoName ?: script.env.JOB_NAME,
                'workflow': script.env.JOB_NAME,
                'builder_url': script.env.JENKINS_URL,
                'scm': 'git',
                'scm_commit': gitInfo.commit,
                'scm_branch': gitInfo.branch,
                'scm_origin': gitInfo.origin
            ]
            
            sendRequest(script, "${BASE_URL}/start", queryParams)
            
        } catch (Exception e) {
            script.echo "Warning: Failed to send start notification: ${e.message}"
        }
    }

    static void notifyEnd(script, buildUrl, buildStatus) {
        try {
            validateInputs(script)
            
            def status = buildStatus?.toLowerCase() ?: 'success'
            def gitInfo = getGitInfo(script)
            
            def queryParams = [
                'builder': 'jenkins',
                'id': script.env.SCAN_ID,
                'build_id': script.env.BUILD_NUMBER,
                'build_url': buildUrl,
                'project': gitInfo.repoName ?: script.env.JOB_NAME,
                'workflow': script.env.JOB_NAME,
                'builder_url': script.env.JENKINS_URL,
                'scm': 'git',
                'scm_commit': gitInfo.commit,
                'scm_branch': gitInfo.branch,
                'scm_origin': gitInfo.origin,
                'status': status
            ]
            
            sendRequest(script, "${BASE_URL}/end", queryParams)
            
        } catch (Exception e) {
            script.echo "Warning: Failed to send end notification: ${e.message}"
        }
    }
    
    private static void validateInputs(script) {
        if (!script.env.SCAN_ID) throw new IllegalArgumentException("SCAN_ID environment variable is not set")
        if (!script.env.BUILD_NUMBER) throw new IllegalArgumentException("BUILD_NUMBER environment variable is not set")
        if (!script.env.JOB_NAME) throw new IllegalArgumentException("JOB_NAME environment variable is not set")
    }
    
    private static Map getGitInfo(script) {
        try {
            def gitUrl = script.sh(script: 'git config --get remote.origin.url || true', returnStdout: true).trim()
            def gitBranch = script.sh(script: 'git rev-parse --abbrev-ref HEAD || true', returnStdout: true).trim()
            def gitCommit = script.sh(script: 'git rev-parse HEAD || true', returnStdout: true).trim()
            
            def repoName = gitUrl ? gitUrl.tokenize('/')[-2..-1].join('/').replaceAll('\\.git$', '') : ''
            
            return [
                origin: gitUrl,
                branch: gitBranch,
                commit: gitCommit,
                repoName: repoName
            ]
        } catch (Exception e) {
            script.echo "Warning: Could not get git info: ${e.message}"
            return [origin: '', branch: '', commit: '', repoName: '']
        }
    }
    
    private static void sendRequest(script, url, params) {
        def queryString = params.findAll { it.value }
                               .collect { k, v -> "${urlEncode(k)}=${urlEncode(v.toString())}" }
                               .join('&')
        
        script.sh """
            curl -X POST '${url}' \
            -H 'Content-Type: application/x-www-form-urlencoded' \
            -H 'User-Agent: pse-action' \
            -d "${queryString}" \
            -k \
            --tlsv1.2 \
            --insecure \
            --retry 3 \
            --retry-delay 2 \
            --max-time 10 \
            || true
        """
    }
    
    private static String urlEncode(String value) {
        if (!value) return ''
        return java.net.URLEncoder.encode(value, 'UTF-8')
            .replace('+', '%20')
            .replace('%2F', '/')
    }
}
