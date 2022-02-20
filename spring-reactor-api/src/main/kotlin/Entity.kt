import kotlinx.serialization.Serializable
import kotlinx.serialization.encodeToString
import kotlinx.serialization.decodeFromString
import kotlinx.serialization.json.Json
import org.ufoss.kotysa.columns.StringDbVarcharColumnNotNull
import org.ufoss.kotysa.columns.StringDbVarcharColumnNullable
import org.ufoss.kotysa.postgresql.PostgresqlTable

interface EntityTable<T : Any> {
    val name: StringDbVarcharColumnNotNull<T>
}

object PersonTable : PostgresqlTable<Person>("person"), EntityTable<Person> {
    override val name = varchar(Person::name).primaryKey()
    val age = integer(Person::age)
    val dogName = varchar(Person::dogName, defaultValue = "none").foreignKey(DogTable.name)
}

object DogTable : PostgresqlTable<Dog>("dog"), EntityTable<Dog> {
    override val name = varchar(Dog::name).primaryKey()
    val age = integer(Dog::age)
}

interface Entity {
    val name: String
}

@Serializable
data class Person(override val name: String, val age: Int, val dogName: String? = "none") : Entity

@Serializable
data class Dog(override val name: String, val age: Int) : Entity

val personBasePath: BasePath<Person> = BasePath("person")
val dogBasePath: BasePath<Dog> = BasePath("dog")
