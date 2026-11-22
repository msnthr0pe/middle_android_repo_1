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

@Composable
fun AnimatedCardStack(cards: List<CardData>) {
    var orderedCards by remember { mutableStateOf(cards) }
    val cardCount = orderedCards.size
    val density = LocalDensity.current
    val swipeThreshold = with(density) {
        80.dp.toPx()
    }
    var isRotated by remember { mutableStateOf(false) }

    fun onSwipe(isVertical: Boolean) {
        if (isVertical) {
            isRotated = !isRotated
        } else {
            orderedCards = reorderCards(orderedCards)
        }
    }

    Box(
        modifier = Modifier.pointerInput(Unit) {
            var totalDrag = Offset.Zero
            detectDragGestures(
                onDragStart = {
                    totalDrag = Offset.Zero
                },
                onDragEnd = {
                    val dx = totalDrag.x
                    val dy = totalDrag.y

                    if (isVerticalSwipe(dx, dy, swipeThreshold)) {
                        onSwipe(isVertical = true)
                    } else if (isHorizontalSwipe(dx, dy, swipeThreshold)) {
                        onSwipe(isVertical = false)
                    }
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
                val targetRotation = calculateCardRotation(i, cardCount, isRotated)

                AnimatedCard(
                    cardIndex = i,
                    targetRotation = targetRotation,
                    cardData = cardData
                    // TODO: [Задание 5] Здесь добавьте параметры анимации карты
                )
            }
        }
    }
}

// Простая функция перестановки карт
fun reorderCards(cards: List<CardData>): List<CardData> {
    return cards.drop(1) + cards.first()
}