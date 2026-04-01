package com.example.wishlistapp.ui.components

import androidx.annotation.DrawableRes
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Shape
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.dp

@Composable
fun RoundedImage(
    @DrawableRes imageRes: Int,
    modifier: Modifier = Modifier,
    imageModifier: Modifier = Modifier,
    backgroundColor: Color = Color.Transparent,
    shape: Shape = RoundedCornerShape(8.dp),
    contentDescription: String? = null
) {
    Box(
        modifier = modifier
            .background(backgroundColor, shape)
            .clip(shape),
        contentAlignment = Alignment.Center
    ) {
        Image(
            painter = painterResource(id = imageRes),
            contentDescription = contentDescription,
            modifier = imageModifier,
            contentScale = ContentScale.Fit
        )
    }
}
