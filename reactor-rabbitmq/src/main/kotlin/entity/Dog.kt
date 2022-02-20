package entity

import arrow.core.Either
import arrow.core.right
import kotlinx.serialization.Serializable

enum class Hair {
    Short, Medium, Long
}

enum class Size {
    Small, Medium, Big
}

enum class Attitude {
    Aggressive, Friendly
}

enum class Species(val hair: Hair, val size: Size, val attitude: Attitude) {
    GermanShepherd(Hair.Medium, Size.Big, Attitude.Aggressive),
    AmericanPitBull(Hair.Short, Size.Medium, Attitude.Aggressive),
    Chihuahua(Hair.Medium, Size.Small, Attitude.Aggressive),
    Beagle(Hair.Short, Size.Medium, Attitude.Friendly),
    Collie(Hair.Long, Size.Medium, Attitude.Friendly)
}

@Serializable
data class Incoming(
        val name: String,
        val hair: Hair,
        val size: Size,
        val attitude: Attitude
)

@Serializable
data class UnidentifiedDog(
        val name: String,
        val hair: Hair,
        val size: Size,
        val attitude: Attitude
)

@Serializable
data class Dog(val name: String, val species: Species)


object DogOps {
    fun incomingToDog(incoming: Incoming): Either<UnidentifiedDog, Dog> {
        val compare: (Hair, Size, Attitude) -> Boolean = { hair, size, attitude ->
            incoming.hair == hair
                    && incoming.size == size
                    && incoming.attitude == attitude
        }


        val rightDog: (Species) -> Either.Right<Dog> = {
            Either.Right(Dog(incoming.name, it))
        }

        return when {
            compare(Hair.Medium, Size.Big, Attitude.Aggressive)
            -> rightDog(Species.GermanShepherd)
            compare(Hair.Short, Size.Medium, Attitude.Aggressive)
            -> rightDog(Species.AmericanPitBull)
            compare(Hair.Medium, Size.Medium, Attitude.Aggressive)
            -> rightDog(Species.Chihuahua)
            compare(Hair.Short, Size.Medium, Attitude.Friendly)
            -> rightDog(Species.Beagle)
            compare(Hair.Long, Size.Medium, Attitude.Friendly)
            -> rightDog(Species.Collie)
            else -> Either.Left(UnidentifiedDog(incoming.name,
                                                incoming.hair,
                                                incoming.size,
                                                incoming.attitude))
        }
    }
}