package com.example.mozic.core.designsystem.theme

import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Shapes
import androidx.compose.ui.unit.dp

/**
 * Mozic corner shapes, consumed via `MaterialTheme.shapes` — per
 * `doc/DESIGN.md` §3's radius scale: 10px small chips/list art, 14px cards,
 * 18-20px hero cards/sheets. Pills (buttons/chips/tags, 100px) have no slot
 * here since M3's [Shapes] has no "full" role — set explicitly per component.
 */
val MozicShapes = Shapes(
    extraSmall = RoundedCornerShape(10.dp),
    small = RoundedCornerShape(10.dp),
    medium = RoundedCornerShape(14.dp),
    large = RoundedCornerShape(18.dp),
    extraLarge = RoundedCornerShape(20.dp),
)
