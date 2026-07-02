package world.hachimi.app.ui.util

import androidx.compose.animation.ContentTransform
import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.togetherWith
import soup.compose.material.motion.MotionConstants
import soup.compose.material.motion.animation.materialFadeOut

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
