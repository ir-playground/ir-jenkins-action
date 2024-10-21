package com.invisirisk

class CertificateGenerator {
    static void generate() {
        def caFile = "/etc/ssl/certs/pse.pem"

        // Use Jenkins' HTTP request plugin to fetch the certificate
        def response = httpRequest(
            url: 'https://pse.invisirisk.com/ca',
            validResponseCodes: '200',
            ignoreSslErrors: true
        )

        if (response.status != 200) {
            error("Error getting CA certificate, received status ${response.status}")
        }

        // Write the certificate to the file
        writeFile file: caFile, text: response.content

        // Update CA certificates
        sh 'update-ca-certificates'

        // Configure Git to use the new CA file
        sh "git config --global http.sslCAInfo ${caFile}"

        // Set environment variables
        env.NODE_EXTRA_CA_CERTS = caFile
        env.REQUESTS_CA_BUNDLE = caFile

        // If you need to use these variables in npm, you can add:
        sh """
            npm config set cafile ${caFile}
            npm config set strict-ssl false
        """
    }
}
