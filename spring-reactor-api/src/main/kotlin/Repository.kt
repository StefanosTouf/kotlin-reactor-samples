import io.r2dbc.postgresql.PostgresqlConnectionConfiguration
import io.r2dbc.postgresql.PostgresqlConnectionFactory
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.reactive.asFlow
import org.springframework.r2dbc.core.DatabaseClient
import org.ufoss.kotysa.Table
import org.ufoss.kotysa.postgresql.PostgresqlTable
import org.ufoss.kotysa.r2dbc.ReactorSqlClient
import org.ufoss.kotysa.r2dbc.sqlClient
import org.ufoss.kotysa.tables
import reactor.core.publisher.Flux
import reactor.core.publisher.Mono

typealias FindAllRepo<T> = () -> Flux<T>
typealias FindOneRepo<T> = (String) -> Mono<T>
typealias CreateRepo<T> = (T) -> Mono<Unit>

interface Repo {
    fun <A, T> findAll(client: ReactorSqlClient, table: T, log: Log): FindAllRepo<A>
            where A : Any, T : Table<A>, T : EntityTable<A>

    fun <A, T> findOne(client: ReactorSqlClient, table: T, log: Log): FindOneRepo<A>
            where A : Any, T : Table<A>, T : EntityTable<A>

    fun <A> create(client: ReactorSqlClient, log: Log): CreateRepo<A> where A : Any
}

object GenericRepo : Repo {
    override fun <A, T> findAll(client: ReactorSqlClient, table: T, log: Log): FindAllRepo<A>
            where A : Any, T : Table<A>, T : EntityTable<A> =
        {
            (client selectAllFrom table)
                .doOnNext { log(it) }
        }

    override fun <A, T> findOne(client: ReactorSqlClient, table: T, log: Log): FindOneRepo<A>
            where A : Any, T : Table<A>, T : EntityTable<A> =
        { name ->
            (client selectFrom table where table.name eq name)
                .fetchOne()
                .doOnNext { log(it) }
        }

    override fun <A> create(client: ReactorSqlClient, log: Log): CreateRepo<A> where A : Any =
        { a ->
            (client insert a)
                .map { log(it) }
        }
}

fun getClient(conf: DBConfig, vararg tables: PostgresqlTable<*>): ReactorSqlClient =
    DatabaseClient.builder()
        .connectionFactory(
            PostgresqlConnectionFactory(PostgresqlConnectionConfiguration
                                            .builder()
                                            .host(conf.host)
                                            .database(conf.dbName)
                                            .username(conf.username)
                                            .password(conf.password)
                                            .build()))
        .namedParameters(true)
        .build()
        .sqlClient(tables().postgresql(*tables))
