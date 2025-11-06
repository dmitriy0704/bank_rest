package docs.pro.corutines

import kotlin.concurrent.thread

fun main() {
    println("I am ${Thread.currentThread().name}")
    thread { println("I am a thread: ${Thread.currentThread().name}") }
}
