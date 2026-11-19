package world.hachimi.app.ui.share

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.drawWithContent
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.layer.drawLayer
import androidx.compose.ui.graphics.rememberGraphicsLayer
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil3.compose.LocalPlatformContext
import hachimiworld.composeapp.generated.resources.Res
import hachimiworld.composeapp.generated.resources.common_close
import hachimiworld.composeapp.generated.resources.share_brand
import hachimiworld.composeapp.generated.resources.share_image_save_failed
import hachimiworld.composeapp.generated.resources.share_image_saved
import hachimiworld.composeapp.generated.resources.share_poster_hint
import hachimiworld.composeapp.generated.resources.share_poster_title
import hachimiworld.composeapp.generated.resources.share_save_image
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.launch
import org.jetbrains.compose.resources.stringResource
import org.koin.compose.koinInject
import qrcode.raw.QRCodeProcessor
import world.hachimi.app.model.GlobalStore
import world.hachimi.app.ui.design.HachimiTheme
import world.hachimi.app.ui.design.components.AccentButton
import world.hachimi.app.ui.design.components.AlertDialog
import world.hachimi.app.ui.design.components.CircularProgressIndicator
import world.hachimi.app.ui.design.components.Text
import world.hachimi.app.ui.design.components.TextButton
import world.hachimi.app.ui.message.components.RemoteImage
import world.hachimi.app.ui.theme.PreviewTheme

/** The song's page, as the poster's code points at it. @since 261119 */
internal fun songLink(jmid: String) = "https://hachimi.world/song/${jmid.lowercase()}"

/**
 * A card to keep or post elsewhere: the cover over it, the song, and a code that opens the song's page.
 *
 * @since 261119
 */
@Composable
internal fun SharePoster(
    title: String,
    author: String,
    coverUrl: String?,
    jmid: String,
    modifier: Modifier = Modifier,
) {
    Box(
        modifier
            .width(PosterWidth)
            .height(PosterHeight)
            .clip(RoundedCornerShape(20.dp))
            .background(Color(0xFF1B1B1B)),
    ) {
        RemoteImage(coverUrl, Modifier.fillMaxSize())
        // Dark enough at the bottom for white text over any cover
        Box(
            Modifier
                .fillMaxSize()
                .background(
                    Brush.verticalGradient(
                        0f to Color.Black.copy(alpha = 0.2f),
                        0.45f to Color.Black.copy(alpha = 0.5f),
                        1f to Color.Black.copy(alpha = 0.85f),
                    )
                )
        )
        Column(Modifier.fillMaxSize().padding(20.dp)) {
            Spacer(Modifier.weight(1f))
            RemoteImage(coverUrl, Modifier.size(112.dp).clip(RoundedCornerShape(14.dp)))
            Text(
                title,
                Modifier.padding(top = 14.dp),
                fontSize = 20.sp,
                fontWeight = FontWeight.SemiBold,
                color = Color.White,
                maxLines = 2,
                overflow = TextOverflow.Ellipsis,
            )
            Text(
                author,
                Modifier.padding(top = 4.dp),
                fontSize = 13.sp,
                color = Color.White.copy(alpha = 0.7f),
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
            )
            Spacer(Modifier.weight(1f))
            Row(verticalAlignment = Alignment.CenterVertically) {
                Box(
                    Modifier
                        .clip(RoundedCornerShape(10.dp))
                        .background(Color.White)
                        .padding(6.dp)
                ) {
                    QrCode(songLink(jmid), Modifier.size(64.dp))
                }
                Column(Modifier.padding(start = 12.dp)) {
                    Text(stringResource(Res.string.share_brand), fontSize = 14.sp, fontWeight = FontWeight.Medium, color = Color.White)
                    Text(
                        stringResource(Res.string.share_poster_hint),
                        Modifier.padding(top = 2.dp),
                        fontSize = 11.sp,
                        color = Color.White.copy(alpha = 0.7f),
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis,
                    )
                }
            }
        }
    }
}

/** The poster, and a button that saves it as a picture. @since 261119 */
@Composable
fun SharePosterDialog(
    onDismissRequest: () -> Unit,
    title: String,
    author: String,
    coverUrl: String?,
    jmid: String,
    global: GlobalStore = koinInject(),
) {
    val context = LocalPlatformContext.current
    val scope = rememberCoroutineScope()
    val layer = rememberGraphicsLayer()
    var saving by remember { mutableStateOf(false) }

    AlertDialog(
        onDismissRequest = onDismissRequest,
        title = { Text(stringResource(Res.string.share_poster_title)) },
        text = {
            // Records what it draws, so the picture saved is the card itself
            Box(Modifier.drawWithContent {
                layer.record { this@drawWithContent.drawContent() }
                drawLayer(layer)
            }) {
                SharePoster(title, author, coverUrl, jmid)
            }
        },
        confirmButton = {
            AccentButton(
                onClick = {
                    if (saving) return@AccentButton
                    saving = true
                    scope.launch {
                        try {
                            val bytes = encodePng(layer.toImageBitmap())
                            val saved = bytes != null && savePosterImage(context, bytes, "hachimi-${jmid.lowercase()}.png")
                            global.alert(if (saved) Res.string.share_image_saved else Res.string.share_image_save_failed)
                            if (saved) onDismissRequest()
                        } catch (e: CancellationException) {
                            throw e
                        } catch (e: Throwable) {
                            global.alert(Res.string.share_image_save_failed)
                        } finally {
                            saving = false
                        }
                    }
                },
                enabled = !saving,
            ) {
                if (saving) {
                    CircularProgressIndicator(Modifier.size(16.dp), strokeWidth = 2.dp)
                } else {
                    Text(stringResource(Res.string.share_save_image))
                }
            }
        },
        dismissButton = {
            TextButton(onClick = onDismissRequest) {
                Text(stringResource(Res.string.common_close))
            }
        },
    )
}

/** Draws [content] as a code, square by square. @since 261119 */
@Composable
internal fun QrCode(content: String, modifier: Modifier = Modifier) {
    val matrix = remember(content) { runCatching { QRCodeProcessor(content).encode() }.getOrNull() } ?: return
    Canvas(modifier) {
        val cell = size.minDimension / matrix.size
        matrix.forEachIndexed { row, squares ->
            squares.forEachIndexed { column, square ->
                if (square.dark) drawRect(
                    Color.Black,
                    topLeft = Offset(column * cell, row * cell),
                    size = Size(cell, cell),
                )
            }
        }
    }
}

internal val PosterWidth = 280.dp
internal val PosterHeight = 374.dp

@Composable
@Preview
private fun SharePosterPreview() {
    PreviewTheme(background = false) {
        SharePoster(
            title = "Blue and White Porcelain (Jay Chou 青花瓷)",
            author = "9hSoul",
            coverUrl = null,
            jmid = "JM-ABCD-001",
        )
    }
}
