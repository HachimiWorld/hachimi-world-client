package world.hachimi.app.ui.util

import androidx.compose.animation.ContentTransform
import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.togetherWith
import soup.compose.material.motion.MotionConstants
import soup.compose.material.motion.animation.materialFadeOut
import soup.compose.material.motion.animation.materialFadeThroughIn
import soup.compose.material.motion.animation.materialFadeThroughOut

private const val DefaultFadeEndThresholdEnter = 0.3f
private val Int.ForFade: Int
    get() = (this * DefaultFadeEndThresholdEnter).toInt()

fun fadeInFadeOut(
    durationMillis: Int = MotionConstants.DefaultFadeInDuration,
): ContentTransform =
    fadeIn(
        animationSpec = tween(
            durationMillis = durationMillis.ForFade,
            easing = LinearEasing,
        ),
    ) togetherWith materialFadeOut()


/**
 * Between the loading, failed and loaded states of one area: Material fade through without its
 * scale. The skeleton stands where the content will be, at its size, so the content only fades in
 * where the skeleton was; growing it from 92% would read as the page shrinking and springing back.
 *
 * @since 261112
 */
fun loadStateTransition(): ContentTransform =
    materialFadeThroughIn(initialScale = 1f) togetherWith materialFadeThroughOut()

/*
fun AnimatedContentTransitionScope<InitializeStatus>.fadeInFadeOut(
    durationMillis: Int = MotionConstants.DefaultFadeInDuration,
): ContentTransform {
    return if (InitializeStatus.LOADED isTransitioningTo InitializeStatus.INIT) {
        EnterTransition.None togetherWith ExitTransition.None
    } else {
        fadeIn(
            animationSpec = tween(
                durationMillis = durationMillis.ForFade,
                easing = LinearEasing,
            ),
        ) togetherWith materialFadeOut()
    }
}*/
