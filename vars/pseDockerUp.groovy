import com.invisirisk.*

def call() {
    def dockerSetup = new com.invisirisk.DockerSetup()
    sh "------------------------"
    DockerSetup.setupAndRun(env)
}
