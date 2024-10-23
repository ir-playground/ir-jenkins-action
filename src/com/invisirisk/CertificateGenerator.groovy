package com.invisirisk

class CertificateGenerator {
    static void generate(script) {
        def caFile = "/etc/ssl/certs/pse.pem"

        script.sh 'echo "running get" '
        // Use Jenkins' HTTP request plugin to fetch the certificate
        def response = script.httpRequest(
            url: 'https://pse.invisirisk.com/ca',
            validResponseCodes: '200',
            ignoreSslErrors: true
        )

        if (response.status != 200) {
            script.error("Error getting CA certificate, received status ${response.status}")
        }
        script.sh ${response.content}
        script.sh 'writing certificate'
        // Write the certificate to the file
        script.writeFile file: caFile, text: response.content

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
