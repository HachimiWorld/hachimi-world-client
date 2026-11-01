package world.hachimi.app.util

import world.hachimi.app.util.DiffOp.Kind.ADDED
import world.hachimi.app.util.DiffOp.Kind.REMOVED
import world.hachimi.app.util.DiffOp.Kind.SAME
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse

class SequenceDiffTest {
    private fun diff(before: List<String>, after: List<String>, maxCells: Long = 4_000_000L) =
        diffSequence(before, after, maxCells) { it }.map { it.kind to it.value }

    @Test
    fun identicalHasNoChanges() {
        val ops = diffSequence(listOf("a", "b"), listOf("a", "b")) { it }
        assertFalse(ops.hasChanges())
    }

    @Test
    fun changedLineIsRemovedThenAdded() {
        assertEquals(
            listOf(SAME to "a", REMOVED to "b", ADDED to "B", SAME to "c"),
            diff(listOf("a", "b", "c"), listOf("a", "B", "c"))
        )
    }

    @Test
    fun insertionAndDeletion() {
        assertEquals(
            listOf(REMOVED to "a", SAME to "b", SAME to "c", ADDED to "d"),
            diff(listOf("a", "b", "c"), listOf("b", "c", "d"))
        )
    }

    @Test
    fun emptySides() {
        assertEquals(listOf(ADDED to "a"), diff(emptyList(), listOf("a")))
        assertEquals(listOf(REMOVED to "a"), diff(listOf("a"), emptyList()))
    }

    @Test
    fun largeMiddleFallsBackToReplace() {
        assertEquals(
            listOf(SAME to "x", REMOVED to "a", REMOVED to "b", ADDED to "b", ADDED to "c", SAME to "y"),
            diff(listOf("x", "a", "b", "y"), listOf("x", "b", "c", "y"), maxCells = 1)
        )
    }
}
