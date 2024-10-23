package com.invisirisk

class DockerSetup {
    static void setupAndRun(script, env) {
        def WORKSPACE = script.env.WORKSPACE
        script.sh """
            docker run -d --name ${env.PSE_CONTAINER_NAME} \
            -e INVISIRISK_JWT_TOKEN=${env.INVISIRISK_JWT_TOKEN} \
            -e PSE_DEBUG_FLAG=${env.PSE_DEBUG_FLAG} \
            -e POLICY_LOG=${env.POLICY_LOG} \
            -e INVISIRISK_PORTAL=${env.INVISIRISK_PORTAL} \
            -e POLICY_URL=${env.POLICY_URL} \
            -e POLICY_AUTH_TOKEN=${env.POLICY_AUTH_TOKEN} \
            --cap-add=NET_ADMIN \
            invisirisk/pse:latest
        """
    }

    static void cleanup(script, containerName) {
        script.sh """
        

            echo "Final PSE Proxy Logs:"
            docker logs ${containerName}

            echo "Performing cleanup"
            #docker stop ${containerName} || true
            #docker rm ${containerName} || true
        """
    }
}
