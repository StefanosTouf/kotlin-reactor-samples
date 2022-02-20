import com.sksamuel.hoplite.ConfigLoader

data class DBConfig(val host: String,
                    val dbName: String,
                    val username: String,
                    val password: String)

data class ApiConfig(val host: String, val port: Int)

data class Config(val database: DBConfig, val api: ApiConfig)

fun getConfig() =
    ConfigLoader()
        .loadConfig<Config>()
