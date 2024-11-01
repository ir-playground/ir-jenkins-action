// vars/CreateScan.groovy
import groovy.json.JsonSlurper
import groovy.json.JsonOutput

def call() {
    try {
        echo "Starting VB scan initialization..."
        
        // Step 1: Export VB Environment Variables
        exportVBEnvironmentVariables()
        
        // Step 2: Create SBOM Scan
        createSBOMScan()
        
        // Step 3: Pull VB Image
        pullVBImage()
        
        return env.SCAN_ID
    } catch (Exception e) {
        echo "Error in CreateScan: ${e.message}"
        error "VB scan initialization failed: ${e.message}"
    }
}

private void exportVBEnvironmentVariables() {
    echo "Exporting VB Environment Variables..."
    
    // Validate VB API environment variables
    if (!env.VB_API_URL || !env.VB_API_KEY) {
        error "Missing required variables: VB_API_URL or VB_API_KEY"
    }
    
    def api_url = "${env.VB_API_URL}utilityapi/v1/registry?api_key=${env.VB_API_KEY}"
    
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
            curl --location '${env.VB_API_URL}utilityapi/v1/scan' \
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