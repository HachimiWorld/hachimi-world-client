package world.hachimi.app.ui.player.fullscreen.components

import androidx.compose.animation.*
import androidx.compose.runtime.Composable
import soup.compose.material.motion.animation.materialSharedAxisX
import soup.compose.material.motion.animation.rememberSlideDistance


typealias TabTransitionSpec = AnimatedContentTransitionScope<Page?>.() -> ContentTransform

@Composable
fun rememberTabTransitionSpec(): TabTransitionSpec {
    val sliderDistance = rememberSlideDistance()
    val spec: TabTransitionSpec = {
        val initialState = initialState
        val targetState = targetState

        if (initialState == null || targetState == null) {
            fadeIn() togetherWith fadeOut()
        } else if (targetState < initialState) {
            // Slide to the left page
            materialSharedAxisX(forward = false, slideDistance = sliderDistance)
        } else {
            // Slide to the right page
            materialSharedAxisX(forward = true, slideDistance = sliderDistance)
        }
    }
    return spec
}
