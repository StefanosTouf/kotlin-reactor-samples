import com.sksamuel.hoplite.ConfigLoader
import kotlinx.coroutines.*
import kotlinx.coroutines.flow.flattenMerge
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.reactive.asFlow
import org.springframework.http.server.reactive.ReactorHttpHandlerAdapter
import org.springframework.web.reactive.function.server.*
import org.ufoss.kotysa.r2dbc.ReactorSqlClient
import reactor.netty.http.server.HttpServer


fun main(): Unit = runBlocking {
    getConfig().map { (dbConfig, apiConfig) ->
        val sqlClient = getClient(dbConfig, PersonTable, DogTable)

        sqlClient.createTableIfNotExists(PersonTable)
            .flatMap { sqlClient.createTableIfNotExists(DogTable) }
            .subscribe()

        makeRoutes(GenericRepo,
                   log,
                   sqlClient,
                   GenericService,
                   SerdesImpl,
                   TransformersImpl)
            .let {
                ReactorHttpHandlerAdapter(RouterFunctions.toHttpHandler(it))
            }.let {
                HttpServer.create()
                    .host(apiConfig.host)
                    .port(apiConfig.port)
                    .handle(it)
                    .bindNow()
                    .onDispose()
                    .block()
            }
    }.onFailure {
        log("Couldn't start because of: $it")
    }

}

fun makeRoutes(genericRepo: Repo,
               log: Log,
               sqlClient: ReactorSqlClient,
               service: Service,
               serdes: Serdes,
               transformers: Transformers): RouterFunction<ServerResponse> {

    val personFindOneService =
        service.findOne(genericRepo.findOne(sqlClient, PersonTable, log),
                        serdes.person.serialize)
    val dogFindOneService =
        service.findOne(genericRepo.findOne(sqlClient, DogTable, log),
                        serdes.dog.serialize)

    val personFindAllService =
        service.findAll(genericRepo.findAll(sqlClient, PersonTable, log),
                        serdes.person.serialize)
    val dogFindAllService =
        service.findAll(genericRepo.findAll(sqlClient, DogTable, log),
                        serdes.dog.serialize)

    val transformedPersonFindAllService =
        service.findAllTransformed(genericRepo.findAll(sqlClient, DogTable, log),
                                   serdes.transformedPerson.serialize,
                                   transformers.dogToPerson)
    val transformedDogFindAllService =
        service.findAllTransformed(genericRepo.findAll(sqlClient, PersonTable, log),
                                   serdes.transformedDog.serialize,
                                   transformers.personToDog)

    val personCreateService =
        service.create(genericRepo.create(sqlClient, log),
                       serdes.person.deserialize)

    val dogCreateService =
        service.create(genericRepo.create(sqlClient, log),
                       serdes.dog.deserialize)

    return getRoutes(log,
                     personFindOneService,
                     personFindAllService,
                     transformedPersonFindAllService,
                     personCreateService,
                     personBasePath)
        .and(getRoutes(log,
                       dogFindOneService,
                       dogFindAllService,
                       transformedDogFindAllService,
                       dogCreateService,
                       dogBasePath))
}