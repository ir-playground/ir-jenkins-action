package com.invisirisk

class BuildSteps {
    static void notifyStart(script, buildUrl) {
        script.sh """
            curl -X POST 'https://pse.invisirisk.com/start' \
            -H 'Content-Type: application/x-www-form-urlencoded' \
            -H 'User-Agent: pse-action' \
            -d "build_url=${buildUrl}&SCAN_ID=${script.env.SCAN_ID}" \
            -k \
            --tlsv1.2 \
            --insecure \
            --retry 3 \
            --retry-delay 2 \
            --max-time 10 \
            || true
        """
    }

    static void notifyEnd(script, buildUrl, buildStatus) {
        def status = buildStatus?.toLowerCase() ?: 'success'
        script.sh """
            curl -X POST 'https://pse.invisirisk.com/end' \
            -H 'Content-Type: application/x-www-form-urlencoded' \
            -H 'User-Agent: pse-action' \
            -d "build_url=${buildUrl}&status=${status}&SCAN_ID=${script.env.SCAN_ID}" \
            -k \
            --tlsv1.2 \
            --insecure \
            --retry 3 \
            --retry-delay 2 \
            --max-time 10 \
            || true
        """
    }
}