package com.github.reyst.barchart.specs

import androidx.annotation.DrawableRes
import androidx.annotation.FloatRange
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Rect
import androidx.compose.ui.geometry.RoundRect
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.graphics.drawscope.translate
import androidx.compose.ui.graphics.painter.Painter
import com.github.reyst.barchart.BarSpecification
import com.github.reyst.barchart.ChartInsets

data class DefaultBarSpecification(
    val barColor: Color = Color.Blue,
    val barSelectionColor: Color = Color.Green.copy(alpha = 0.3F),
    @FloatRange(from = 0.0, to = 1.0) override val barWidth: Float = 0.6F,
    @DrawableRes override val zeroDataIcon: Int? = android.R.drawable.ic_secure,
    val cornerRadius: CornerRadius = CornerRadius(20f, 20f),
): BarSpecification {

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

        if (isSelected) {
            drawRect(
                color = barSelectionColor,
                topLeft = Offset(
                    insets.left + index * itemWidth,
                    insets.top,
                ),
                size = Size(
                    itemWidth,
                    fHeight - insets.vertical,
                )
            )
        }

        if (isEmpty) {
            if (emptyIconPainter != null && emptyIconSize != null) {
                val left = insets.left + index * itemWidth + (itemWidth - emptyIconSize.width) / 2
                val top = insets.bottom + emptyIconSize.height
                translate(left = left, fHeight - top) {
                    with(emptyIconPainter) {
                        draw(size = emptyIconSize)
                    }
                }
            }
        } else {
            val path = Path()
                .apply {
                    addRoundRect(
                        RoundRect(
                            rect = Rect(
                                offset = Offset(
                                    insets.left + index * itemWidth + ((itemWidth - itemWidth * barWidth) / 2),
                                    fHeight - insets.bottom,
                                ),
                                size = Size(
                                    itemWidth * barWidth,
                                    itemHeight,
                                ),
                            ),
                            topLeft = cornerRadius,
                            topRight = cornerRadius,
                        )
                    )
                }
            drawPath(path, color = barColor)
        }
    }
}
