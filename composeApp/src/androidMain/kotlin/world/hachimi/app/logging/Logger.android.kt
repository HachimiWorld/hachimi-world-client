package world.hachimi.app.logging

import android.util.Log

@Suppress("NOTHING_TO_INLINE")
actual object Logger {

    actual inline fun e(tag: String, message: String, throwable: Throwable?) {
        if (throwable != null) {
            Log.e(tag, message, throwable)
        } else {
            Log.e(tag, message)
        }
    }

    actual inline fun w(tag: String, message: String, throwable: Throwable?) {
        if (throwable != null) {
            Log.w(tag, message, throwable)
        } else {
            Log.w(tag, message)
        }
    }

    actual inline fun d(tag: String, message: String) {
        Log.d(tag, message)
    }

    actual inline fun i(tag: String, message: String) {
        Log.i(tag, message)
    }
}