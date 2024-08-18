package com.github.reyst.barchart.specs

import androidx.annotation.DrawableRes
import androidx.annotation.FloatRange
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.ColorFilter
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.graphics.drawscope.translate
import androidx.compose.ui.graphics.painter.Painter
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import com.github.reyst.barchart.BarSpecification
import com.github.reyst.barchart.ChartInsets

data class FloatingDashBarSpecification(
    val barColor: Color = Color.Blue,
    val barSelectionColor: Color = Color.Green,
    @FloatRange(from = 0.0, to = 1.0) val alpha: Float = 0.2F,
    @FloatRange(from = 0.0, to = 1.0) override val barWidth: Float = 0.9F,
    @DrawableRes override val zeroDataIcon: Int? = android.R.drawable.ic_secure,
    val dashHeight: Dp = 3.dp,
) : BarSpecification {

    override fun DrawScope.drawBar(
        index: Int,
        isSelected: Boolean,
        isEmpty: Boolean,
        itemWidth: Float,
        itemHeight: Float,
        emptyIconPainter: Painter?,
        emptyIconSize: Size?,
        insets: ChartInsets,
    ) {
        val fHeight = size.height
        val dashHeightPx = dashHeight.toPx()
        val color =
            if (isSelected) barSelectionColor
            else barColor
        val bgColor = color.copy(alpha = alpha)

        if (isEmpty) {
            if (emptyIconPainter != null && emptyIconSize != null) {
                val left = insets.left + index * itemWidth + (itemWidth - emptyIconSize.width) / 2
                val top = insets.bottom + emptyIconSize.height
                translate(left = left, fHeight - top) {
                    with(emptyIconPainter) {
                        draw(
                            size = emptyIconSize,
                            colorFilter = ColorFilter.tint(barSelectionColor).takeIf { isSelected },
                        )
                    }
                }
            }
        } else {

            drawRect(
                color = bgColor,
                topLeft = Offset(
                    insets.left + index * itemWidth + ((itemWidth - itemWidth * barWidth) / 2),
                    if (isSelected) insets.top else (fHeight - insets.bottom + itemHeight)
                ),
                size = Size(
                    itemWidth * barWidth,
                    if (isSelected) (fHeight - insets.vertical) else -itemHeight,
                ),
            )

            drawRect(
                color = color,
                topLeft = Offset(
                    insets.left + index * itemWidth + ((itemWidth - itemWidth * barWidth) / 2),
                    fHeight - insets.bottom + itemHeight,
                ),
                size = Size(
                    itemWidth * barWidth,
                    dashHeightPx,
                ),
            )
        }
    }
}