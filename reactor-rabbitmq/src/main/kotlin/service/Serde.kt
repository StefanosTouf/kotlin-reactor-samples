package service

import arrow.core.Either
import entity.*
import kotlinx.serialization.decodeFromString
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json
import service.Serialized

data class Serialized<T>(val str: String)

typealias Deserialize<T> = (String) -> T
typealias Serialize<T> = (T) -> Serialized<T>

object Serde {
    val dogSerialize: Serialize<Dog> =
        fun(dog) = Serialized<Dog>(Json.encodeToString(dog))

    val dogDeserialize: Deserialize<Dog> =
        fun(str) = Json.decodeFromString<Dog>(str)

    val incomingSerialize: Serialize<Incoming> =
        fun(dog) = Serialized<Incoming>(Json.encodeToString(dog))

    val incomingDeserialize: Deserialize<Incoming> =
        fun(str) = Json.decodeFromString<Incoming>(str)

    val unidentifiedSerialize: Serialize<UnidentifiedDog> =
        fun(dog) = Serialized<UnidentifiedDog>(Json.encodeToString(dog))

    val unidentifiedDeserialize: Deserialize<UnidentifiedDog> =
        fun(str) = Json.decodeFromString<UnidentifiedDog>(str)

}

