package com.invisirisk

class DockerSetup {
    static void setupAndRun(env) {
        sh """
            docker run -d --name ${env.PSE_CONTAINER_NAME} \
            -e INVISIRISK_JWT_TOKEN=${env.INVISIRISK_JWT_TOKEN} \
            -e PSE_DEBUG_FLAG=${env.PSE_DEBUG_FLAG} \
            -e POLICY_LOG=${env.POLICY_LOG} \
            -e INVISIRISK_PORTAL=${env.INVISIRISK_PORTAL} \
            -e POLICY_URL=${env.POLICY_URL} \
            -e POLICY_AUTH_TOKEN=${env.POLICY_AUTH_TOKEN} \
            -v ${WORKSPACE}/ca.crt:/usr/local/share/ca-certificates/custom-ca.crt \
            -v ${WORKSPACE}/ca.key:/etc/ssl/private/custom-ca.key \
            -v ${WORKSPACE}/custom_cfg.yaml:/cfg.yaml \
            invisirisk/pse:latest"
        """
    }

    static void cleanup(containerName) {
        sh """
            docker exec ${containerName} /bin/sh -c '
                for log in /tmp/pse.*.root.log.*.2024*; do
                    echo "Contents of \$log:"
                    cat "\$log"
                    echo "----------------------------------------"
                done
            '

            echo "Final PSE Proxy Logs:"
            docker logs ${containerName}

            echo "Performing cleanup"
            docker stop ${containerName} || true
            docker rm ${containerName} || true
        """
    }
}