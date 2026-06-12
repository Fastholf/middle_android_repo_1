package ru.yandexpraktikum.cardsanimation.compose

import androidx.compose.foundation.gestures.detectDragGestures
import androidx.compose.foundation.layout.Box
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.key
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.input.pointer.pointerInput
import ru.yandexpraktikum.cardsanimation.model.CardData
import ru.yandexpraktikum.cardsanimation.ui.AppMath
import ru.yandexpraktikum.cardsanimation.ui.CardSwapAnimationState
import kotlin.math.abs


@Composable
fun AnimatedCardStack(cards: List<CardData>) {
    var orderedCards by remember(cards) { mutableStateOf(cards) }
    var isRotated by remember { mutableStateOf(false) }
    var verticalDragOffset by remember { mutableFloatStateOf(0f) }
    var horizontalDragOffset by remember { mutableFloatStateOf(0f) }
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
                                            animationStep = 1
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
                val isMovingCard = i == (if (animationState.animationStep == 3) 3 else 0)

                AnimatedCard(
                    cardIndex = i,
                    targetRotation = targetRotation,
                    cardData = cardData,
                    isAnimating = if (isMovingCard) animationState.isAnimating else false,
                    animationStep = animationState.animationStep,
                    onAnimationStepComplete = { completedStep ->
                        animationState = when (completedStep) {
                            1 -> CardSwapAnimationState(isAnimating = true, animationStep = 2)
                            2 -> {
                                orderedCards = reorderCards(orderedCards)
                                CardSwapAnimationState(isAnimating = true, animationStep = 3)
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

    val offsetThreshold = 75f
    if (abs(verticalDragOffset) > abs(horizontalDragOffset)) {
        if (verticalDragOffset < -offsetThreshold) onFanStateChange(true)
        if (verticalDragOffset > offsetThreshold) onFanStateChange(false)
    } else {
        if (abs(horizontalDragOffset) > offsetThreshold) onCardsReorder()
    }
}

// Простая функция перестановки карт
fun reorderCards(cards: List<CardData>): List<CardData> {
    return cards.drop(1) + cards.first()
}