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
import androidx.compose.ui.input.pointer.pointerInput
import ru.yandexpraktikum.cardsanimation.model.CardData
import ru.yandexpraktikum.cardsanimation.ui.AnimParams.OFFSET_THRESHOLD_PX
import ru.yandexpraktikum.cardsanimation.ui.AppMath
import ru.yandexpraktikum.cardsanimation.ui.CardSwapAnimationState
import ru.yandexpraktikum.cardsanimation.ui.CardSwapAnimationStep.FINAL_ROTATION
import ru.yandexpraktikum.cardsanimation.ui.CardSwapAnimationStep.MOVE_RIGHT
import ru.yandexpraktikum.cardsanimation.ui.CardSwapAnimationStep.MOVE_TO_TOP
import kotlin.math.abs


@Composable
fun AnimatedCardStack(cards: List<CardData>) {
    var orderedCards by remember(cards) { mutableStateOf(cards) }
    var isRotated by remember { mutableStateOf(false) }
    var verticalDragOffset = 0f
    var horizontalDragOffset = 0f
    var animationState by remember { mutableStateOf(CardSwapAnimationState()) }

    Box(
        modifier = Modifier
            .pointerInput(Unit) {
                detectDragGestures(
                    onDrag = { _, dragAmount ->
                        verticalDragOffset += dragAmount.y
                        horizontalDragOffset += dragAmount.x
                    },
                    onDragEnd = {
                        handleDragEnd(
                            animationState,
                            verticalDragOffset,
                            horizontalDragOffset,
                            onFanStateChange = { newFanState -> isRotated = newFanState },
                            onCardsReorder = {
                                if (!isRotated) {
                                    animationState =
                                        CardSwapAnimationState(
                                            isAnimating = true,
                                            step = MOVE_RIGHT
                                        )
                                }
                            }
                        )
                        verticalDragOffset = 0f
                        horizontalDragOffset = 0f
                    }
                )
            },
        contentAlignment = Alignment.Center
    ) {
        orderedCards.forEachIndexed { i, cardData ->
            key(cardData.imageResId) {
                val targetRotation = AppMath.cardRotation(isRotated, i, orderedCards.size)

                // Перед началом 3 этапа мы поместили нижнюю карту наверх
                val movingCardIndex = if (animationState.step.isFinalRotation) {
                    orderedCards.lastIndex
                } else {
                    0
                }
                val isMovingCard = i == movingCardIndex

                AnimatedCard(
                    cardIndex = i,
                    targetRotation = targetRotation,
                    cardData = cardData,
                    isAnimating = if (isMovingCard) animationState.isAnimating else false,
                    animationStep = animationState.step,
                    onAnimationStepComplete = { completedStep ->
                        animationState = when (completedStep) {
                            MOVE_RIGHT -> CardSwapAnimationState(
                                isAnimating = true,
                                step = MOVE_TO_TOP
                            )

                            MOVE_TO_TOP -> {
                                orderedCards = reorderCards(orderedCards)
                                CardSwapAnimationState(
                                    isAnimating = true,
                                    step = FINAL_ROTATION
                                )
                            }

                            else -> CardSwapAnimationState()
                        }
                    }
                )
            }
        }
    }
}

fun handleDragEnd(
    animationState: CardSwapAnimationState,
    verticalDragOffset: Float,
    horizontalDragOffset: Float,
    onFanStateChange: (Boolean) -> Unit,
    onCardsReorder: () -> Unit
) {
    if (animationState.isAnimating) return

    if (abs(verticalDragOffset) > abs(horizontalDragOffset)) {
        if (verticalDragOffset < -OFFSET_THRESHOLD_PX) onFanStateChange(true)
        if (verticalDragOffset > OFFSET_THRESHOLD_PX) onFanStateChange(false)
    } else {
        if (abs(horizontalDragOffset) > OFFSET_THRESHOLD_PX) onCardsReorder()
    }
}

// Простая функция перестановки карт
fun reorderCards(cards: List<CardData>): List<CardData> {
    return cards.drop(1) + cards.first()
}