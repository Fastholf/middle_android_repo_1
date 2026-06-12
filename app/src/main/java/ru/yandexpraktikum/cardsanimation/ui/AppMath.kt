package ru.yandexpraktikum.cardsanimation.ui

object AppMath {
    /**
     * Метод для вычисления поворота карты в конкретной позиции
     */
    fun cardRotation(isRotated: Boolean, cardIndex: Int, cardCount: Int = 4): Float {
        if (cardCount <= 1) return 0f

        return if (isRotated) {
            val angleStep = 180f / (cardCount - 1)
            90f - (cardIndex * angleStep)
        } else {
            val angleStep = 45f / (cardCount - 1)
            22.5f - (cardIndex * angleStep)
        }
    }
}