import com.invisirisk.*

def call(this) {
    def dockerSetup = new com.invisirisk.DockerSetup()
    DockerSetup.setupAndRun(env)
}
