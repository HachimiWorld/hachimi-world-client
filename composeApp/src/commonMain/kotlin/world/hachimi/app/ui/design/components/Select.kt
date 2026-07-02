package world.hachimi.app.ui.design.components

import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.KeyboardArrowDown
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.compositeOver
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import world.hachimi.app.ui.design.HachimiTheme
import world.hachimi.app.ui.theme.PreviewTheme

enum class SelectStyle {
    /** Surface fill + outline border. Default for settings and forms. */
    Outlined,
    /** Value text + chevron only, for dense list rows. */
    Minimal,
}

private val SelectShape = RoundedCornerShape(10.dp)
private val SelectPadding = PaddingValues(horizontal = 10.dp, vertical = 5.dp)
private val SelectTextStyle = TextStyle(
    fontWeight = FontWeight.Medium,
    fontSize = 13.sp,
    lineHeight = 18.sp,
)
private const val AnimationDurationMs = 200

/**
 * A single-select dropdown paired with [DropdownMenu].
 */
@Composable
fun <T> Select(
    value: T,
    onValueChange: (T) -> Unit,
    options: List<T>,
    modifier: Modifier = Modifier,
    enabled: Boolean = true,
    style: SelectStyle = SelectStyle.Outlined,
    equals: (T, T) -> Boolean = { a, b -> a == b },
    expandIconContentDescription: String? = null,
    label: @Composable (T) -> Unit,
) {
    val colorScheme = HachimiTheme.colorScheme
    var expanded by remember { mutableStateOf(false) }

    val borderColor by animateColorAsState(
        targetValue = when {
            !enabled -> colorScheme.outline.copy(alpha = 0.5f)
            expanded -> colorScheme.primary.copy(alpha = 0.45f)
            else -> colorScheme.outline
        },
        animationSpec = tween(AnimationDurationMs),
        label = "select_border",
    )
    val chevronRotation by animateFloatAsState(
        targetValue = if (expanded) 180f else 0f,
        animationSpec = tween(AnimationDurationMs),
        label = "select_chevron",
    )
    val contentAlpha = if (enabled) 1f else 0.38f

    Box(modifier.widthIn(max = 220.dp)) {
        when (style) {
            SelectStyle.Outlined -> {
                val containerColor = colorScheme.surface
                    .compositeOver(colorScheme.background)
                    .copy(alpha = contentAlpha)
                Surface(
                    shape = SelectShape,
                    color = containerColor,
                    contentColor = colorScheme.onSurface.copy(alpha = contentAlpha),
                    modifier = Modifier
                        .border(1.dp, borderColor, SelectShape)
                        .clip(SelectShape)
                        .clickable(
                            enabled = enabled,
                            role = Role.DropdownList,
                            onClick = { expanded = true },
                        ),
                ) {
                    SelectTriggerContent(
                        label = label,
                        value = value,
                        chevronRotation = chevronRotation,
                        expandIconContentDescription = expandIconContentDescription,
                        chevronTint = colorScheme.onSurfaceVariant.copy(alpha = contentAlpha),
                    )
                }
            }

            SelectStyle.Minimal -> {
                Row(
                    modifier = Modifier
                        .clip(SelectShape)
                        .clickable(
                            enabled = enabled,
                            role = Role.DropdownList,
                            onClick = { expanded = true },
                        )
                        .padding(SelectPadding),
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    CompositionLocalProvider(
                        LocalContentColor provides colorScheme.onSurfaceVariant.copy(alpha = contentAlpha),
                        LocalTextStyle provides SelectTextStyle,
                    ) {
                        Box(Modifier.weight(1f, fill = false)) {
                            label(value)
                        }
                        Spacer(Modifier.size(2.dp))
                        Icon(
                            imageVector = Icons.Default.KeyboardArrowDown,
                            contentDescription = expandIconContentDescription,
                            modifier = Modifier
                                .size(16.dp)
                                .rotate(chevronRotation),
                            tint = colorScheme.onSurfaceVariant.copy(alpha = contentAlpha * 0.8f),
                        )
                    }
                }
            }
        }

        DropdownMenu(
            expanded = expanded,
            onDismissRequest = { expanded = false },
        ) {
            options.forEach { option ->
                DropdownMenuItem(
                    text = { label(option) },
                    onClick = {
                        onValueChange(option)
                        expanded = false
                    },
                )
            }
        }
    }
}

@Composable
private fun <T> SelectTriggerContent(
    label: @Composable (T) -> Unit,
    value: T,
    chevronRotation: Float,
    expandIconContentDescription: String?,
    chevronTint: Color,
) {
    Row(
        modifier = Modifier.padding(SelectPadding),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        CompositionLocalProvider(
            LocalTextStyle provides SelectTextStyle,
        ) {
            Box(Modifier.weight(1f, fill = false)) {
                label(value)
            }
            Spacer(Modifier.size(4.dp))
            Icon(
                imageVector = Icons.Default.KeyboardArrowDown,
                contentDescription = expandIconContentDescription,
                modifier = Modifier
                    .size(16.dp)
                    .rotate(chevronRotation),
                tint = chevronTint,
            )
        }
    }
}

@Preview
@Composable
private fun PreviewSelectOutlined() {
    PreviewTheme(background = true) {
        var value by remember { mutableStateOf("system") }
        Select(
            value = value,
            onValueChange = { value = it },
            options = listOf("system", "on", "off"),
            style = SelectStyle.Outlined,
            label = { option ->
                Text(
                    when (option) {
                        "system" -> "跟随系统"
                        "on" -> "开启"
                        else -> "关闭"
                    },
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                )
            },
        )
    }
}

@Preview
@Composable
private fun PreviewSelectMinimal() {
    PreviewTheme(background = true) {
        var value by remember { mutableStateOf("system") }
        Select(
            value = value,
            onValueChange = { value = it },
            options = listOf("system", "on", "off"),
            style = SelectStyle.Minimal,
            label = { option ->
                Text(
                    when (option) {
                        "system" -> "跟随系统"
                        "on" -> "开启"
                        else -> "关闭"
                    },
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                )
            },
        )
    }
}