package com.liyaqa.android.ui.theme

import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Shapes
import androidx.compose.ui.unit.dp

val Shapes = Shapes(
    // Extra small components (chips, buttons)
    extraSmall = RoundedCornerShape(4.dp),

    // Small components (cards, dialogs)
    small = RoundedCornerShape(8.dp),

    // Medium components (extended FABs)
    medium = RoundedCornerShape(12.dp),

    // Large components (bottom sheets, modals)
    large = RoundedCornerShape(16.dp),

    // Extra large components (large sheets)
    extraLarge = RoundedCornerShape(28.dp)
)
