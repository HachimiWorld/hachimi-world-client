package world.hachimi.app.logging

import kotlin.time.Clock
import kotlin.time.ExperimentalTime

@OptIn(ExperimentalTime::class)
@Suppress("NOTHING_TO_INLINE")
actual object Logger {
    actual inline fun e(tag: String, message: String, throwable: Throwable?) {
        println("${Clock.System.now()}  ERROR [$tag] $message ${throwable?.stackTraceToString()}")
    }

    actual inline fun w(tag: String, message: String, throwable: Throwable?) {
        println("${Clock.System.now()}  WARNING [$tag] $message ${throwable?.stackTraceToString()}")
    }

    actual inline fun d(tag: String, message: String) {
        println("${Clock.System.now()}  DEBUG [$tag] $message")
    }

    actual inline fun i(tag: String, message: String) {
        println("${Clock.System.now()}  INFO [$tag] $message")
    }

}