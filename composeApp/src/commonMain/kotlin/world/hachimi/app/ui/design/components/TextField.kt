package world.hachimi.app.ui.design.components

import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.defaultMinSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Shape
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.layout.Layout
import androidx.compose.ui.text.TextLayoutResult
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import world.hachimi.app.ui.design.HachimiTheme
import world.hachimi.app.util.singleLined

private val textFieldStyle = TextStyle(
    fontWeight = FontWeight.Normal,
    fontSize = 16.sp,
    lineHeight = 24.sp
)

private val supportingTextStyle = TextStyle(
    fontWeight = FontWeight.Normal,
    fontSize = 12.sp,
    lineHeight = 16.sp
)

@Composable
fun TextField(
    value: String,
    onValueChange: (String) -> Unit,
    modifier: Modifier = Modifier,
    shape: Shape = RoundedCornerShape(8.dp),
    contentPadding: PaddingValues = PaddingValues(horizontal = 12.dp, vertical = 8.dp),
    placeholder: @Composable (() -> Unit)? = null,
    prefix: @Composable (() -> Unit)? = null,
    postfix: @Composable (() -> Unit)? = null,
    supportingText: @Composable (() -> Unit)? = null,
    leadingIcon: @Composable (() -> Unit)? = null,
    trailingIcon: @Composable (() -> Unit)? = null,
    enabled: Boolean = true,
    readOnly: Boolean = false,
    textStyle: TextStyle = textFieldStyle.copy(LocalContentColor.current),
    keyboardOptions: KeyboardOptions = KeyboardOptions.Default,
    keyboardActions: KeyboardActions = KeyboardActions.Default,
    singleLine: Boolean = false,
    minLines: Int = 1,
    maxLines: Int = if (singleLine) 1 else Int.MAX_VALUE,
    visualTransformation: VisualTransformation = VisualTransformation.None,
    onTextLayout: (TextLayoutResult) -> Unit = {},
    interactionSource: MutableInteractionSource? = null,
    cursorBrush: SolidColor = SolidColor(LocalContentColor.current),
) {
    BasicTextField(
        value = value,
        onValueChange = {
            onValueChange(if (singleLine) it.singleLined() else it)
        },
        modifier = modifier.defaultMinSize(minHeight = 48.dp),
        enabled = enabled,
        readOnly = readOnly,
        textStyle = textStyle,
        keyboardOptions = keyboardOptions,
        keyboardActions = keyboardActions,
        singleLine = singleLine,
        maxLines = maxLines,
        minLines = minLines,
        visualTransformation = visualTransformation,
        onTextLayout = onTextLayout,
        interactionSource = interactionSource,
        cursorBrush = cursorBrush,
        decorationBox = { innerTextField ->
            Layout(
                content = {
                    Surface(
                        shape = shape,
                        color = HachimiTheme.colorScheme.background
                    ) {
                        Row(
                            modifier = Modifier.padding(contentPadding),
                            verticalAlignment = Alignment.CenterVertically,
                        ) {
                            CompositionLocalProvider(
                                LocalContentColor provides HachimiTheme.colorScheme.onSurfaceVariant,
                                LocalTextStyle provides textFieldStyle
                            ) {
                                prefix?.invoke()
                                leadingIcon?.let {
                                    Box(Modifier.padding(end = 12.dp)) {
                                        it()
                                    }
                                }
                                Box(
                                    modifier = Modifier.weight(1f),
                                    contentAlignment = Alignment.CenterStart
                                ) {
                                    innerTextField()
                                    if (value.isEmpty()) {
                                        Box(
                                            modifier = Modifier.align(
                                                if (maxLines == 1) Alignment.CenterStart
                                                else Alignment.TopStart
                                            )
                                        ) {
                                            placeholder?.invoke()
                                        }
                                    }
                                }
                                trailingIcon?.let {
                                    Box(Modifier.padding(start = 12.dp)) {
                                        it()
                                    }
                                }
                                postfix?.invoke()
                            }
                        }
                    }
                    CompositionLocalProvider(LocalTextStyle provides supportingTextStyle) {
                        supportingText?.let {
                            Box(Modifier.padding(top = 4.dp), propagateMinConstraints = true) {
                                it()
                            }
                        }
                    }
                }
            ) { measureables, constraints ->
                val field = measureables[0]
                val supporting = measureables.getOrNull(1)
                val fieldPlaceable = field.measure(constraints)
                val supportingPlaceable = supporting?.measure(
                    constraints.copy(
                        minHeight = 0,
                        maxWidth = fieldPlaceable.width
                    )
                )

                layout(
                    width = fieldPlaceable.width,
                    height = fieldPlaceable.height + (supportingPlaceable?.height ?: 0)
                ) {
                    fieldPlaceable.place(0, 0)
                    supportingPlaceable?.place(0, fieldPlaceable.height)
                }
            }
        }
    )
}