import kotlinx.serialization.decodeFromString
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json
import reactor.core.publisher.Mono

typealias Serialize<T> = (T) -> Mono<Serialized<T>>
typealias Deserialize<T> = (Serialized<T>) -> Mono<T>

interface Serdes {
    val person: Serde<Person>
    val dog: Serde<Dog>
    val transformedDog: Serde<Transformed<Person, Dog>>
    val transformedPerson: Serde<Transformed<Dog, Person>>
}

interface Serde<T> {
    val serialize: Serialize<T>
    val deserialize: Deserialize<T>
}

object DogSerde : Serde<Dog> {
    override val serialize: Serialize<Dog> = {
        Mono.just(Serialized(Json.encodeToString(it)))
    }

    override val deserialize: Deserialize<Dog> = {
        Mono.just(Json.decodeFromString(it.str))
    }
}

object PersonSerde : Serde<Person> {
    override val serialize: Serialize<Person> = {
        Mono.just(Serialized(Json.encodeToString(it)))
    }

    override val deserialize: Deserialize<Person> = {
        Mono.just(Json.decodeFromString(it.str))
    }
}

object TransformedDogSerde : Serde<Transformed<Person, Dog>> {
    override val serialize: Serialize<Transformed<Person, Dog>> = {
        Mono.just(Serialized(Json.encodeToString(it.entity)))
    }

    override val deserialize: Deserialize<Transformed<Person, Dog>> = {
        Mono.just(Transformed(Json.decodeFromString<Dog>(it.str)))
    }
}

object TransformedPersonSerde : Serde<Transformed<Dog, Person>> {
    override val serialize: Serialize<Transformed<Dog, Person>> = {
        Mono.just(Serialized(Json.encodeToString(it.entity)))
    }

    override val deserialize: Deserialize<Transformed<Dog, Person>> = {
        Mono.just(Transformed(Json.decodeFromString<Person>(it.str)))
    }
}

object SerdesImpl : Serdes {
    override val person = PersonSerde
    override val dog = DogSerde
    override val transformedDog = TransformedDogSerde
    override val transformedPerson = TransformedPersonSerde
}