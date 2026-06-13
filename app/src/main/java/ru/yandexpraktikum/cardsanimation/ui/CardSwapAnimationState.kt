package ru.yandexpraktikum.cardsanimation.ui

import androidx.compose.runtime.Immutable

enum class CardSwapAnimationStep {
    IDLE,
    MOVE_RIGHT,
    MOVE_TO_TOP,
    FINAL_ROTATION;

    val isMoveRight: Boolean
        get() = this == MOVE_RIGHT

    val isMoveToTop: Boolean
        get() = this == MOVE_TO_TOP

    val movesCard: Boolean
        get() = this == MOVE_RIGHT || this == MOVE_TO_TOP

    val isFinalRotation: Boolean
        get() = this == FINAL_ROTATION
}

@Immutable
data class CardSwapAnimationState(
    val isAnimating: Boolean = false,
    val step: CardSwapAnimationStep = CardSwapAnimationStep.IDLE
)