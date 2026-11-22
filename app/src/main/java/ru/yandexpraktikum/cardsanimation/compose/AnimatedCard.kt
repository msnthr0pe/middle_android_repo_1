package ru.yandexpraktikum.cardsanimation.compose

import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.TransformOrigin
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.dp
import androidx.compose.ui.zIndex
import ru.yandexpraktikum.cardsanimation.model.CardData
import kotlin.math.cos
import kotlin.math.sin

@Composable
fun AnimatedCard(
    cardIndex: Int,
    cardData: CardData,
    targetRotation: Float,
    isAnimating: Boolean = false,
    animationStep: Int = 0,
    onAnimationStepComplete: ((Int) -> Unit)? = null,
    finalRotation: Float = targetRotation,
    isMovingCard: Boolean = true
) {
    val density = LocalDensity.current
    val animatedRotation by animateFloatAsState(
        targetValue = when {
            animationStep == 3 -> finalRotation
            else -> targetRotation
        },
        animationSpec = tween(durationMillis = if (animationStep == 3) 300 else 800),
        finishedListener = {
            if (animationStep == 3 && isAnimating) {
                onAnimationStepComplete?.invoke(3)
            }
        },
        label = "rotation"
    )

    val moveProgress by animateFloatAsState(
        targetValue = if (isAnimating && isMovingCard && animationStep == 1) 1f else 0f,
        animationSpec = tween(durationMillis = 300),
        finishedListener = { progress ->
            if (isAnimating && isMovingCard) {
                when {
                    animationStep == 1 && progress == 1f ->
                        onAnimationStepComplete?.invoke(1)
                    animationStep == 2 && progress == 0f ->
                        onAnimationStepComplete?.invoke(2)
                }
            }
        },
        label = "cardMoveProgress"
    )
    val moveDistance = with(density) { 50.dp.toPx() }
    val rotationRad = Math.toRadians(targetRotation.toDouble())
    val shouldBringToFront = isAnimating && isMovingCard && animationStep >= 2

    Card(
        modifier = Modifier
            .size(width = 100.dp, height = 160.dp)
            .graphicsLayer {
                translationX = moveDistance * cos(rotationRad).toFloat() * moveProgress
                translationY = moveDistance * sin(rotationRad).toFloat() * moveProgress
                rotationZ = animatedRotation
                transformOrigin = TransformOrigin(0.5f, 1.0f)
            }
            .let { modifier ->
                if (shouldBringToFront) {
                    modifier.zIndex(1000f)
                } else {
                    modifier
                }
            },
        shape = RoundedCornerShape(16.dp),
        elevation = CardDefaults.cardElevation(
            defaultElevation = (4 + cardIndex).dp
        )
    ) {
        Image(
            modifier = Modifier.fillMaxSize(),
            painter = painterResource(cardData.imageResId),
            contentDescription = null,
            contentScale = ContentScale.Crop
        )
    }
}