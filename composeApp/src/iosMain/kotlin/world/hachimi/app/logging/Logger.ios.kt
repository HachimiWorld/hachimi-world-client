package world.hachimi.app.logging

import platform.Foundation.NSLog

@Suppress("NOTHING_TO_INLINE")
actual object Logger {
    actual inline fun e(tag: String, message: String, throwable: Throwable?) {
        NSLog("[ERROR]($tag) $message ${throwable?.stackTraceToString()}")
    }

    actual inline fun w(tag: String, message: String, throwable: Throwable?) {
        NSLog("[WARNING]($tag) $message ${throwable?.stackTraceToString()}")
    }

    actual inline fun d(tag: String, message: String) {
        NSLog("[DEBUG]($tag) $message")
    }

    actual inline fun i(tag: String, message: String) {
        NSLog("[INFO]($tag) $message")
    }
}