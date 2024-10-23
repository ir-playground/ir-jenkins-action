package com.invisirisk

class DockerSetup {
    static void setupAndRun(script, env) {
        def WORKSPACE = script.env.WORKSPACE

        def envVars = [
            "INVISIRISK_JWT_TOKEN=${env.INVISIRISK_JWT_TOKEN}",
            "PSE_DEBUG_FLAG=${env.PSE_DEBUG_FLAG}",
            "POLICY_LOG=${env.POLICY_LOG}",
            "INVISIRISK_PORTAL=${env.INVISIRISK_PORTAL}"
        ]

        if (env.POLICY_URL) {
            envVars.add("POLICY_URL=${env.POLICY_URL}")
        }
        if (env.POLICY_AUTH_TOKEN) {
            envVars.add("POLICY_AUTH_TOKEN=${env.POLICY_AUTH_TOKEN}")
        }
        script.sh """
            docker run -d --name ${env.PSE_CONTAINER_NAME} \
            ${envVars.collect { "-e $it" }.join(' ')} \
            --cap-add=NET_ADMIN \
            invisirisk/pse:latest
        """
    }

    static void cleanup(script, containerName) {
        script.sh """


            echo "Final PSE Proxy Logs:"
            docker logs ${containerName}

            echo "Performing cleanup"
            docker stop ${containerName} || true
            docker rm ${containerName} || true
        """
    }
}
