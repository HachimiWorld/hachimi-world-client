package world.hachimi.app.logging

import org.slf4j.LoggerFactory

@Suppress("NOTHING_TO_INLINE")
actual object Logger {
    val logger = LoggerFactory.getLogger(Logger::class.java)!!

    actual inline fun e(
        tag: String,
        message: String,
        throwable: Throwable?
    ) {
        if (throwable != null) {
            logger.error("[$tag] $message", throwable)
        } else {
            logger.error("[$tag] $message")
        }
    }

    actual  inline fun w(tag: String, message: String, throwable: Throwable?) {
        if (throwable != null) {
            logger.warn("[$tag] $message", throwable)
        } else {
            logger.warn("[$tag] $message")
        }
    }

    actual inline fun d(tag: String, message: String) {
        logger.debug("[$tag] $message")
    }

    actual inline fun i(tag: String, message: String) {
        logger.info("[$tag] $message")
    }
}