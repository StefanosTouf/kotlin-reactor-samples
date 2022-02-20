package service

interface Config {
    val consumeQueue: String
    val publishQueueIdentified: String
    val publishQueueUnidentified: String
    val host: String
}

object MockConfig: Config {
   override val consumeQueue = "incoming"
   override val publishQueueIdentified = "identified"
   override val publishQueueUnidentified = "unidentified"
   override val host = "localhost"
}

object ContainerConfig: Config {
    override val consumeQueue = "incoming"
    override val publishQueueIdentified = "identified"
    override val publishQueueUnidentified = "unidentified"
    override val host = "rabbitmq"
}