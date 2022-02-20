@file:Suppress("NAME_SHADOWING")

package main

import arrow.core.Either
import entity.Dog
import entity.DogOps.incomingToDog
import entity.Incoming
import entity.UnidentifiedDog
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.runBlocking
import service.*

fun <T, R> Flow<T>.flatMap(concurrency: Int = DEFAULT_CONCURRENCY,
                           transform: suspend (value: T) -> Flow<R>) =
    this.flatMapMerge(concurrency, transform)

/**
 * Requires a logger.
 * Attempts to identify an `Incoming` entity and logs its output.
 * If identification is successful, returns an `Either.Right` containing an instance of `Dog`.
 * If unsuccessful, returns an `Either.Left` containing an instance of unidentified `Dog`
 */
fun logAndConvertIncoming(log: Log) =
    fun(incoming: Incoming): Flow<Either<UnidentifiedDog, Dog>> =
        flowOf(incoming)
            .map { incomingToDog(it) }
            .onEach { log(LogType.INFO, "Got $it") }

/**
 * Requires a channel of type T and a channel of type Y.
 * Receives an `Either` of T (Left) or Y (Right) and pipes the value to the correct channel.
 */
fun <T, Y> sendEitherToChannels(channelT: Channel<T>, channelY: Channel<Y>) =
    fun(either: Either<T, Y>): Flow<Unit> =
        flowOf(either).map { either ->
            either.fold({ channelT.send(it) },
                        { channelY.send(it) })
        }

/**
 * Requires a serializer of T and a logger.
 * Attempts to serialize the entity of type T and handles exceptions that might be thrown in the process.
 */
fun <T> attemptSerialize(serialize: Serialize<T>, log: Log) =
    fun(entity: T): Flow<Serialized<T>> =
        flowOf(serialize(entity))
            .catch { e ->
                log(LogType.WARN, "Couldn't serialize $entity because of $e")
            }

/**
 * Requires a deserializer of T and a logger.
 * Attempts to deserialize a string into an entity of type T and handles exceptions that might be thrown in the process
 */
fun <T> attemptDeserialize(deserialize: Deserialize<T>, log: Log) =
    fun(json: String): Flow<T> =
        flowOf(deserialize(json))
            .catch { e ->
                log(LogType.WARN, "Couldn't deserialize $json because of $e")
            }

/**
 * The core processing logic of incoming messages.
 * It deserializes the incoming messages then identifies them as dogs, then sends them to appropriate channels.
 */
fun processIncoming(consumer: Consumer,
                    deserialize: (String) -> Flow<Incoming>,
                    tryConvert: (Incoming) -> Flow<Either<UnidentifiedDog, Dog>>,
                    sendToChannels: (Either<UnidentifiedDog, Dog>) -> Flow<Unit>) =
    consumer
        .flatMap { json -> deserialize(json) }
        .flatMap { incoming -> tryConvert(incoming) }
        .flatMap { either -> sendToChannels(either) }

/**
 * Requires a `Serializer` of T.
 * Receives messages of type T from a channel, serializes them and publishes them.
 */
fun <T> serializeAndSend(serialize: (T) -> Flow<Serialized<T>>) =
    fun(channel: Channel<T>, publishFlow: (Flow<Serialized<T>>) -> Flow<Void>): Flow<Unit> =
        channel.receiveAsFlow()
            .flatMap { serialize(it) }
            .let(publishFlow)
            .map { }

/**
 * Partially applies all the services with their dependencies, preparing them for use, and supplies them to the core logic.
 */
fun app(config: Config,
        log: Log,
        dogSer: Serialize<Dog>,
        unIdSer: Serialize<UnidentifiedDog>,
        incomingDeser: Deserialize<Incoming>,
        makeConsumerResource: MakeConsumerResource,
        makeDogPublisherResource: MakePublisherResource<Dog>,
        makeUnIdPublisherResource: MakePublisherResource<UnidentifiedDog>): Flow<Unit> {

    val (closeConsumer, consumer) =
        makeConsumerResource(log, config.host, config.consumeQueue)

    val (closeDogPub, dogPublisher) =
        makeDogPublisherResource(log, config.host, "", config.publishQueueIdentified)

    val (closeUnIdPub, unIdPublisher) =
        makeUnIdPublisherResource(log, config.host,
                                  "",
                                  config.publishQueueUnidentified)

    val dogChannel = Channel<Dog>()
    val unIdChannel = Channel<UnidentifiedDog>()

    val convertIncoming = logAndConvertIncoming(log)
    val sendEither = sendEitherToChannels(unIdChannel, dogChannel)

    val deserializeIncoming = attemptDeserialize(incomingDeser, log)

    val processIncomingAndSendToChannels =
        processIncoming(consumer, deserializeIncoming, convertIncoming, sendEither)

    val sendDogs = serializeAndSend(attemptSerialize(dogSer, log))
    val sendUnId = serializeAndSend(attemptSerialize(unIdSer, log))

    return flowOf(processIncomingAndSendToChannels,
                  sendDogs(dogChannel, dogPublisher),
                  sendUnId(unIdChannel, unIdPublisher))
        .flattenMerge()
        .catch { e -> log(LogType.ERROR, "Shutting down because of $e") }
        .onCompletion { closeConsumer(); closeDogPub(); closeUnIdPub() }
}

fun main(args: Array<String>) = runBlocking {
    app(MockConfig,
        Loggers.log,
        Serde.dogSerialize,
        Serde.unidentifiedSerialize,
        Serde.incomingDeserialize,
        Rabbit.makeConsumerResource,
        Rabbit.makePublisherResourceOfType(),
        Rabbit.makePublisherResourceOfType()).collect()
}
