package com.invisirisk

class BuildSteps {
    static void notifyStart(buildUrl) {
        sh """
            curl -X POST 'https://pse.invisirisk.com/start' \
            -H 'Content-Type: application/x-www-form-urlencoded' \
            -H 'User-Agent: pse-action' \
            -d 'build_url=${buildUrl}' \
            -k
        """
    }

    static void notifyEnd(buildUrl, buildStatus) {
        def status = buildStatus?.toLowerCase() ?: 'success'
        sh """
            curl -X POST 'https://pse.invisirisk.com/end' \
            -H 'Content-Type: application/x-www-form-urlencoded' \
            -H 'User-Agent: pse-action' \
            -d 'build_url=${buildUrl}&status=${status}' \
            -k
        """
    }
}
