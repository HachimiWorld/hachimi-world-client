package world.hachimi.app.ui.component

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Cancel
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.outlined.Schedule
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import coil3.compose.AsyncImage
import coil3.compose.LocalPlatformContext
import coil3.network.httpHeaders
import coil3.request.ImageRequest
import coil3.request.crossfade
import kotlinx.datetime.LocalDateTime
import world.hachimi.app.api.CoilHeaders
import world.hachimi.app.api.module.PublishModule
import world.hachimi.app.ui.design.components.Icon
import world.hachimi.app.ui.design.components.LocalContentColor
import world.hachimi.app.ui.design.components.Surface
import world.hachimi.app.ui.design.components.Text
import world.hachimi.app.util.YMD
import world.hachimi.app.util.formatTime
import kotlin.time.Instant

@Composable
fun ReviewItem(
    id: Long,
    coverUrl: String,
    title: String,
    subtitle: String,
    artist: String,
    submitTime: Instant,
    status: Int,
    type: Int,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val statusColor = when (status) {
        PublishModule.SongPublishReviewBrief.STATUS_PENDING -> Color(0xFFD29922)
        PublishModule.SongPublishReviewBrief.STATUS_APPROVED -> Color(0xFF3FB950)
        PublishModule.SongPublishReviewBrief.STATUS_REJECTED -> Color(0xFFF85149)
        else -> LocalContentColor.current
    }
    val statusIcon = when (status) {
        PublishModule.SongPublishReviewBrief.STATUS_PENDING -> Icons.Outlined.Schedule
        PublishModule.SongPublishReviewBrief.STATUS_APPROVED -> Icons.Filled.CheckCircle
        PublishModule.SongPublishReviewBrief.STATUS_REJECTED -> Icons.Filled.Cancel
        else -> Icons.Outlined.Schedule
    }
    val statusText = when (status) {
        PublishModule.SongPublishReviewBrief.STATUS_PENDING -> "待审核"
        PublishModule.SongPublishReviewBrief.STATUS_APPROVED -> "通过"
        PublishModule.SongPublishReviewBrief.STATUS_REJECTED -> "退回"
        else -> "未知"
    }
    val typeText = when (type) {
        PublishModule.SongPublishReviewBrief.TYPE_CREATION -> "发布"
        PublishModule.SongPublishReviewBrief.TYPE_MODIFICATION -> "编辑"
        else -> "未知"
    }

    Column(
        modifier
            .clickable(onClick = onClick)
            .padding(horizontal = 24.dp, vertical = 12.dp),
        verticalArrangement = Arrangement.spacedBy(10.dp)
    ) {
        // Top row: status icon + #ID + status text ... type badge
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(6.dp)
        ) {
            Icon(
                imageVector = statusIcon,
                contentDescription = statusText,
                modifier = Modifier.size(16.dp),
                tint = statusColor
            )
            Text(
                text = "#$id",
                style = MaterialTheme.typography.labelMedium,
                fontWeight = FontWeight.SemiBold
            )
            Text(
                text = statusText,
                style = MaterialTheme.typography.labelSmall,
                color = statusColor
            )
            Spacer(Modifier.weight(1f))
            Surface(
                shape = RoundedCornerShape(4.dp),
                color = LocalContentColor.current.copy(0.08f)
            ) {
                Text(
                    text = typeText,
                    modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp),
                    style = MaterialTheme.typography.labelSmall
                )
            }
        }

        // Content row: cover + info
        Row(
            horizontalArrangement = Arrangement.spacedBy(12.dp),
            verticalAlignment = Alignment.Top
        ) {
            // Cover image
            Surface(
                Modifier.size(52.dp),
                RoundedCornerShape(8.dp),
                LocalContentColor.current.copy(0.08f)
            ) {
                AsyncImage(
                    modifier = Modifier.fillMaxSize(),
                    model = ImageRequest.Builder(LocalPlatformContext.current)
                        .httpHeaders(CoilHeaders)
                        .data(coverUrl)
                        .crossfade(true)
                        .build(),
                    contentDescription = "Cover Image",
                    contentScale = ContentScale.Crop
                )
            }

            // Title + subtitle + meta info
            Column(
                modifier = Modifier.weight(1f),
                verticalArrangement = Arrangement.spacedBy(2.dp)
            ) {
                Text(
                    text = title,
                    style = MaterialTheme.typography.bodyMedium,
                    fontWeight = FontWeight.Medium,
                    maxLines = 2,
                    overflow = TextOverflow.Ellipsis
                )
                Text(
                    text = subtitle,
                    style = MaterialTheme.typography.bodySmall,
                    color = LocalContentColor.current.copy(0.6f),
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
                // Meta row: artist · time ago
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(4.dp)
                ) {
                    Text(
                        text = artist,
                        style = MaterialTheme.typography.labelSmall,
                        color = LocalContentColor.current.copy(0.5f),
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                    Text(
                        text = "·",
                        style = MaterialTheme.typography.labelSmall,
                        color = LocalContentColor.current.copy(0.4f)
                    )
                    Text(
                        text = formatTime(
                            submitTime,
                            distance = true,
                            precise = false,
                            fullFormat = LocalDateTime.Formats.YMD
                        ),
                        style = MaterialTheme.typography.labelSmall,
                        color = LocalContentColor.current.copy(0.5f),
                        maxLines = 1
                    )
                }
            }
        }
    }
}

