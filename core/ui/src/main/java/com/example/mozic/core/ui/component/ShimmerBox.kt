package com.example.mozic.core.ui.component

import androidx.compose.foundation.layout.Box
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Shape
import com.example.mozic.core.ui.modifier.shimmer

/** A shimmering placeholder shape; skeleton layouts compose this at the real content's size. */
@Composable
fun ShimmerBox(
    modifier: Modifier = Modifier,
    shape: Shape = MaterialTheme.shapes.medium,
) {
    Box(modifier = modifier.clip(shape).shimmer())
}
