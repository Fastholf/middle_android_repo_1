package ru.yandexpraktikum.cardsanimation.views

import android.animation.ObjectAnimator
import android.content.Context
import android.util.AttributeSet
import android.view.LayoutInflater
import android.widget.FrameLayout
import android.widget.ImageView
import androidx.cardview.widget.CardView
import ru.yandexpraktikum.cardsanimation.R
import ru.yandexpraktikum.cardsanimation.model.CardData
import ru.yandexpraktikum.cardsanimation.ui.AnimParams.FAN_DURATION_MS
import ru.yandexpraktikum.cardsanimation.ui.AnimParams.REORDER_STEP_DURATION_MS
import kotlin.math.cos
import kotlin.math.sin

class AnimatedCardView @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null,
    defStyleAttr: Int = 0
) : FrameLayout(context, attrs, defStyleAttr) {

    val cardView: CardView
    private val cardImageView: ImageView

    init {
        LayoutInflater.from(context).inflate(R.layout.card_view, this, true)

        cardView = this.getChildAt(0) as CardView
        cardImageView = findViewById(R.id.cardImage)

        pivotX = width / 2f
        pivotY = height.toFloat()
    }

    // Ушаков Алексей: При выполнении Задания 4 пришлось закомментить этот код. По крайней мере на
    // моём девайсе карты разъезжались в разные стороны при вызове метода startCardSwapAnimation().
    // Я бы на самом деле убрал pivot из AnimatedCardStackView и оставил здесь. Но тогда надо будет
    // ещё в AnimatedCardStackView поправить добавление вью в контейнер - сейчас там не указываются
    // явно LayoutParams и видимо FrameLayout AnimatedCardView раздувается до размера контейнера.
//    override fun onSizeChanged(w: Int, h: Int, oldw: Int, oldh: Int) {
//        super.onSizeChanged(w, h, oldw, oldh)
//        pivotX = w / 2f
//        pivotY = h.toFloat()
//    }

    fun setCardData(cardData: CardData) {
        cardImageView.setImageResource(cardData.imageResId)
    }

    fun setStackPosition(index: Int) {
        cardView.cardElevation = (4 + index * 1).toFloat() * resources.displayMetrics.density
    }

    fun animateToRotation(targetRotation: Float) {
        ObjectAnimator
            .ofFloat(this, "rotation", targetRotation)
            .apply { this.duration = FAN_DURATION_MS.toLong() }
            .start()
    }

    fun moveCardRight(onComplete: (() -> Unit)? = null) {
        val moveDistance = resources.getDimension(R.dimen.reorder_first_translation)
        val currentRotationRad = Math.toRadians(rotation.toDouble())

        val deltaX = moveDistance * cos(currentRotationRad).toFloat()
        val deltaY = moveDistance * sin(currentRotationRad).toFloat()

        val currentX = x
        val currentY = y

        val animatorX = ObjectAnimator.ofFloat(this, "x", currentX, currentX + deltaX)
        val animatorY = ObjectAnimator.ofFloat(this, "y", currentY, currentY + deltaY)

        val animatorSet = android.animation.AnimatorSet().apply {
            playTogether(animatorX, animatorY)
            duration = REORDER_STEP_DURATION_MS.toLong()
            addListener(object : android.animation.AnimatorListenerAdapter() {
                override fun onAnimationEnd(animation: android.animation.Animator) {
                    onComplete?.invoke()
                }
            })
        }

        animatorSet.start()
    }

    fun moveCardToTop(onComplete: (() -> Unit)? = null) {
        val parent = parent as? FrameLayout ?: return
        val cardWidth = resources.getDimension(R.dimen.card_width)
        val cardHeight = resources.getDimension(R.dimen.card_height)
        val centerX = parent.width / 2f - cardWidth / 2f
        val centerY = parent.height / 2f - cardHeight / 2f

        val animatorX = ObjectAnimator.ofFloat(this, "x", x, centerX)
        val animatorY = ObjectAnimator.ofFloat(this, "y", y, centerY)

        val animatorSet = android.animation.AnimatorSet().apply {
            playTogether(animatorX, animatorY)
            duration = REORDER_STEP_DURATION_MS.toLong()
            addListener(object : android.animation.AnimatorListenerAdapter() {
                override fun onAnimationEnd(animation: android.animation.Animator) {
                    onComplete?.invoke()
                }
            })
        }

        animatorSet.start()
    }

    fun adjustToFinalPosition(
        finalRotation: Float,
        finalZOrder: Int,
        onComplete: (() -> Unit)? = null
    ) {
        ObjectAnimator.ofFloat(this, "rotation", rotation, finalRotation).apply {
            duration = REORDER_STEP_DURATION_MS.toLong()
            addListener(object : android.animation.AnimatorListenerAdapter() {
                override fun onAnimationEnd(animation: android.animation.Animator) {
                    setStackPosition(finalZOrder)
                    onComplete?.invoke()
                }
            })
            start()
        }
    }
} 