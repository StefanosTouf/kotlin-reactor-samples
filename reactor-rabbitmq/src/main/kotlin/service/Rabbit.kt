package service

import arrow.core.Either
import arrow.fx.coroutines.Resource
import arrow.fx.coroutines.release
import arrow.fx.coroutines.resource
import com.rabbitmq.client.*
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.reactive.asFlow
import kotlinx.coroutines.reactive.asPublisher
import reactor.core.scheduler.Schedulers
import reactor.rabbitmq.*
import java.util.logging.Logger

private object ConnectionHandler {
    val connectionFactory = lazy {
        val connectionFactory = ConnectionFactory();
        connectionFactory.useNio();
        connectionFactory
    }
}
typealias MakeReceiver = (host: String) -> Receiver
typealias MakeSender = (host: String) -> Sender

typealias CloseResource = () -> Unit

typealias Publish<T> = (Flow<Serialized<T>>) -> Flow<Void>
typealias MakePublisherResource<T> = (log: Log, host: String, exchange: String, routingKey: String) -> Pair<CloseResource, Publish<T>>

typealias Consumer = Flow<String>
typealias MakeConsumerResource = (log: Log, host: String, queue: String) -> Pair<CloseResource, Consumer>

object Rabbit {
    val makeReceiver: MakeReceiver = { host ->
        RabbitFlux.createReceiver(
            ReceiverOptions()
                .connectionFactory(ConnectionHandler.connectionFactory.value)
                .connectionSupplier { cf ->
                    cf.newConnection(arrayOf(Address(host)), "reactive-receiver")
                }.connectionSubscriptionScheduler(Schedulers.boundedElastic()))
    }

    val makeSender: MakeSender = { host ->
        RabbitFlux.createSender(
            SenderOptions()
                .connectionFactory(ConnectionHandler.connectionFactory.value)
                .connectionSupplier { cf ->
                    cf.newConnection(arrayOf(Address(host)), "reactive-sender")
                }.resourceManagementScheduler(Schedulers.boundedElastic()))

    }

    val makeConsumerResource: MakeConsumerResource = { log: Log, host: String, queue: String ->
        val receiver = makeReceiver(host)

        val get =
            receiver
                .consumeAutoAck(queue)
                .asFlow()
                .map { delivery -> String(delivery.body) }

        val close = {
            log(LogType.INFO, "Closing consumer").let { receiver.close() }
        }

        Pair(close, get)
    }

    fun <T> makePublisherResourceOfType(): MakePublisherResource<T> =
        { log: Log, host: String, exchange: String, routingKey: String ->
            val sender = makeSender(host)

            val publish = { messages: Flow<Serialized<T>> ->
                sender.send(
                    messages.map { message ->
                        OutboundMessage(exchange, routingKey, message.str.toByteArray())
                    }.asPublisher()
                ).asFlow()
            }

            val close = {
                log(LogType.INFO, "Closing publisher").let { sender.close() }
            }

            Pair(close, publish)
        }

}