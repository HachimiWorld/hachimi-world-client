package world.hachimi.app.util

import androidx.compose.runtime.Composable
import androidx.compose.runtime.Stable
import androidx.compose.runtime.remember
import hachimiworld.composeapp.generated.resources.Res
import hachimiworld.composeapp.generated.resources.date_after_tomorrow
import hachimiworld.composeapp.generated.resources.date_before_yesterday
import hachimiworld.composeapp.generated.resources.date_today
import hachimiworld.composeapp.generated.resources.date_tomorrow
import hachimiworld.composeapp.generated.resources.date_yesterday
import hachimiworld.composeapp.generated.resources.time_ago_days
import hachimiworld.composeapp.generated.resources.time_ago_days_hours
import hachimiworld.composeapp.generated.resources.time_ago_hours
import hachimiworld.composeapp.generated.resources.time_ago_hours_minutes
import hachimiworld.composeapp.generated.resources.time_ago_minutes
import hachimiworld.composeapp.generated.resources.time_ago_minutes_seconds
import hachimiworld.composeapp.generated.resources.time_ago_seconds
import kotlinx.datetime.LocalDateTime
import kotlinx.datetime.TimeZone
import kotlinx.datetime.format
import kotlinx.datetime.format.DateTimeFormat
import kotlinx.datetime.format.char
import kotlinx.datetime.toLocalDateTime
import org.jetbrains.compose.resources.pluralStringResource
import org.jetbrains.compose.resources.stringResource
import kotlin.math.abs
import kotlin.time.Clock
import kotlin.time.Duration
import kotlin.time.Instant

@Composable
fun formatTime(instant: Instant, distance: Boolean = false, thresholdDay: Int = 7, precise: Boolean = true, fullFormat: DateTimeFormat<LocalDateTime> = LocalDateTime.Formats.ISO): String {
    return if (distance) {
        formatDistance(time = instant, thresholdDay = thresholdDay, precise = precise, fullFormat = fullFormat)
    } else {
        remember(instant, fullFormat) {
            fullFormat.format(instant.toLocalDateTime(TimeZone.currentSystemDefault()))
        }
    }
}

@Composable
fun formatDaysDistance(days: Int): String? {
    val res = when (days) {
        -2 -> Res.string.date_after_tomorrow
        -1 -> Res.string.date_tomorrow
        0 -> Res.string.date_today
        1 -> Res.string.date_yesterday
        2 -> Res.string.date_before_yesterday
        else -> null
    }
    return res?.let { stringResource(it) }
}

/**
 * Convert duration to format like: 10 分钟 20 秒前
 */
@Composable
fun formatDistance(distance: Duration, precise: Boolean): String {
    if (distance.isNegative()) {
        val secsAbs = abs(distance.inWholeSeconds).toInt()
        val minsAbs = abs(distance.inWholeMinutes).toInt()
        val hoursAbs = abs(distance.inWholeHours).toInt()
        val daysAbs = abs(distance.inWholeDays).toInt()

        if (secsAbs < 60) {
            return pluralStringResource(Res.plurals.time_ago_seconds, secsAbs, secsAbs)
        }
        if (minsAbs < 60) {
            return if (precise) {
                stringResource(Res.string.time_ago_minutes_seconds, minsAbs, secsAbs % 60)
            } else {
                pluralStringResource(Res.plurals.time_ago_minutes, minsAbs, minsAbs)
            }
        }
        if (hoursAbs < 24) {
            return if (precise) {
                stringResource(Res.string.time_ago_hours_minutes, hoursAbs, minsAbs % 60)
            } else {
                pluralStringResource(Res.plurals.time_ago_hours, hoursAbs, hoursAbs)
            }
        }
        return if (precise) {
            stringResource(Res.string.time_ago_days_hours, daysAbs, hoursAbs % 24)
        } else {
            pluralStringResource(Res.plurals.time_ago_days, daysAbs, daysAbs)
        }
    } else {
        // TODO: 完善往后数
        // n 分钟前/后
        // n 小时前/后
        // n 天前/后
        return "Unsupported"
    }
}

@Composable
fun formatDistance(
    time: Instant,
    precise: Boolean,
    thresholdDay: Int = 7,
    now: Instant = remember(time) { Clock.System.now() },
    timeZone: TimeZone = TimeZone.currentSystemDefault(),
    fullFormat: DateTimeFormat<LocalDateTime> = LocalDateTime.Formats.ISO
): String {
    val distance = time - now
    return if (abs(distance.inWholeDays) <= thresholdDay) {
        formatDistance(distance, precise)
    } else {
        remember(time, timeZone, fullFormat) { time.toLocalDateTime(timeZone).format(fullFormat) }
    }
}

val LocalDateTime.Formats.YMD get() = LocalDateTime.Format {
    yearTwoDigits(1960)
    char('-')
    monthNumber()
    char('-')
    day()
}

/**
 * 25-10-14 22:29:28
 */
val LocalDateTime.Formats.YMDHMS get() = LocalDateTime.Format {
    yearTwoDigits(1960)
    char('-')
    monthNumber()
    char('-')
    day()
    char(' ')
    hour()
    char(':')
    minute()
    char(':')
    second()
}

/**
 * 2025-10-14 22:28
 */
val LocalDateTime.Formats.YMDHM get() = LocalDateTime.Format {
    year()
    char('-')
    monthNumber()
    char('-')
    day()
    char(' ')
    hour()
    char(':')
    minute()
}

val LocalDateTime.Formats.HMS get() = LocalDateTime.Format {
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
        val a = LocalDateTime.Formats.YMDHMS.format(startDateTime)
        val b = LocalDateTime.Formats.HMS.format(endDateTime)
        return "$a ~ $b"
    } else {
        val a = LocalDateTime.Formats.YMDHMS.format(startDateTime)
        val b = LocalDateTime.Formats.HMS.format(endDateTime)
        return "$a ~ $b"
    }
}

@Stable
fun formatSongDuration(duration: Duration): String {
    val seconds = duration.inWholeSeconds
    val minutesPart = seconds / 60
    val secondsPart = seconds % 60
    val padding = if (secondsPart < 10) "0" else ""
    return "${minutesPart}:${padding}$secondsPart"
}
