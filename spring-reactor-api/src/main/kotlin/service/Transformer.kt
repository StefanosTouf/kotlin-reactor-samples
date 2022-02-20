import reactor.core.publisher.Mono

/**
 * An entity of type T2 transformed from T
 */
data class Transformed<T, T2>(val entity: T2)

typealias Transformer<T, T2> = (T) -> Mono<Transformed<T, T2>>

interface Transformers {
    val personToDog: Transformer<Person, Dog>

    val dogToPerson: Transformer<Dog, Person>
}

object TransformersImpl : Transformers {
    override val personToDog: Transformer<Person, Dog> = { person ->
        Mono.just(Transformed(Dog(person.name, person.age / 7)))
    }

    override val dogToPerson: Transformer<Dog, Person> = { dog ->
        Mono.just(Transformed(Person(dog.name, dog.age * 7)))
    }
}