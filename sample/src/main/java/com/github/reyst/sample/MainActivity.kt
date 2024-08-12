package com.github.reyst.sample

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.animation.core.CubicBezierEasing
import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.spring
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.SideEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.github.reyst.barchart.BarChart
import com.github.reyst.barchart.ValueItem
import com.github.reyst.sample.ui.theme.BarChartTheme

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            BarChartTheme {
                Scaffold(modifier = Modifier.fillMaxSize()) { innerPadding ->

                    val easing = remember { CubicBezierEasing(0.6F, 0.2F, 0.6F, 1.4F) }
                    var target by remember { mutableFloatStateOf(0F) }

                    val displayAnimation = animateFloatAsState(
                        targetValue = target,
                        label = "appearing1",
                        animationSpec = tween(700, easing = easing),
                    )

                    LaunchedEffect(Unit) { target = 1F }

                    BarChart(
                        modifier = Modifier
                            .padding(innerPadding)
                            .fillMaxWidth()
//                            .height(300.dp),
                            .fillMaxHeight(0.5F)
                            .background(Color.Red)
                            .padding(12.dp),


                        /*
                                values = (0..12)
                                    .map {
                                        when(it) {
                                            4 -> ValueItem(-1)
                                            8 -> ValueItem(0)
                                            3 -> ValueItem(6)
                                            6 -> ValueItem(32)
                                            10 -> ValueItem(17)
                                            else -> ValueItem(33 - it * 2)
                                        }

                                    },
                                barWidth = 0.60F,
                        */
                        values = listOf(
                            ValueItem(31),
                            ValueItem(2),
                            ValueItem(13),
                            ValueItem(-1),
                            ValueItem(7),
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
//                0 -> "12:00"
                                1 -> "12:15"
//                2 -> "12:30"
                                3 -> "12:45"
                                else -> ""
                            }
                        },
                        barScale = displayAnimation.value
                    )



//                    Greeting(
//                        name = "Android",
//                        modifier = Modifier.padding(innerPadding)
//                    )
                }
            }
        }
    }
}

@Composable
fun Greeting(name: String, modifier: Modifier = Modifier) {
    Text(
        text = "Hello $name!",
        modifier = modifier
    )
}

@Preview(showBackground = true)
@Composable
fun GreetingPreview() {
    BarChartTheme {
        Greeting("Android")
    }
}