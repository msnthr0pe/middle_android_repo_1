package ru.yandexpraktikum.cardsanimation.utils

import kotlin.math.abs


fun isVerticalSwipe(dx: Float, dy: Float, swipeThreshold: Float): Boolean {
    return abs(dy) > abs(dx) && abs(dy) > swipeThreshold
}

fun isHorizontalSwipe(dx: Float, dy: Float, swipeThreshold: Float): Boolean {
    return abs(dx) > abs(dy) && abs(dx) > swipeThreshold
}