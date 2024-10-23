package com.invisirisk

class CertificateGenerator {
    static void generate(script) {
        def caFile = "/etc/ssl/certs/pse.pem"

        script.sh "echo 'Downloading CA certificate'"

        // Use curl to fetch the certificate
        def curlCommand = """
            curl -s -o ${caFile} https://pse.invisirisk.com/ca \
                -H 'User-Agent: Jenkins' \
                --insecure
        """

        def result = script.sh(script: curlCommand, returnStatus: true)

        if (result != 0) {
            script.sh "echo 'Error getting CA certificate, curl command failed with status ${result}'"
            script.error("Error getting CA certificate, curl command failed with status ${result}")
        }

        script.sh "echo 'Installing CA certificate'"

        // Update CA certificates
        script.sh 'update-ca-certificates'

        // Configure Git to use the new CA file
        script.sh "git config --global http.sslCAInfo ${caFile}"

        // Set environment variables using script.env instead of env
        script.env.NODE_EXTRA_CA_CERTS = caFile
        script.env.REQUESTS_CA_BUNDLE = caFile

        // If you need to use these variables in npm, you can add:
        script.sh """
            npm config set cafile ${caFile}
            npm config set strict-ssl false
        """
    }
}
