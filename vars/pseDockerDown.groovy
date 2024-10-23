import com.invisirisk.*

def call() {
    def dockerSetup = new com.invisirisk.DockerSetup()
    DockerSetup.cleanup(this, env.PSE_CONTAINER_NAME)
}
