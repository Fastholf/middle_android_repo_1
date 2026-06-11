package ru.yandexpraktikum.cardsanimation.compose

import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.animateOffsetAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.TransformOrigin
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.res.dimensionResource
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.dp
import ru.yandexpraktikum.cardsanimation.R
import ru.yandexpraktikum.cardsanimation.model.CardData
import kotlin.math.cos
import kotlin.math.sin

@Composable
fun AnimatedCard(
    cardIndex: Int,
    cardData: CardData,
    targetRotation: Float,
    isAnimating: Boolean,
    animationStep: Int,
    onAnimationStepComplete: ((Int) -> Unit)?
) {
    val animatedRotation by animateFloatAsState(targetRotation)

    val targetTranslation = if (isAnimating && animationStep == 1) {
        val moveDistance = with(LocalDensity.current) {
            dimensionResource(R.dimen.reorder_first_translation).toPx()
        }
        val rotationRad = Math.toRadians(targetRotation.toDouble())
        Offset(
            x = moveDistance * cos(rotationRad).toFloat(),
            y = moveDistance * sin(rotationRad).toFloat()
        )
    } else {
        Offset.Zero
    }
    val animatedTranslation by animateOffsetAsState(
        targetValue = targetTranslation,
        animationSpec = tween(durationMillis = 300),
        finishedListener = {
            if (isAnimating && animationStep == 1) onAnimationStepComplete?.invoke(1)
        },
        label = "cardTranslation"
    )

    Card(
        modifier = Modifier
            .size(width = 100.dp, height = 160.dp)
            .graphicsLayer {
                rotationZ = animatedRotation
                transformOrigin = TransformOrigin(0.5f, 1.0f)
                translationX = if (isAnimating) animatedTranslation.x else 0f
                translationY = if (isAnimating) animatedTranslation.y else 0f
            },
        shape = RoundedCornerShape(16.dp),
        elevation = CardDefaults.cardElevation(
            defaultElevation = (4 + cardIndex).dp
        )
    ) {
        Image(
            modifier = Modifier.fillMaxSize(),
            painter = painterResource(cardData.imageResId),
            contentDescription = null,
            contentScale = ContentScale.Crop
        )
    }
}