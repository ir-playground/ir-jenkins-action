
// import com.invisirisk.*

// def call() {
//     try {
//         // Initialize network and security setup
//         IpTablesSetup.setup(this)
//         CertificateGenerator.generate(this)

//         // Validate SCAN_ID is present
//         if (!env.SCAN_ID) {
//             error "SCAN_ID environment variable is not set"
//         }

//         // Construct the build URL using Jenkins environment variables
//         def buildUrl = "${env.JENKINS_URL}job/${env.JOB_NAME}/${env.BUILD_NUMBER}/".replaceAll("//+", "/")
//                                                                                     .replaceAll(":/", "://")
        
//         // Store build URL in environment for later use
//         env.PSE_BUILD_URL = buildUrl
        
//         echo "Starting PSE notification with SCAN_ID: ${env.SCAN_ID}"
//         BuildSteps.notifyStart(this, buildUrl)

//         return buildUrl
//     } catch (Exception e) {
//         echo "Warning: Error in pseStart: ${e.message}"
//         // Don't throw the error to prevent pipeline failure
//         return "${env.JENKINS_URL}job/${env.JOB_NAME}/${env.BUILD_NUMBER}/"
//     }
// }


import com.invisirisk.*

def call() {
    try {
         echo "Starting PSE initialization..."
        
        // Step 1: Export VB Environment Variables
        exportVBEnvironmentVariables()
        
        // Step 2: Create SBOM Scan
        createSBOMScan()
        
        // Step 3: Pull VB Image
        pullVBImage()

        // Initialize network and security setup
        IpTablesSetup.setup(this)
        CertificateGenerator.generate(this)

        // Validate SCAN_ID is present
        if (!env.SCAN_ID) {
            error "SCAN_ID environment variable is not set"
        }

        // Construct the build URL using Jenkins environment variables
        def buildUrl = "${env.JENKINS_URL}job/${env.JOB_NAME}/${env.BUILD_NUMBER}/".replaceAll("//+", "/")
                                                                                    .replaceAll(":/", "://")
        
        // Store build URL in environment for later use
        env.PSE_BUILD_URL = buildUrl
        
        echo "Starting PSE notification with SCAN_ID: ${env.SCAN_ID}"
        BuildSteps.notifyStart(this, buildUrl)

        return buildUrl
    } catch (Exception e) {
        echo "Warning: Error in pseStart: ${e.message}"
        // Don't throw the error to prevent pipeline failure
        return "${env.JENKINS_URL}job/${env.JOB_NAME}/${env.BUILD_NUMBER}/"
    }
}

private void exportVBEnvironmentVariables() {
    echo "Exporting VB Environment Variables..."
    
    def api_url = "${env.VB_API_URL}/utilityapi/v1/registry?api_key=${env.VB_API_KEY}"
    
    // Using httpRequest for better handling
    def response = httpRequest(
        url: api_url,
        wrapAsMultipart: false,
        validResponseCodes: '200'
    )
    
    def json = new JsonSlurper().parseText(response.content)
    
    if (!json.data) {
        error "No data received from registry API"
    }
    
    // Decode Base64 data
    byte[] decodedBytes = json.data.decodeBase64()
    String decodedString = new String(decodedBytes)
    def decodedData = new JsonSlurper().parseText(decodedString)
    
    // Set environment variables
    env.ECR_USERNAME = decodedData.username
    env.ECR_TOKEN = decodedData.password
    env.ECR_REGION = decodedData.region
    env.ECR_REGISTRY_ID = decodedData.registry_id
    
    echo "Successfully exported ECR environment variables"
}

private void createSBOMScan() {
    echo "Creating SBOM Scan..."
    
    def requestBodyJson = [api_key: env.VB_API_KEY]
    def requestBody = JsonOutput.toJson(requestBodyJson)
    def requestHeader = '"Content-Type: application/json"'
    
    def response = sh(
        script: """
            curl --location '${env.VB_API_URL}/utilityapi/v1/scan' \
            -d '${requestBody}' \
            --header ${requestHeader} \
            2>/dev/null
        """,
        returnStdout: true
    ).trim()
    
    def jsonResponse = new JsonSlurper().parseText(response)
    
    if (!jsonResponse.data?.scan_id) {
        error "Failed to get scan_id from API response"
    }
    
    env.SCAN_ID = jsonResponse.data.scan_id
    echo "Successfully created SBOM scan with ID: ${env.SCAN_ID}"
}

private void pullVBImage() {
    echo "Pulling VB Image..."
    
    // Validate required environment variables
    def required = ['ECR_USERNAME', 'ECR_TOKEN', 'ECR_REGISTRY_ID', 'ECR_REGION']
    def missing = required.findAll { !env[it] }
    if (missing) {
        error "Missing required ECR environment variables: ${missing.join(', ')}"
    }
    
    // Execute docker login
    def loginResult = sh(
        script: """
            docker login -u ${env.ECR_USERNAME} \
            -p ${env.ECR_TOKEN} \
            ${env.ECR_REGISTRY_ID}.dkr.ecr.${env.ECR_REGION}.amazonaws.com
        """,
        returnStatus: true
    )
    
    if (loginResult != 0) {
        error "Failed to login to ECR registry"
    }
    
    echo "Successfully logged into ECR registry"
}