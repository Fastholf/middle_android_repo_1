package ru.yandexpraktikum.cardsanimation.views

import android.annotation.SuppressLint
import android.content.Context
import android.util.AttributeSet
import android.view.GestureDetector
import android.view.MotionEvent
import android.widget.FrameLayout
import ru.yandexpraktikum.cardsanimation.model.CardData
import ru.yandexpraktikum.cardsanimation.ui.AppMath
import ru.yandexpraktikum.cardsanimation.ui.CardSwapAnimationState
import kotlin.math.abs

class AnimatedCardStackView @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null,
    defStyleAttr: Int = 0
) : FrameLayout(context, attrs, defStyleAttr) {

    private var cardDataList: List<CardData> = emptyList()
    private val cards = mutableListOf<AnimatedCardView>()
    private var isRotated = false
    private val offsetThreshold = 75f
    private var verticalDragOffset = 0f
    private var horizontalDragOffset = 0f
    private var flingDetected = false
    private var animationState = CardSwapAnimationState()

    init {
        @SuppressLint("ClickableViewAccessibility")
        setOnTouchListener { _, event ->
            var handled = gestureDetector.onTouchEvent(event)
            if (event.action == MotionEvent.ACTION_UP || event.action == MotionEvent.ACTION_CANCEL) {
                if (!flingDetected) {
                    if (abs(verticalDragOffset) > abs(horizontalDragOffset)) {
                        if (abs(verticalDragOffset) > offsetThreshold) {
                            toggleStack(open = verticalDragOffset > 0)
                            handled = true
                        }
                    } else {
                        if (abs(horizontalDragOffset) > offsetThreshold) {
                            startCardSwapAnimation(cards.last())
                            handled = true
                        }
                    }
                }
            }
            handled
        }
    }

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

    private fun updateCardPositions(animated: Boolean = false) {
        val cardCount = cards.size
        cards.forEachIndexed { index, cardView ->
            val targetRotation = AppMath.cardRotation(isRotated, index, cardCount)

            val cardWidth = 100f * resources.displayMetrics.density
            val cardHeight = 160f * resources.displayMetrics.density
            val sharedX = width / 2f - cardWidth / 2f
            val sharedY = height / 2f - cardHeight / 2f

            cardView.x = sharedX
            cardView.y = sharedY

            cardView.pivotX = cardWidth / 2f
            cardView.pivotY = cardHeight

            if (animated) {
                cardView.animateToRotation(targetRotation)
            } else {
                cardView.rotation = targetRotation
            }
        }
    }

    override fun onLayout(changed: Boolean, left: Int, top: Int, right: Int, bottom: Int) {
        super.onLayout(changed, left, top, right, bottom)
        if (changed) {
            updateCardPositions()
        }
    }

    private val gestureDetector = GestureDetector(
        context,
        object : GestureDetector.SimpleOnGestureListener() {
            override fun onDown(e: MotionEvent): Boolean {
                verticalDragOffset = 0f
                horizontalDragOffset = 0f
                flingDetected = false
                return true
            }

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
                flingDetected = true
                val isVertical = abs(velocityY) > abs(velocityX)
                if (isVertical) {
                    toggleStack(open = velocityY < 0)
                } else {
                    startCardSwapAnimation(cards.first())
                }
                return true
            }
        }
    )

    private fun toggleStack(open: Boolean) {
        if (animationState.isAnimating) return
        isRotated = open
        updateCardPositions(animated = true)
    }

    private fun startCardSwapAnimation(bottomCard: AnimatedCardView) {
        if (animationState.isAnimating || isRotated) return

        animationState = CardSwapAnimationState(true, 1)

        bottomCard.moveCardRight {
            animationState = CardSwapAnimationState(true, 2)
            bringCardToFront(bottomCard)
            bottomCard.moveCardToTop {
                animationState = CardSwapAnimationState(true, 3)
                reorderCardsData()
                animateAllCardsToFinalPositions()
            }
        }
    }

    private fun bringCardToFront(card: AnimatedCardView) {
        card.bringToFront()
        val maxElevation = (4 + cards.size + 20).toFloat() * resources.displayMetrics.density
        card.cardView.cardElevation = maxElevation
    }

    private fun reorderCardsData() {
        val reorderedCards = cardDataList.drop(1) + cardDataList.first()
        cardDataList = reorderedCards

        val bottomCardView = cards.removeAt(0)
        cards.add(bottomCardView)

        cards.forEachIndexed { index, cardView ->
            cardView.setCardData(cardDataList[index])
        }
    }

    private fun animateAllCardsToFinalPositions() {
        var completedAnimations = 0
        val totalAnimations = cards.size
        val cardCount = cards.size

        cards.forEachIndexed { index, cardView ->
            val finalRotation = AppMath.cardRotation(isRotated, index, cardCount)

            cardView.adjustToFinalPosition(finalRotation, index) {
                completedAnimations++
                if (completedAnimations == totalAnimations) {
                    finalizeCardPositions()
                }
            }
        }
    }

    private fun finalizeCardPositions() {
        val cardCount = cards.size
        cards.forEachIndexed { index, card ->
            card.setStackPosition(index)
            val correctRotation = AppMath.cardRotation(isRotated, index, cardCount)
            card.rotation = correctRotation
        }

        animationState = CardSwapAnimationState()
    }
}