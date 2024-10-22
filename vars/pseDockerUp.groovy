import com.invisirisk.*

def call(script) {
    def dockerSetup = new com.invisirisk.DockerSetup()
    DockerSetup.setupAndRun(env)
}
