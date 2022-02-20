import java.util.*

typealias Log = (Any) -> Unit

val log: Log = { message: Any -> println("${Date()} -- $message") }