package world.hachimi.app.util

import androidx.compose.runtime.Composable
import androidx.compose.runtime.Stable
import androidx.compose.runtime.remember
import kotlinx.datetime.*
import kotlinx.datetime.format
import kotlinx.datetime.format.char
import kotlinx.datetime.toLocalDateTime
import kotlin.math.abs
import kotlin.ranges.coerceIn
import kotlin.time.Duration
import kotlin.time.DurationUnit

@Composable
fun formatTime(instant: Instant, distance: Boolean = false, thresholdDay: Int = 7, precise: Boolean = true): String {
    return remember(instant, distance, thresholdDay, precise) {
        val now = Clock.System.now()
        if (distance) {
            formatDistance(time = instant, thresholdDay = thresholdDay, precise = precise, now = now)
        } else {
            val formatted = LocalDateTime.Formats.ISO.format(
                instant.toLocalDateTime(TimeZone.currentSystemDefault())
            )
            formatted
        }
    }
}

@Stable
fun formatDaysDistance(days: Int): String? {
    return when (days) {
        -2 -> "后天"
        -1 -> "明天"
        0 -> "今天"
        1 -> "昨天"
        2 -> "前天"
        else -> null
    }
}

@Stable
fun formatDuration(duration: Duration, precise: Boolean, minUnit: DurationUnit = DurationUnit.SECONDS, maxUnit: DurationUnit = DurationUnit.DAYS): String {
    val secsAbs = abs(duration.inWholeSeconds)
    val minsAbs = abs(duration.inWholeMinutes)
    val hoursAbs = abs(duration.inWholeHours)
    val daysAbs = abs(duration.inWholeDays)

    if (secsAbs == 0L) return "0 秒"

    val unit = if (secsAbs < 1) {
        DurationUnit.MILLISECONDS
    } else if (secsAbs < 60) {
        DurationUnit.SECONDS
    } else if (minsAbs < 60) {
        DurationUnit.MINUTES
    } else if (hoursAbs < 24) {
        DurationUnit.HOURS
    } else {
        DurationUnit.DAYS
    }

    val actualUnit = unit.coerceIn(minUnit, maxUnit)

    return when (actualUnit) {
        DurationUnit.NANOSECONDS, DurationUnit.MICROSECONDS, DurationUnit.MILLISECONDS, DurationUnit.SECONDS -> "$secsAbs 秒"
        DurationUnit.MINUTES -> if (precise) {
            val remainSecs = secsAbs % 60
            "$minsAbs 分 $remainSecs 秒种"
        } else {
            "$minsAbs 分钟"
        }
        DurationUnit.HOURS -> if (precise) {
            val remainMinutes = minsAbs % 60
            "$hoursAbs 小时 $remainMinutes 分钟"
        } else {
            "$hoursAbs 小时"
        }
        DurationUnit.DAYS -> if (precise) {
            val remainHours = hoursAbs % 24
            "$daysAbs 天 $remainHours 小时"
        } else {
            "$daysAbs 天"
        }

        else -> error("unreachable")
    }
}

/**
 * Convert duration to format like: 10 分钟 20 秒前
 */
@Stable
fun formatDistance(distance: Duration, precise: Boolean): String {
    if (distance.isNegative()) {
        val secsAbs = abs(distance.inWholeSeconds)
        val minsAbs = abs(distance.inWholeMinutes)
        val hoursAbs = abs(distance.inWholeHours)
        val daysAbs = abs(distance.inWholeDays)

        if (secsAbs < 60) {
            return "$secsAbs 秒前"
        }
        if (minsAbs < 60) {
            return if (precise) {
                val remainSecs = secsAbs % 60
                "$minsAbs 分钟 $remainSecs 秒前"
            } else {
                "$minsAbs 分钟前"
            }
        }
        if (hoursAbs < 24) {
            return if (precise) {
                val remainMinutes = minsAbs % 60
                "$hoursAbs 小时 $remainMinutes 分钟前"
            } else {
                "$hoursAbs 小时前"
            }
        }
        return if (precise) {
            val remainHours = hoursAbs % 24
            "$daysAbs 天 $remainHours 小时前"
        } else {
            "$daysAbs 天前"
        }
    } else {
        // TODO: 完善往后数
        // n 分钟前/后
        // n 小时前/后
        // n 天前/后
        return "Unsupported"
    }
}

@Stable
fun formatDistance(
    time: Instant,
    precise: Boolean,
    thresholdDay: Int = 7,
    now: Instant = Clock.System.now(),
    timeZone: TimeZone = TimeZone.currentSystemDefault()
): String {
    val distance = time - now
    return if (abs(distance.inWholeDays) <= thresholdDay) {
        formatDistance(distance, precise)
    } else {
        val local = time.toLocalDateTime(timeZone)
        local.format(LocalDateTime.Formats.ISO)
    }
}

private val ymdhms = LocalDateTime.Format {
    yearTwoDigits(1960)
    char('/')
    monthNumber()
    char('/')
    dayOfMonth()
    char('-')
    hour()
    char(':')
    minute()
    char(':')
    second()
}

private val hms = LocalDateTime.Format {
    hour()
    char(':')
    minute()
    char(':')
    second()
}

@Stable
fun formatDateRange(
    start: Instant,
    end: Instant,
    timeZone: TimeZone = TimeZone.currentSystemDefault()
): String {
    val startDateTime = start.toLocalDateTime(timeZone)
    val endDateTime = end.toLocalDateTime(timeZone)

    if (startDateTime.date == endDateTime.date) {
        val a = ymdhms.format(startDateTime)
        val b = hms.format(endDateTime)
        return "$a ~ $b"
    } else {
        val a = ymdhms.format(startDateTime)
        val b = hms.format(endDateTime)
        return "$a ~ $b"
    }
}

@Stable
fun formatSongDuration(duration: Duration): String {
    val seconds = duration.inWholeSeconds
    val minutesPart = seconds / 60
    val secondsPart = seconds % 60
    val padding = if (secondsPart < 10) "0" else ""
    return "${minutesPart}:${padding}$secondsPart)}"
}
