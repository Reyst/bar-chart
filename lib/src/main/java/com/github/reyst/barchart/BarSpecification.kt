package com.github.reyst.barchart

import androidx.annotation.DrawableRes
import androidx.annotation.FloatRange
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.graphics.painter.Painter
import com.github.reyst.barchart.specs.DefaultBarSpecification

interface BarSpecification {

//    val barColor: Color
//    val barSelectionColor: Color

    @get:FloatRange(from = 0.0, to = 1.0)
    val barWidth: Float

    @get:DrawableRes
    val zeroDataIcon: Int?

//    val cornerRadius: CornerRadius


    fun DrawScope.drawBar(
        index: Int,
        isSelected: Boolean,
        isEmpty: Boolean,
        itemWidth: Float,
        itemHeight: Float,
        emptyIconPainter: Painter?,
        emptyIconSize: Size?,
        insets: ChartInsets,
    )

    companion object {
        val DEFAULT_BAR_ITEM: BarSpecification = DefaultBarSpecification()
    }
}
