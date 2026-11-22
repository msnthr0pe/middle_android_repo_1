package ru.yandexpraktikum.cardsanimation.views

import android.content.Context
import android.util.AttributeSet
import android.view.GestureDetector
import android.view.MotionEvent
import android.widget.FrameLayout
import ru.yandexpraktikum.cardsanimation.model.CardData
import ru.yandexpraktikum.cardsanimation.utils.isHorizontalSwipe
import ru.yandexpraktikum.cardsanimation.utils.isVerticalSwipe

class AnimatedCardStackView @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null,
    defStyleAttr: Int = 0
) : FrameLayout(context, attrs, defStyleAttr) {

    private var cardDataList: List<CardData> = emptyList()
    private val cards = mutableListOf<AnimatedCardView>()
    private var isRotated = false
    private var verticalDragOffset = 0f
    private var horizontalDragOffset = 0f
    private val swipeThreshold = 80f * resources.displayMetrics.density
    private val flingVelocityThreshold = 800f * resources.displayMetrics.density

    fun setCards(newCardDataList: List<CardData>) {
        cardDataList = newCardDataList
        setupCards()
    }

    private fun setupCards() {
        clearCards()
        cardDataList.forEachIndexed { index, cardData ->
            val cardView = AnimatedCardView(context).apply {
                setCardData(cardData)
                setStackPosition(index)
            }
            cards.add(cardView)
            addView(cardView)
        }
        // Возврат в исходное положение
        isRotated = false
        updateCardPositions()
    }

    private fun clearCards() {
        cards.clear()
        removeAllViews()
    }

    private fun updateCardPositions() {
        val cardCount = cards.size

        cards.forEachIndexed { index, cardView ->
            // Расчёт расположения карт в исходной позиции
            val baseRotation = if (cardCount > 1) {
                val angleStep = 45f / (cardCount - 1)
                22.5f - (index * angleStep)
            } else {
                0f
            }

            // Расчёт финальной позиции (для эффекта раскрытой колоды карт)
            val targetRotation = if (isRotated) {
                val angleStep = if (cardCount > 1) 180f / (cardCount - 1) else 0f
                90f - (index * angleStep)
            } else {
                baseRotation
            }

            val cardWidth = 100f * resources.displayMetrics.density
            val cardHeight = 160f * resources.displayMetrics.density
            val sharedX = width / 2f - cardWidth / 2f
            val sharedY = height / 2f - cardHeight / 2f

            cardView.x = sharedX
            cardView.y = sharedY

            cardView.pivotX = cardWidth / 2f
            cardView.pivotY = cardHeight

            cardView.animateToRotation(targetRotation)
        }
    }

    override fun onLayout(changed: Boolean, left: Int, top: Int, right: Int, bottom: Int) {
        super.onLayout(changed, left, top, right, bottom)
        if (changed) {
            updateCardPositions()
        }
    }

    private fun startCardSwapAnimation(bottomCard: AnimatedCardView) {
        // TODO: [Задание 5] Добавьте анимацию перетасовки карт
        // На данном этапе просто быстро двигаем нижнюю карту наверх
        cardDataList = reorderCards(cardDataList)
        setupCards()
    }

    // Простая функция перестановки карт
    fun reorderCards(cards: List<CardData>): List<CardData> {
        return cards.drop(1) + cards.first()
    }

    private val gestureDetector = GestureDetector(context, object : GestureDetector.SimpleOnGestureListener() {

        override fun onScroll(
            e1: MotionEvent?,
            e2: MotionEvent,
            distanceX: Float,
            distanceY: Float
        ): Boolean {
            verticalDragOffset += distanceY
            horizontalDragOffset += distanceX
            return true
        }

        override fun onFling(
            e1: MotionEvent?,
            e2: MotionEvent,
            velocityX: Float,
            velocityY: Float
        ): Boolean {
            if (isVerticalSwipe(velocityX, velocityY, flingVelocityThreshold)) {
                onSwipe(isVertical = true)
                return true
            } else if (isHorizontalSwipe(velocityX, velocityY, flingVelocityThreshold)) {
                onSwipe(isVertical = false)
                return true
            }
            return false
        }
    })

    // Нужно также переопределить onTouchEvent
    override fun onTouchEvent(event: MotionEvent): Boolean {
        gestureDetector.onTouchEvent(event)

        when (event.actionMasked) {
            MotionEvent.ACTION_DOWN, MotionEvent.ACTION_CANCEL -> {
                clearDragOffsets()
            }
            MotionEvent.ACTION_UP -> {
                handleDragEnd()
            }
        }
        return true
    }

    private fun handleDragEnd() {
        if (isVerticalSwipe(horizontalDragOffset, verticalDragOffset, swipeThreshold)) {
            onSwipe(isVertical = true)
        } else if (isHorizontalSwipe(horizontalDragOffset, verticalDragOffset, swipeThreshold)) {
            onSwipe(isVertical = false)
        }
    }

    private fun onSwipe(isVertical: Boolean) {
        if (isVertical) {
            isRotated = !isRotated
            updateCardPositions()
        } else {
            cards.firstOrNull()?.let { bottomCard ->
                startCardSwapAnimation(bottomCard)
            }
        }
        clearDragOffsets()
    }

    private fun clearDragOffsets() {
        verticalDragOffset = 0f
        horizontalDragOffset = 0f
    }
}