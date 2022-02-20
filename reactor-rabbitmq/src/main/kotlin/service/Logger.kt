package service

import java.util.*

enum class LogType(val str: String) {
    WARN("Warn"),
    INFO("Info"),
    ERROR("Error")
}

typealias Log = (logType: LogType, message: String) -> Unit

object Loggers {
    val log: Log = { logType, message ->
        println("${logType.str} -- ${Date()} -- $message")
    }
}