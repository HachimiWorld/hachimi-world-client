package world.hachimi.app.ui.follow.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.unit.dp
import world.hachimi.app.ui.design.HachimiTheme
import world.hachimi.app.ui.design.components.Surface

@Composable
fun LoadingSkeleton() {
    Column(
        modifier = Modifier.fillMaxSize().padding(12.dp),
        verticalArrangement = Arrangement.spacedBy(6.dp)
    ) {
        repeat(6) {
            Surface(
                modifier = Modifier.fillMaxWidth().height(72.dp),
                shape = RoundedCornerShape(12.dp),
                color = HachimiTheme.colorScheme.surface
            ) {
                Row(
                    modifier = Modifier.padding(12.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Box(
                        Modifier.size(48.dp)
                            .clip(CircleShape)
                            .background(HachimiTheme.colorScheme.onSurface.copy(0.06f))
                    )
                    Column(
                        modifier = Modifier.padding(start = 12.dp),
                        verticalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Box(
                            Modifier.fillMaxWidth(0.45f).height(13.dp)
                                .clip(RoundedCornerShape(5.dp))
                                .background(HachimiTheme.colorScheme.onSurface.copy(0.06f))
                        )
                        Box(
                            Modifier.fillMaxWidth(0.68f).height(13.dp)
                                .clip(RoundedCornerShape(5.dp))
                                .background(HachimiTheme.colorScheme.onSurface.copy(0.06f))
                        )
                        Box(
                            Modifier.fillMaxWidth(0.28f).height(13.dp)
                                .clip(RoundedCornerShape(5.dp))
                                .background(HachimiTheme.colorScheme.onSurface.copy(0.06f))
                        )
                    }
                }
            }
        }
    }
}
