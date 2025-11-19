@file:OptIn(ExperimentalWasmJsInterop::class)

package world.hachimi.app.font

import androidx.compose.runtime.Composable
import kotlin.js.ExperimentalWasmJsInterop

@Composable
expect fun WithFont(content: @Composable () -> Unit)