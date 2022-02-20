import kotlinx.coroutines.flow.*
import kotlinx.coroutines.reactive.asPublisher
import kotlinx.coroutines.reactor.asFlux
import kotlinx.coroutines.reactor.mono
import org.springframework.http.MediaType
import org.springframework.web.reactive.function.server.*
import java.util.concurrent.Flow.Publisher

/**
 * An entity of type T serialized into string
 */
data class Serialized<T>(val str: String)

data class BasePath<T>(val path: String)

fun <T : Entity, T2 : Entity> getRoutes(log: Log,
                                        getEntity: FindOneService<T>,
                                        getEntities: FindAllService<T>,
                                        getTransformed: FindAllTransformedService<T2, T>,
                                        createEntity: CreateService<T>,
                                        basePath: BasePath<T>) =
    router {
        val (path) = basePath

        accept(MediaType.APPLICATION_JSON)
            .nest {
                "/$path".nest {
                    log("Registering GET:/$path")
                    GET("") {
                        getEntities()
                            .map { it.str }
                            .doOnError { log(it) }
                            .let { ServerResponse.ok().body(it) }
                    }

                    log("Registering GET:/$path/{name}")
                    GET("/{name}") { req ->
                        getEntity(req.pathVariable("name"))
                            .map { it.str }
                            .doOnError { log(it) }
                            .let { ServerResponse.ok().body(it) }
                    }

                    log("Registering POST:/$path")
                    POST("") { req: ServerRequest ->
                        req.bodyToMono<String>()
                            .doOnEach { println(it) }
                            .flatMap { createEntity(Serialized(it)) }
                            .let { ServerResponse.ok().body(it) }
                    }
                }

                log("Registering GET:/transformed/$path")
                GET("/transformed/$path") {
                    getTransformed()
                        .map { it.str }
                        .doOnError { log(it) }
                        .let { ServerResponse.ok().body(it) }
                }

            }
    }


