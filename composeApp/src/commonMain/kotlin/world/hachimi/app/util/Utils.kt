package world.hachimi.app.util

import androidx.compose.runtime.Stable
import io.ktor.http.*


@Stable
fun formatBytes(bytes: Long): String {
    if (bytes == 0L) return "0 MB"
    return ((bytes.toFloat() / (1024 * 1024) * 10).toInt()).toString()
        .toCharArray()
        .toMutableList()
        .also {
            if (it.size < 2) {
                it.add(0, '0')
            }
            it.add(it.lastIndex, '.')
        }
        .joinToString("")
        .plus(" MB")
}

@Stable
fun formatCompactCount(value: Long): String {
    val abs = kotlin.math.abs(value)
    val sign = if (value < 0) "-" else ""

    fun formatScaled(divisor: Long, suffix: String): String {
        val scaledTimes10 = abs * 10 / divisor
        val whole = scaledTimes10 / 10
        val decimal = scaledTimes10 % 10
        return if (whole >= 10) {
            "${sign}${whole}$suffix"
        } else {
            if (decimal == 0L) "${sign}${whole}$suffix"
            else "${sign}${whole}.${decimal}$suffix"
        }
    }

    return when {
        abs < 1_000 -> value.toString()
        abs < 1_000_000 -> formatScaled(1_000, "k")
        abs < 1_000_000_000 -> formatScaled(1_000_000, "m")
        else -> formatScaled(1_000_000_000, "b")
    }
}

@Stable
fun isValidHttpsUrl(content: String): Boolean {
    try {
        val url = Url(content)
        if (url.protocolOrNull == URLProtocol.HTTPS)  {
            return true
        }
    } catch (_: URLParserException) {
        return false
    }
    return false
}

@Stable
fun validateEmailPattern(string: String): Boolean {
    return string.matches(Regex("^[a-zA-Z0-9._%+-]+@[a-zA-Z0-9.-]+\\.[a-zA-Z]{2,}$"))
}

@Stable
fun validatePasswordPattern(string: String): Boolean {
    return string.length >= 6
}

/**
 * TextField with `singleLine = true` could be bypassed by pasting multiline text.
 */
fun String.singleLined() = replace('\n', ' ').replace('\r', ' ')

/**
 * Parse pattern JM-ABCD-123 to `("ABCD", "123")`
 */
fun parseJmid(input: String): Pair<String, String>? {
    val displayIdPattern = "^(?:JM-)?([A-Z]{3,4})-?(\\d{3})$".toRegex()
    val matchResult = displayIdPattern.find(input)
    if (matchResult != null) {
        val part1 = matchResult.groupValues[1]
        val part2 = matchResult.groupValues[2]
        return part1 to part2
    }
    return null
}