package com.github.reyst.barchart

import androidx.annotation.FloatRange
import androidx.compose.animation.core.CubicBezierEasing
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.wrapContentSize
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.Stable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.UiComposable
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Rect
import androidx.compose.ui.geometry.RoundRect
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.PathEffect
import androidx.compose.ui.graphics.drawscope.translate
import androidx.compose.ui.graphics.painter.Painter
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.layout.Layout
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.TextLayoutResult
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.drawText
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.rememberTextMeasurer
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import kotlin.math.ceil
import kotlin.math.max

interface ChartValue<T : Number> {
    val value: T
    val isEmpty: Boolean
}

interface IntChartValue : ChartValue<Int> {
    override val isEmpty: Boolean
        get() = value < 0
}

@Stable
private data class GridConfig(
    val horizontalLines: Int,
    val hLineStep: Int,
    val yGuideResults: List<TextLayoutResult>,
    val verticalLines: Int,
    val vLineStep: Float,
    val xGuideResults: List<TextLayoutResult?>,
    val color: Color,
    val lineThickness: Dp = 1.dp,
)

@Stable
private data class BarConfig(
    val itemStep: Float,
    val cornerRadius: CornerRadius,
    val barColor: Color,
    val barWidth: Float,
    val iconSize: Size,
    val zeroDataIcon: Painter,
    val barSelectionColor: Color,
)


private data class ChartInsets(
    val left: Float,
    val top: Float,
    val right: Float,
    val bottom: Float,
) {
    val vertical: Float
        get() = top + bottom

    val horizontal: Float
        get() = left + right
}


fun calculateScaleStep(value: Double, divisions: Int, roundMultiplier: Int = 1): Int {

    val maxValue = ceil(value + 0.01)

    val step = ceil(maxValue / (divisions - 1)).toInt()

    return roundUpTo(step, roundMultiplier)
}

fun roundUpTo(number: Int, multiplier: Int): Int {
    return if (number % multiplier == 0) number else (number / multiplier + 1) * multiplier
}


@Composable
fun BarChart(
    values: List<IntChartValue>,
    modifier: Modifier = Modifier,
    xAxisGuideCount: Int = 3,
    xLabelStyle: TextStyle = TextStyle.Default,
    xLabelProvider: (Int) -> String = { "" },
    yAxisGuideCount: Int = 2,
    yAxisStepRoundMultiplier: (maxValue: Int) -> Int = { if (it > 10) 5 else 1 },
    yLabelStyle: TextStyle = TextStyle.Default,
    gridColor: Color = Color.LightGray,
    barColor: Color = Color.Blue,
    barSelectionColor: Color = Color.Green.copy(alpha = 0.3F),
    @FloatRange(from = 0.0, to = 1.0) barWidth: Float = 0.6F,
    zeroDataIcon: Painter = painterResource(id = android.R.drawable.ic_secure),
    cornerRadius: CornerRadius = CornerRadius(20f, 20f),
    @FloatRange(from = 0.0, to = 1.0) barScale: Float = 1F,
    markerContent: @Composable @UiComposable (ChartValue<*>) -> Unit = {},
) {

    var selectedItemIndex by remember(values) { mutableIntStateOf(-1) }

    BoxWithConstraints(modifier = modifier) {

        val fWidth = constraints.maxWidth
        val fHeight = constraints.maxHeight

        val textMeasurer = rememberTextMeasurer()

        val maxValue = remember(values) {
            values
                .fold(0) { result, item ->
                    if (item.isEmpty) result
                    else maxOf(result, item.value)

                }
                .takeIf { it > 0 }
                ?: 1
        }

        val horizontalLines = yAxisGuideCount + 2
        val hLineStep = remember(maxValue, horizontalLines) {
            calculateScaleStep(
                maxValue.toDouble(),
                horizontalLines,
                yAxisStepRoundMultiplier(maxValue)
            )
        }
        val topValue = remember(hLineStep) { hLineStep * (horizontalLines - 1) }

        val yGuideResults = remember(horizontalLines) {
            List(horizontalLines) { (it * hLineStep).toString() }
                .map { textMeasurer.measure(it, yLabelStyle) }
        }

        val maxYMarkWidth = remember(yGuideResults) {
            yGuideResults.fold(0) { width, tlr -> maxOf(width, tlr.size.width) }
        }
        val maxYMarkHeight = remember(yGuideResults) {
            yGuideResults.fold(0) { height, tlr -> maxOf(height, tlr.size.height) }
        }

        val verticalLines = xAxisGuideCount + 1
        val xGuideResults = remember(verticalLines, xLabelProvider) {
            (0 until verticalLines)
                .map { index ->
                    xLabelProvider(index)
                        .takeIf { it.isNotBlank() }
                        ?.let { textMeasurer.measure(it, xLabelStyle) }
                }
        }

        val maxXMarkHeight = remember(xGuideResults) {
            xGuideResults
                .filterNotNull()
                .fold(0) { height, tlr -> maxOf(height, tlr.size.height) }
        }

        val insets = remember(xGuideResults, yGuideResults) {
            ChartInsets(
                maxYMarkWidth.toFloat(),
                maxYMarkHeight.toFloat(),
                maxYMarkWidth * 2F,
                max(maxYMarkHeight, maxXMarkHeight).toFloat(),
            )
        }

        val hMultiplier = -(fHeight - insets.vertical) / topValue

        val vLineStep = (fWidth - insets.horizontal) / verticalLines


        val gridConfig = remember(xGuideResults, yGuideResults) {
            GridConfig(
                horizontalLines = horizontalLines,
                hLineStep = hLineStep,
                yGuideResults = yGuideResults,
                verticalLines = verticalLines,
                vLineStep = vLineStep,
                xGuideResults = xGuideResults,
                color = gridColor,
            )
        }

        CoordinateGrid(
            gridConfig,
            insets,
            hMultiplier,
        )


        val hItemStep = (fWidth - insets.horizontal) / values.size

        val iconSize = remember(zeroDataIcon, hItemStep) {
            zeroDataIcon.intrinsicSize * (barWidth * hItemStep / zeroDataIcon.intrinsicSize.width)
        }

        val barsConfig = remember(hItemStep, iconSize, barColor, barSelectionColor, barWidth) {
            BarConfig(
                itemStep = hItemStep,
                cornerRadius = cornerRadius,
                barColor = barColor,
                barWidth = barWidth,
                iconSize = iconSize,
                zeroDataIcon = zeroDataIcon,
                barSelectionColor = barSelectionColor,
            )
        }

        DrawValueBars(
            values,
            insets,
            barsConfig,
            hMultiplier * barScale,
            selectedItemIndex,
            toggleSelection = {
                selectedItemIndex = if (it == selectedItemIndex) -1 else it
            },
        )

        if (selectedItemIndex != -1) {
            Layout(
                modifier = Modifier
                    .fillMaxWidth()
                    .fillMaxHeight(0.75F),
                content = {
                    markerContent(values[selectedItemIndex])
                },
                measurePolicy = { measurables, constraints ->

                    val placeable = measurables
                        .first()
                        .measure(constraints.copy(minWidth = 0, minHeight = 0))

                    layout(constraints.maxWidth, constraints.maxHeight) {

                        val top = insets
                            .top
                            .toInt()
                            .takeIf { it + placeable.height <= constraints.maxHeight }
                            ?: 0

                        val baseStart =
                            (insets.left + selectedItemIndex * hItemStep - placeable.width / 2 + hItemStep / 2)

                        val left = baseStart
                            .toInt()
                            .let {
                                if (it + placeable.width > constraints.maxWidth) constraints.maxWidth - placeable.width
                                else it
                            }
                            .takeIf { it > 0 }
                            ?: 0

                        placeable.placeRelative(left, top)
                    }
                }
            )
        }

    }
}


@Composable
private fun DrawValueBars(
    values: List<IntChartValue>,
    insets: ChartInsets,
    config: BarConfig,
    hMultiplier: Float,
    selectedIndex: Int,
    toggleSelection: (Int) -> Unit,
) {
    Canvas(
        modifier = Modifier
            .fillMaxSize()
            .pointerInput(Unit) {
                detectTapGestures { tapOffset ->
                    if (tapOffset in Rect(
                            insets.left,
                            insets.top,
                            size.width - insets.right,
                            size.height - insets.bottom
                        )
                    ) {
                        val x = tapOffset.x
                        val index = (ceil(x - insets.left) / config.itemStep).toInt()
                        toggleSelection(index)
                    }
                }
            }
    ) {

        val fHeight = size.height

        val itemStep = config.itemStep

        values.forEachIndexed { index, item ->

            if (index == selectedIndex) {
                drawRect(
                    color = config.barSelectionColor,
                    topLeft = Offset(
                        insets.left + index * itemStep,
                        insets.top,
                    ),
                    size = Size(
                        itemStep,
                        fHeight - insets.vertical,
                    )
                )
            }


            if (item.isEmpty) {

                val left = insets.left + index * itemStep + (itemStep - config.iconSize.width) / 2
                val top = insets.bottom + config.iconSize.height
                translate(left = left, fHeight - top) {
                    with(config.zeroDataIcon) {
                        draw(size = config.iconSize)
                    }
                }
            } else {
                val path = Path()
                    .apply {
                        addRoundRect(
                            RoundRect(
                                rect = Rect(
                                    offset = Offset(
                                        insets.left + index * itemStep + ((itemStep - itemStep * config.barWidth) / 2),
                                        fHeight - insets.bottom,
                                    ),
                                    size = Size(
                                        itemStep * config.barWidth,
                                        item.value * hMultiplier
                                    ),
                                ),
                                topLeft = config.cornerRadius,
                                topRight = config.cornerRadius,
                            )
                        )
                    }
                drawPath(path, color = config.barColor)
            }
        }
    }
}

@Composable
private fun CoordinateGrid(
    config: GridConfig,
    insets: ChartInsets,
    hMultiplier: Float,
) {
    val pathEffect = remember { PathEffect.dashPathEffect(floatArrayOf(20F, 10F), 0F) }

    Canvas(modifier = Modifier.fillMaxSize()) {

        val fHeight = size.height
        val fWidth = size.width
        val oneDp = 1.dp.toPx()

        (0 until config.horizontalLines)
            .forEach { index ->
                val lineY =
                    (fHeight - insets.bottom) + index * hMultiplier * config.hLineStep

                drawLine(
                    config.color,
                    start = Offset(insets.left, lineY),
                    end = Offset(fWidth - insets.right, lineY),
                    strokeWidth = config.lineThickness.toPx()
                )

                val textLayoutResult = config.yGuideResults[index]

                drawText(
                    textLayoutResult,
                    topLeft = Offset(
                        fWidth - 3 * insets.right / 4,
                        lineY - textLayoutResult.size.height / 2F,
                    )
                )
            }

        val yTop = insets.top

        (0 until config.verticalLines)
            .forEach { index ->

                val x = insets.left + config.vLineStep * index
                val labelResult = config.xGuideResults[index]
                val yOffset = insets.bottom
                    .takeIf { index == 0 || labelResult == null }
                    ?: 0F
                val yBottom = fHeight - yOffset

                drawLine(
                    Color.LightGray,
                    start = Offset(x, yTop),
                    end = Offset(x, yBottom),
                    strokeWidth = oneDp,
                    pathEffect = pathEffect.takeIf { index > 0 }
                )

                if (labelResult != null) {
                    drawText(
                        labelResult,
                        topLeft = Offset(
                            x + oneDp,
                            fHeight - insets.bottom + oneDp
                        )
                    )
                }
            }
    }
}


@JvmInline
value class ValueItem(override val value: Int) : IntChartValue

@Preview(showBackground = true, showSystemUi = true)
@Composable
fun BarChartPreview() {

    val easing = remember { CubicBezierEasing(0.6F, 0.2F, 0.6F, 1.4F) }
    var target by remember { mutableFloatStateOf(1F) }

    val displayAnimation = animateFloatAsState(
        targetValue = target,
        label = "appearing1",
        animationSpec = tween(700, easing = easing),
    )

    LaunchedEffect(Unit) { target = 1F }

    BarChart(
        modifier = Modifier
//            .padding(innerPadding)
            .fillMaxWidth()
            .fillMaxHeight(0.5F),
        values = listOf(
            ValueItem(31),
            ValueItem(2),
            ValueItem(13),
            ValueItem(-1),
            ValueItem(7),
            ValueItem(26),
            ValueItem(-1),
            ValueItem(8),
            ValueItem(5),
            ValueItem(4),
            ValueItem(6),
            ValueItem(17),
        ),
        yAxisGuideCount = 1,
        yAxisStepRoundMultiplier = {
            when {
                it > 5000 -> 1000
                it > 1000 -> 500
                it > 500 -> 100
                it > 100 -> 50
                it > 50 -> 10
                it > 10 -> 5
                else -> 1
            }
        },
        xAxisGuideCount = 3,
        xLabelStyle = TextStyle(
            Color.DarkGray,
            fontSize = 16.sp,
            fontWeight = FontWeight.SemiBold,
        ),
        yLabelStyle = TextStyle(
            Color.DarkGray,
            fontSize = 14.sp,
            fontWeight = FontWeight.SemiBold,
        ),
        xLabelProvider = {
            when (it) {
                0 -> "12:00"
                1 -> "12:15"
                2 -> "12:30"
                3 -> "12:45"
                else -> ""
            }
        },
        barScale = displayAnimation.value
    ) {
        ChartMarker(
            data = "Item with value: ${it.value}",
            modifier = Modifier.wrapContentSize()
        )
    }
}