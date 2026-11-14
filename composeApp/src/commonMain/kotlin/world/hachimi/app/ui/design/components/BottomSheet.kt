package world.hachimi.app.ui.design.components

import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.SheetValue
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.compositeOver
import world.hachimi.app.ui.design.HachimiTheme

/**
 * A sheet that comes up from the bottom, for compact windows where a dialog would be cramped.
 *
 * @param confirmDismiss Asked before a swipe down or a tap outside closes it; return false to keep it open.
 * @since 261114
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ModalBottomSheet(
    onDismissRequest: () -> Unit,
    modifier: Modifier = Modifier,
    confirmDismiss: () -> Boolean = { true },
    content: @Composable ColumnScope.() -> Unit,
) {
    val state = rememberModalBottomSheetState(
        skipPartiallyExpanded = true,
        confirmValueChange = { it != SheetValue.Hidden || confirmDismiss() },
    )
    androidx.compose.material3.ModalBottomSheet(
        onDismissRequest = onDismissRequest,
        modifier = modifier,
        sheetState = state,
        containerColor = HachimiTheme.colorScheme.surface.compositeOver(HachimiTheme.colorScheme.background),
        contentColor = HachimiTheme.colorScheme.onSurface,
        content = content,
    )
}
