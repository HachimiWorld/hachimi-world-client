package world.hachimi.app.ui.root.component

import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.SubcomposeLayout
import androidx.compose.ui.tooling.preview.Devices
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import world.hachimi.app.ui.theme.PreviewTheme
import world.hachimi.app.util.WindowSize

@Composable
fun ExpandedScaffoldLayout(
    appbar: @Composable () -> Unit,
    navigation: @Composable () -> Unit,
    footerPlayer: @Composable () -> Unit,
    content: @Composable (contentPadding: PaddingValues) -> Unit,
    modifier: Modifier = Modifier
) {
    SubcomposeLayout(modifier) { constraints ->
        val appbarPlaceable = subcompose(Slots.AppBar, appbar).first()
            .measure(constraints.copy(minHeight = 0, minWidth = 0))
        val compactMode = constraints.maxWidth < WindowSize.MEDIUM.roundToPx()
        val (navigationPlaceable, footerPlayerPlaceable) = if (compactMode) {
            val footerPlayerPlaceable = subcompose(Slots.FooterPlayer, footerPlayer).first()
                .measure(constraints.copy(minHeight = 0, minWidth = 0))
            val navigationPlaceable = subcompose(Slots.Navigation, navigation).first()
                .measure(constraints.copy(
                    minHeight = 0, minWidth = 0,
                    maxHeight = constraints.maxHeight - footerPlayerPlaceable.height - appbarPlaceable.height
                ))
            navigationPlaceable to footerPlayerPlaceable
        } else {
            val navigationPlaceable = subcompose(Slots.Navigation, navigation).first()
                .measure(constraints.copy(
                    minHeight = 0, minWidth = 0,
                    maxHeight = constraints.maxHeight - appbarPlaceable.height
                ))
            val footerPlayerPlaceable = subcompose(Slots.FooterPlayer, footerPlayer).first()
                .measure(constraints.copy(
                    minHeight = 0, minWidth = 0,
                    maxWidth = constraints.maxWidth - navigationPlaceable.width
                ))
            navigationPlaceable to footerPlayerPlaceable
        }

        val contentPlaceable = subcompose(Slots.Content) {
            content(PaddingValues(
                top = appbarPlaceable.height.toDp(),
                start = navigationPlaceable.width.toDp(),
                bottom = footerPlayerPlaceable.height.toDp()
            ))
        }.first().measure(constraints)

        layout(constraints.maxWidth, constraints.maxHeight) {
            contentPlaceable.place(0, 0)
            appbarPlaceable.place(0, 0)
            navigationPlaceable.place(0, appbarPlaceable.height)
            if (compactMode) {
                footerPlayerPlaceable.place(0, constraints.maxHeight - footerPlayerPlaceable.height)
            } else {
                footerPlayerPlaceable.place(navigationPlaceable.width, constraints.maxHeight - footerPlayerPlaceable.height)
            }
        }
    }
}

private enum class Slots {
    AppBar, Navigation, FooterPlayer, Content
}

@Preview(device = Devices.DESKTOP)
@Preview(device = Devices.PIXEL_TABLET, showSystemUi = true)
@Preview(widthDp = 1600, heightDp = 720)
@Preview(widthDp = 800, heightDp = 720)
@Composable
private fun Preview() {
    PreviewTheme(background = true) {
        ExpandedScaffoldLayout(
            appbar = {
                Box(Modifier.height(100.dp).fillMaxWidth().border(1.dp, Color.Red))
            },
            navigation = {
                Box(Modifier.width(300.dp).fillMaxHeight().border(1.dp, Color.Blue))
            },
            footerPlayer = {
                Box(Modifier.height(200.dp).fillMaxWidth().border(1.dp, Color.Yellow))
            },
            content = {
                Box(Modifier.fillMaxSize().border(1.dp, Color.Black).padding(it).border(1.dp, Color.Gray))
            }
        )
    }
}