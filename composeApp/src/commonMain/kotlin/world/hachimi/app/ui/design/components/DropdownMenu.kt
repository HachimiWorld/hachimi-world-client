package world.hachimi.app.ui.design.components

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.MutableTransitionState
import androidx.compose.foundation.ScrollState
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.IntrinsicSize
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.defaultMinSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.sizeIn
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.compositeOver
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.DpOffset
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Popup
import androidx.compose.ui.window.PopupProperties
import soup.compose.material.motion.animation.materialSharedAxisYIn
import soup.compose.material.motion.animation.materialSharedAxisYOut
import soup.compose.material.motion.animation.rememberSlideDistance
import world.hachimi.app.ui.design.HachimiTheme

private val DropdownMenuShape = RoundedCornerShape(16.dp)
private val DropdownMenuVerticalPadding = 8.dp
private val DropdownMenuItemMinWidth = 112.dp
private val DropdownMenuItemMaxWidth = 280.dp
private val DropdownMenuItemMinHeight = 40.dp
private val DropdownMenuItemHorizontalPadding = 12.dp
private val DropdownMenuItemLeadingIconMinWidth = 24.dp

private val dropdownMenuItemTextStyle = TextStyle(
    fontWeight = FontWeight.Medium,
    fontSize = 14.sp,
    lineHeight = 20.sp,
)

/**
 * A dropdown menu following the Hachimi design style.
 *
 * Drop-in replacement for Material3's [androidx.compose.material3.DropdownMenu].
 * Place it inside a [Box] next to the anchor element. The menu positions itself
 * relative to the parent layout, just like the Material3 version.
 *
 * @param expanded whether the menu is expanded
 * @param onDismissRequest called when the user requests to dismiss the menu
 * @param modifier [Modifier] for the menu content column
 * @param offset [DpOffset] from the original position of the menu
 * @param scrollState a [ScrollState] used by the menu's content for items vertical scrolling
 * @param properties [PopupProperties] for further customization of this popup's behavior
 * @param content the content of this dropdown menu, typically [DropdownMenuItem]
 */
@Composable
fun DropdownMenu(
    expanded: Boolean,
    onDismissRequest: () -> Unit,
    modifier: Modifier = Modifier,
    offset: DpOffset = DpOffset(0.dp, 0.dp),
    scrollState: ScrollState = rememberScrollState(),
    properties: PopupProperties = PopupProperties(focusable = true),
    content: @Composable ColumnScope.() -> Unit,
) {
    val expandedState = remember { MutableTransitionState(false) }
    expandedState.targetState = expanded

    if (expandedState.targetState || expandedState.currentState) {
        Popup(
            onDismissRequest = onDismissRequest,
            properties = properties,
        ) {
            val slideDistance = rememberSlideDistance()
            AnimatedVisibility(
                visibleState = expandedState,
                enter = materialSharedAxisYIn(forward = true, slideDistance = slideDistance),
                exit = materialSharedAxisYOut(forward = false, slideDistance = slideDistance),
            ) {
                val containerColor = HachimiTheme.colorScheme.surface
                    .compositeOver(HachimiTheme.colorScheme.background)
                ElevatedCard(
                    shape = DropdownMenuShape,
                    color = containerColor,
                ) {
                    Column(
                        modifier = modifier
                            .padding(vertical = DropdownMenuVerticalPadding)
                            .width(IntrinsicSize.Max)
                            .verticalScroll(scrollState),
                        content = content,
                    )
                }
            }
        }
    }
}

/**
 * A dropdown menu item following the Hachimi design style.
 *
 * Drop-in replacement for Material3's [androidx.compose.material3.DropdownMenuItem].
 *
 * @param text text of the menu item
 * @param onClick called when this menu item is clicked
 * @param modifier the [Modifier] to be applied to this menu item
 * @param leadingIcon optional leading icon
 * @param trailingIcon optional trailing icon
 * @param enabled controls the enabled state of this menu item
 * @param contentPadding the padding applied to the content of this menu item
 * @param interactionSource an optional hoisted [MutableInteractionSource]
 */
@Composable
fun DropdownMenuItem(
    text: @Composable () -> Unit,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    leadingIcon: @Composable (() -> Unit)? = null,
    trailingIcon: @Composable (() -> Unit)? = null,
    enabled: Boolean = true,
    contentPadding: PaddingValues = PaddingValues(horizontal = DropdownMenuItemHorizontalPadding, vertical = 0.dp),
    interactionSource: MutableInteractionSource? = null,
) {
    val contentColor = if (enabled) HachimiTheme.colorScheme.onSurface else HachimiTheme.colorScheme.onSurface.copy(0.38f)

    Row(
        modifier = modifier
            .clickable(
                enabled = enabled,
                onClick = onClick,
                interactionSource = interactionSource,
            )
            .fillMaxWidth()
            .sizeIn(
                minWidth = DropdownMenuItemMinWidth,
                maxWidth = DropdownMenuItemMaxWidth,
                minHeight = DropdownMenuItemMinHeight,
            )
            .padding(contentPadding),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        CompositionLocalProvider(
            LocalTextStyle provides dropdownMenuItemTextStyle,
            LocalContentColor provides contentColor,
        ) {
            if (leadingIcon != null) {
                Box(Modifier.defaultMinSize(minWidth = DropdownMenuItemLeadingIconMinWidth)) {
                    leadingIcon()
                }
            }
            Box(
                Modifier
                    .weight(1f)
                    .padding(
                        start = if (leadingIcon != null) DropdownMenuItemHorizontalPadding else 0.dp,
                        end = if (trailingIcon != null) DropdownMenuItemHorizontalPadding else 0.dp,
                    )
            ) {
                text()
            }
            if (trailingIcon != null) {
                Box(Modifier.defaultMinSize(minWidth = DropdownMenuItemLeadingIconMinWidth)) {
                    trailingIcon()
                }
            }
        }
    }
}
