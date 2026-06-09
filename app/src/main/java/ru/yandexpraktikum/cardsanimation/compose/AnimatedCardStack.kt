package ru.yandexpraktikum.cardsanimation.compose

import android.util.Log
import androidx.compose.foundation.clickable
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
import ru.yandexpraktikum.cardsanimation.model.CardData
import kotlin.math.abs

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
    val cardCount = cards.size
    var isRotated by remember { mutableStateOf(false) }
    var dragOffset by remember { mutableStateOf(Offset.Zero) }

    Box(
        modifier = Modifier
            // TODO: Temporary for Task 1 checks, remove later
            .clickable(onClick = { isRotated = !isRotated })
            .pointerInput(Unit) {
                // TODO: Temporary logs for Task 2 checks, remove later
                detectDragGestures(
                    onDragStart = {
                        dragOffset = Offset.Zero
                        Log.d("TASK2", "Drag started")
                    },
                    onDrag = { _, dragAmount ->
                        dragOffset += dragAmount
                        val isHorizontal = abs(dragOffset.x) > abs(dragOffset.y)
                        Log.d(
                            "TASK2",
                            "Dragging " + (if (isHorizontal) "horizontally" else "vertically") + " ($dragOffset)"
                        )
                    },
                    onDragEnd = {
                        val isHorizontal = abs(dragOffset.x) > abs(dragOffset.y)
                        Log.d(
                            "TASK2",
                            if (isHorizontal) "Horizontal" else "Vertical" + " drag finished"
                        )
                    }
                )
            },
        contentAlignment = Alignment.Center
    ) {
        cards.forEachIndexed { i, cardData ->
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