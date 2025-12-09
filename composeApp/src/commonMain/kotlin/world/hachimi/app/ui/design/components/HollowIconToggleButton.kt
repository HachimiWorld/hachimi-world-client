package world.hachimi.app.ui.design.components

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.selection.selectable
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.paint
import androidx.compose.ui.graphics.BlendMode
import androidx.compose.ui.graphics.ColorFilter
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.graphics.vector.VectorPath
import androidx.compose.ui.graphics.vector.rememberVectorPainter
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.role
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import world.hachimi.app.ui.design.HachimiTheme

@Composable
fun HollowIconToggleButton(
    selected: Boolean,
    onClick: () -> Unit,
    icon: ImageVector,
    contentDescription: String
) {
    val painter = rememberVectorPainter(
        remember(icon) {
            // The `BlendMode.Clear` is not working on my computer, so we use `Xor` instead. To build a hollow icon button.
            // This is a workaround to adjust the viewport size of the icon, so the Xor result fills the whole 28 dp bounds
            resizeImageVector(icon, 20.dp, 28.dp)
        }
    )

    val activeColor = HachimiTheme.colorScheme.onSurface
    val inactiveColor = LocalContentColor.current.copy(0.6f)

    Box(
        modifier = Modifier
            .size(28.dp)
            .clip(CircleShape)
            .paint(
                painter = painter,
                colorFilter =
                    if (selected) ColorFilter.tint(activeColor, blendMode = BlendMode.Xor)
                    else ColorFilter.tint(inactiveColor)
            )
            .selectable(selected = selected, onClick = onClick, role = Role.Tab)
            .semantics {
                this.contentDescription = contentDescription
                this.role = Role.Tab
            }
    )
}

/**
 * Scale the icon to `targetSize`, then resize to `targetViewPortSize` and align to center
 */
internal fun resizeImageVector(
    imageVector: ImageVector,
    targetSize: Dp,
    targetViewportSize: Dp
): ImageVector {
    val originalViewportSize = imageVector.viewportWidth
    val scale = targetSize.value / originalViewportSize
    val offset = (targetViewportSize.value - targetSize.value) / 2f

    return ImageVector.Builder(
        name = imageVector.name,
        defaultWidth = targetViewportSize,
        defaultHeight = targetViewportSize,
        viewportWidth = targetViewportSize.value,
        viewportHeight = targetViewportSize.value
    ).apply {
        addGroup(
            name = "scaled_and_centered_group",
            scaleX = scale,
            scaleY = scale,
            translationX = offset,
            translationY = offset
        )
        imageVector.root.forEach { groupNode ->
            if (groupNode is VectorPath) {
                addPath(
                    pathData = groupNode.pathData,
                    name = groupNode.name,
                    fill = groupNode.fill,
                    stroke = groupNode.stroke,
                    strokeLineWidth = groupNode.strokeLineWidth,
                    strokeLineCap = groupNode.strokeLineCap,
                    strokeLineJoin = groupNode.strokeLineJoin,
                    strokeLineMiter = groupNode.strokeLineMiter,
                    trimPathStart = groupNode.trimPathStart,
                    trimPathEnd = groupNode.trimPathEnd,
                    trimPathOffset = groupNode.trimPathOffset
                )
            }
        }
    }.build()
}