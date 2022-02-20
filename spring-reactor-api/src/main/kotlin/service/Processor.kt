@file:Suppress("NestedLambdaShadowedImplicitParameter")

import reactor.core.publisher.Flux
import reactor.core.publisher.Mono

typealias FindOneService<T> = (name: String) -> Mono<Serialized<T>>
typealias FindAllService<T> = () -> Flux<Serialized<T>>
typealias FindAllTransformedService<T, T2> = () -> Flux<Serialized<Transformed<T, T2>>>
typealias CreateService<T> = (Serialized<T>) -> Mono<Unit>


interface Service {
    fun <T : Entity> findOne(repo: FindOneRepo<T>,
                             serialize: Serialize<T>): FindOneService<T>

    fun <T : Entity> findAll(repo: FindAllRepo<T>,
                             serialize: Serialize<T>): FindAllService<T>

    fun <T2 : Entity, T : Entity> findAllTransformed(repo: FindAllRepo<T2>,
                                                     serialize: Serialize<Transformed<T2, T>>,
                                                     transform: Transformer<T2, T>): FindAllTransformedService<T2, T>

    fun <T : Entity> create(repo: CreateRepo<T>,
                            deserialize: Deserialize<T>): CreateService<T>
}

object GenericService : Service {
    override fun <T : Entity> findOne(repo: FindOneRepo<T>,
                                      serialize: Serialize<T>): FindOneService<T> =
        { name ->
            repo(name)
                .flatMap { serialize(it) }
        }

    override fun <T : Entity> findAll(repo: FindAllRepo<T>,
                                      serialize: Serialize<T>): FindAllService<T> =
        {
            repo()
                .flatMap { serialize(it) }
        }

    override fun <T2 : Entity, T : Entity> findAllTransformed(repo: FindAllRepo<T2>,
                                                              serialize: Serialize<Transformed<T2, T>>,
                                                              transform: Transformer<T2, T>): FindAllTransformedService<T2, T> =
        {
            repo()
                .flatMap { transform(it) }
                .flatMap { serialize(it) }
        }

    override fun <T : Entity> create(repo: CreateRepo<T>,
                                     deserialize: Deserialize<T>): CreateService<T> =
        {
            deserialize(it)
                .flatMap { repo(it) }
        }
}
