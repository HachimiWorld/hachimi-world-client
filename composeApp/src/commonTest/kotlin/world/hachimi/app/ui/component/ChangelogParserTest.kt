package world.hachimi.app.ui.component

import kotlin.test.Test
import kotlin.test.assertEquals

class ChangelogParserTest {
    @Test
    fun parsesSectionsAndItems() {
        val changelog = """
            ## Features 新功能

            1. Support automatic updates. 支持自动更新。

            ## Fixes 修复

            - Fixed a crash
            A plain note
        """.trimIndent()

        assertEquals(
            listOf(
                ChangelogLine.Section("Features 新功能"),
                ChangelogLine.Item("Support automatic updates. 支持自动更新。"),
                ChangelogLine.Section("Fixes 修复"),
                ChangelogLine.Item("Fixed a crash"),
                ChangelogLine.Paragraph("A plain note"),
            ),
            parseChangelog(changelog),
        )
    }

    @Test
    fun keepsAnythingElseAsPlainText() {
        assertEquals(
            listOf(
                ChangelogLine.Paragraph("#123 修复了播放问题"),
                ChangelogLine.Paragraph("**加粗** 和 [链接](https://hachimi.world)"),
                ChangelogLine.Paragraph("1.4.0"),
            ),
            parseChangelog("#123 修复了播放问题\n\n**加粗** 和 [链接](https://hachimi.world)\n1.4.0"),
        )
        assertEquals(emptyList(), parseChangelog("  \n\n"))
    }
}
