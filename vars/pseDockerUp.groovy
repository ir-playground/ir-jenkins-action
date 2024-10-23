import com.invisirisk.*

def call() {
    def dockerSetup = new com.invisirisk.DockerSetup()
    DockerSetup.setupAndRun(this, env)
}
