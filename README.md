# Jenkins Shared Library

You can use the following script to run the pipeline. This example shows how to run the build steps inside a Docker container:

```groovy
@Library('ir-jenkins-action') _

pipeline {
    agent any

    environment {
        INVISIRISK_JWT_TOKEN = credentials('INVISIRISK_JWT_TOKEN')
        INVISIRISK_PORTAL = 'http://192.168.40.114:3000/'
        PSE_DEBUG_FLAG = '--alsologtostderr'
        POLICY_LOG = 't'
        PSE_CONTAINER_NAME = 'pse-proxy'
        POLICY_URL = 'https://api.github.com/repos/ashokkasti/policy/tarball/main'
        POLICY_AUTH_TOKEN = credentials('POLICY_AUTH_TOKEN')
    }

    stages {
        stage('Setup') {
            steps {
                script {
                    buildUrl = pseStart()
                }
            }
        }

        stage('Build and Test') {
            agent {
                docker {
                    image 'node:14'  // Use an appropriate Node.js image
                    args '-v $HOME/.npm:/root/.npm'  // Cache npm packages
                }
            }
            steps {
                // User's custom build steps go here, now running inside the container
                sh '''
                    npm config set cache /root/.npm
                    npm install
                    npm ci
                    npm run build --if-present
                    npm test
                '''
            }
        }
    }

    post {
        always {
            script {
                pseEnd(buildUrl)
            }
        }
    }
}
```

This pipeline script demonstrates how to:

1. Use the shared library
2. Set up environment variables
3. Run the `pseStart()` function from the shared library
4. Execute build and test steps inside a Docker container
5. Run the `pseEnd()` function from the shared library in the post-build stage

Make sure you have Docker installed on your Jenkins agent and the necessary permissions to run Docker commands.
