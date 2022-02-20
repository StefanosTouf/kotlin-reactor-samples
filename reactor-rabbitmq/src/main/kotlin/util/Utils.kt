package util

import arrow.fx.coroutines.Resource
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.runBlocking
import main.flatMap

fun <A, B, C> Resource.Companion.zip3(e1: Resource<A>,
                                      e2: Resource<B>,
                                      e3: Resource<C>): Resource<Triple<A, B, C>> =
    e1.zip(e2, e3) { a, b, c -> Triple(a, b, c) }


fun main() = runBlocking {

    val f = flowOf(1,2).onEach { delay(1000) }


    flowOf(1, 2, 3, 4, 5, 6, 7).flatMap { f }.buffer(100).onEach { println(it) }.collect()
}


