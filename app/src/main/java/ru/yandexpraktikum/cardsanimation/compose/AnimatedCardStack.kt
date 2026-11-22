package ru.yandexpraktikum.cardsanimation.compose

import androidx.compose.foundation.gestures.detectDragGestures
import androidx.compose.foundation.layout.Box
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.key
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.unit.dp
import ru.yandexpraktikum.cardsanimation.model.CardData
import ru.yandexpraktikum.cardsanimation.utils.isHorizontalSwipe
import ru.yandexpraktikum.cardsanimation.utils.isVerticalSwipe

/**
 * Метод для вычисления поворота карты в конкретной позиции
 */
fun calculateCardRotation(
    cardIndex: Int,
    cardCount: Int,
    isRotated: Boolean
): Float {
    if (cardCount <= 1) return 0f

    return if (isRotated) {
        val angleStep = 180f / (cardCount - 1)
        90f - (cardIndex * angleStep)
    } else {
        val angleStep = 45f / (cardCount - 1)
        22.5f - (cardIndex * angleStep)
    }
}

private data class CardSwapAnimationState(
    val isAnimating: Boolean = false,
    val animationStep: Int = 0,
    val movingCardId: Int? = null,
    val initialRotations: Map<Int, Float> = emptyMap(),
    val completedCardIds: Set<Int> = emptySet()
)

@Composable
fun AnimatedCardStack(cards: List<CardData>) {
    var orderedCards by remember(cards) { mutableStateOf(cards) }
    val cardCount = orderedCards.size
    val density = LocalDensity.current
    val swipeThreshold = with(density) {
        80.dp.toPx()
    }
    var isRotated by remember { mutableStateOf(false) }

    val animationState = remember(cards) { mutableStateOf(CardSwapAnimationState()) }

    fun handleVerticalSwipe(
        verticalDragDistance: Float,
        onFanStateChange: (Boolean) -> Unit
    ) {
        onFanStateChange(verticalDragDistance < 0f)
    }

    fun handleHorizontalSwipe(
        horizontalDragDistance: Float,
        onCardsReorder: () -> Unit
    ) {
        if (horizontalDragDistance != 0f) {
            onCardsReorder()
        }
    }

    Box(
        modifier = Modifier.pointerInput(swipeThreshold, cards) {
            var totalDrag = Offset.Zero
            detectDragGestures(
                onDragStart = {
                    totalDrag = Offset.Zero
                },
                onDragEnd = {
                    if (!animationState.value.isAnimating) {
                        val dx = totalDrag.x
                        val dy = totalDrag.y

                        when {
                            isVerticalSwipe(dx, dy, swipeThreshold) -> {
                                handleVerticalSwipe(
                                    verticalDragDistance = dy,
                                    onFanStateChange = { isRotated = it }
                                )
                            }
                            isHorizontalSwipe(dx, dy, swipeThreshold) -> {
                                handleHorizontalSwipe(
                                    horizontalDragDistance = dx,
                                    onCardsReorder = {
                                        if (orderedCards.size > 1) {
                                            animationState.value = CardSwapAnimationState(
                                                isAnimating = true,
                                                animationStep = 1,
                                                movingCardId = orderedCards.first().imageResId,
                                                initialRotations = orderedCards.mapIndexed { index, card ->
                                                    card.imageResId to calculateCardRotation(index, cardCount, isRotated)
                                                }.toMap()
                                            )
                                        }
                                    }
                                )
                            }
                        }
                    }
                    totalDrag = Offset.Zero
                }
            ) { change, dragAmount ->
                change.consume()
                totalDrag += dragAmount
            }
        },
        contentAlignment = Alignment.Center
    ) {
        orderedCards.forEachIndexed { i, cardData ->
            key(cardData.imageResId) {
                val state = animationState.value
                val finalRotation = calculateCardRotation(i, cardCount, isRotated)
                val targetRotation = state.initialRotations[cardData.imageResId] ?: finalRotation
                val isMovingCard = cardData.imageResId == state.movingCardId

                AnimatedCard(
                    cardIndex = i,
                    targetRotation = targetRotation,
                    cardData = cardData,
                    finalRotation = finalRotation,
                    isAnimating = state.isAnimating,
                    animationStep = state.animationStep,
                    isMovingCard = isMovingCard,
                    onAnimationStepComplete = { step ->
                        val currentState = animationState.value
                        if (currentState.isAnimating && currentState.animationStep == step) {
                            val completedIds = currentState.completedCardIds + cardData.imageResId
                            if (step == 3) {
                                animationState.value = currentState.copy(completedCardIds = completedIds)
                            }
                            if (step != 3 || completedIds.size == orderedCards.size) {
                                handleAnimationStepComplete(
                                    step = step,
                                    cardIndex = if (step == 3 || isMovingCard) 0 else i + 1,
                                    onStepChange = { nextStep ->
                                        if (nextStep == 3) {
                                            orderedCards = reorderCards(orderedCards)
                                        }
                                        animationState.value = currentState.copy(animationStep = nextStep)
                                    },
                                    onAnimationComplete = {
                                        animationState.value = CardSwapAnimationState()
                                    }
                                )
                            }
                        }
                    }
                )
            }
        }
    }
}

// Простая функция перестановки карт
fun reorderCards(cards: List<CardData>): List<CardData> {
    if (cards.size <= 1) return cards
    return cards.drop(1) + cards.first()
}

fun handleAnimationStepComplete(
    step: Int,
    cardIndex: Int,
    onStepChange: (Int) -> Unit,
    onAnimationComplete: () -> Unit
) {
    if (cardIndex == 0) {
        when (step) {
            1 -> onStepChange(2)
            2 -> onStepChange(3)
            3 -> onAnimationComplete()
        }
    }
}
