package world.hachimi.app.util

/**
 * One entry of a sequence diff.
 *
 * @since 261016
 */
data class DiffOp<T>(val kind: Kind, val value: T) {
    enum class Kind { SAME, REMOVED, ADDED }
}

/**
 * Diffs two sequences with LCS, matching elements by [key].
 *
 * Within a changed run, removals come before additions. Inputs whose changed middle exceeds
 * [maxCells] LCS cells fall back to "remove all, add all" for that middle.
 *
 * @since 261016
 */
fun <T, K> diffSequence(
    before: List<T>,
    after: List<T>,
    maxCells: Long = 4_000_000L,
    key: (T) -> K,
): List<DiffOp<T>> {
    var prefix = 0
    while (prefix < before.size && prefix < after.size && key(before[prefix]) == key(after[prefix])) prefix++
    var suffix = 0
    while (
        suffix < before.size - prefix && suffix < after.size - prefix &&
        key(before[before.size - 1 - suffix]) == key(after[after.size - 1 - suffix])
    ) suffix++

    val a = before.subList(prefix, before.size - suffix)
    val b = after.subList(prefix, after.size - suffix)

    val result = ArrayList<DiffOp<T>>(before.size + after.size)
    for (i in 0 until prefix) result.add(DiffOp(DiffOp.Kind.SAME, after[i]))

    if (a.size.toLong() * b.size.toLong() > maxCells) {
        a.forEach { result.add(DiffOp(DiffOp.Kind.REMOVED, it)) }
        b.forEach { result.add(DiffOp(DiffOp.Kind.ADDED, it)) }
    } else {
        val ak = a.map(key)
        val bk = b.map(key)
        // lcs[i][j] = LCS length of a[i..] and b[j..]
        val width = b.size + 1
        val lcs = IntArray((a.size + 1) * width)
        for (i in a.size - 1 downTo 0) {
            for (j in b.size - 1 downTo 0) {
                lcs[i * width + j] = if (ak[i] == bk[j]) {
                    lcs[(i + 1) * width + j + 1] + 1
                } else {
                    maxOf(lcs[(i + 1) * width + j], lcs[i * width + j + 1])
                }
            }
        }
        var i = 0
        var j = 0
        val removed = ArrayList<T>()
        val added = ArrayList<T>()
        fun flush() {
            removed.forEach { result.add(DiffOp(DiffOp.Kind.REMOVED, it)) }
            added.forEach { result.add(DiffOp(DiffOp.Kind.ADDED, it)) }
            removed.clear()
            added.clear()
        }
        while (i < a.size || j < b.size) {
            when {
                i < a.size && j < b.size && ak[i] == bk[j] -> {
                    flush()
                    result.add(DiffOp(DiffOp.Kind.SAME, b[j]))
                    i++; j++
                }
                j >= b.size || (i < a.size && lcs[(i + 1) * width + j] >= lcs[i * width + j + 1]) -> {
                    removed.add(a[i]); i++
                }
                else -> {
                    added.add(b[j]); j++
                }
            }
        }
        flush()
    }

    for (i in after.size - suffix until after.size) result.add(DiffOp(DiffOp.Kind.SAME, after[i]))
    return result
}

/** @since 261016 */
fun <T> List<DiffOp<T>>.hasChanges(): Boolean = any { it.kind != DiffOp.Kind.SAME }
