package com.chenxb.composechart

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.tooling.preview.Preview
import com.chenxb.composechart.composechart.animation.ChartAnimation
import com.chenxb.composechart.composechart.charts.*
import com.chenxb.composechart.composechart.components.XAxisConfig
import com.chenxb.composechart.composechart.components.YAxisConfig
import com.chenxb.composechart.composechart.components.LimitLine
import com.chenxb.composechart.composechart.components.LimitLines
import com.chenxb.composechart.composechart.components.LimitLabelPosition
import com.chenxb.composechart.composechart.data.*
import com.chenxb.composechart.composechart.style.ColorTemplates
import com.chenxb.composechart.composechart.style.LineChartStyle
import com.chenxb.composechart.composechart.style.BarChartStyle
import com.chenxb.composechart.composechart.style.PieChartStyle
import com.chenxb.composechart.composechart.style.ScatterChartStyle
import com.chenxb.composechart.composechart.style.RadarChartStyle
import com.chenxb.composechart.composechart.style.BubbleChartStyle
import com.chenxb.composechart.composechart.style.CandleChartStyle
import com.chenxb.composechart.ui.theme.ComposeChartTheme

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            ComposeChartTheme {
                Scaffold(modifier = Modifier.fillMaxSize()) { innerPadding ->
                    AllChartsDemo(
                        modifier = Modifier.padding(innerPadding)
                    )
                }
            }
        }
    }
}

/**
 * 所有图表类型的演示
 */
@Composable
fun AllChartsDemo(modifier: Modifier = Modifier) {
    LazyColumn(
        modifier = modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(24.dp)
    ) {
        item {
            ChartCard(title = "1. 折线图 (Line Chart)") {
                LineChartExample()
            }
        }
        item {
            ChartCard(title = "2. 多数据集折线图") {
                MultiLineChartExample()
            }
        }
        item {
            ChartCard(title = "3. 填充式折线图") {
                FilledLineChartExample()
            }
        }
        item {
            ChartCard(title = "4. 柱状图 (Bar Chart)") {
                BarChartExample()
            }
        }
        item {
            ChartCard(title = "5. 多数据集柱状图") {
                MultiBarChartExample()
            }
        }
        item {
            ChartCard(title = "6. 饼图 (Pie Chart)") {
                PieChartExample()
            }
        }
        item {
            ChartCard(title = "7. 散点图 (Scatter Chart)") {
                ScatterChartExample()
            }
        }
        item {
            ChartCard(title = "8. 雷达图 (Radar Chart)") {
                RadarChartExample()
            }
        }
        item {
            ChartCard(title = "9. 气泡图 (Bubble Chart)") {
                BubbleChartExample()
            }
        }
        item {
            ChartCard(title = "10. K线图 (CandleStick Chart)") {
                CandleStickChartExample()
            }
        }
        item {
            ChartCard(title = "11. 组合图 (Combined Chart)") {
                CombinedChartExample()
            }
        }
    }
}

@Composable
private fun ChartCard(
    title: String,
    content: @Composable () -> Unit
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White)
    ) {
        Column(
            modifier = Modifier.padding(16.dp)
        ) {
            Text(
                text = title,
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold,
                fontSize = 16.sp,
                modifier = Modifier.padding(bottom = 12.dp)
            )
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(200.dp)
            ) {
                content()
            }
        }
    }
}

// ==================== 1. 折线图 ====================
@Composable
private fun LineChartExample() {
    val entries = listOf(
        LineEntry(0f, 25f),
        LineEntry(1f, 30f),
        LineEntry(2f, 45f),
        LineEntry(3f, 40f),
        LineEntry(4f, 55f),
        LineEntry(5f, 50f),
        LineEntry(6f, 65f),
        LineEntry(7f, 60f),
        LineEntry(8f, 75f),
        LineEntry(9f, 70f)
    )

    val dataSet = LineDataSet(label = "销量", entries = entries).apply {
        color = ColorTemplates.HOLO_BLUE
        lineWidth = 2f
        circleRadius = 4f
        isDrawCircleHole = true
        circleHoleRadius = 2f
    }

    val chartData = LineChartData(listOf(dataSet))

    // 创建 LimitLines 并添加限制线
    val limitLines = LimitLines()
        .addLimitLine(
            LimitLine(
                limit = 60f,
                label = "目标线",
                lineColor = Color.Red,
                lineWidth = 2f,
                labelColor = Color.Red,
                labelTextSize = 12f,
                labelPosition = LimitLabelPosition.RIGHT_TOP
            )
        )
        .addLimitLine(
            LimitLine(
                limit = 35f,
                label = "最低线",
                lineColor = Color(0xFF4CAF50),
                lineWidth = 1.5f,
                labelColor = Color(0xFF4CAF50),
                labelTextSize = 10f,
                labelPosition = LimitLabelPosition.LEFT_TOP
            )
        )

    LineChart(
        data = chartData,
        modifier = Modifier.fillMaxSize(),
        xAxisConfig = XAxisConfig(
            labelCount = 10,
            granularity = 1f
        ),
        yAxisConfig = YAxisConfig(
            limitLines = limitLines
        )
    )
}

// ==================== 2. 多数据集折线图 ====================
@Composable
private fun MultiLineChartExample() {
    val entries1 = listOf(
        LineEntry(0f, 25f),
        LineEntry(1f, 30f),
        LineEntry(2f, 45f),
        LineEntry(3f, 40f),
        LineEntry(4f, 55f)
    )
    val entries2 = listOf(
        LineEntry(0f, 35f),
        LineEntry(1f, 25f),
        LineEntry(2f, 35f),
        LineEntry(3f, 50f),
        LineEntry(4f, 45f)
    )

    val dataSet1 = LineDataSet(label = "产品A", entries = entries1).apply {
        color = ColorTemplates.MATERIAL_COLORS[0]
        lineWidth = 2f
    }
    val dataSet2 = LineDataSet(label = "产品B", entries = entries2).apply {
        color = ColorTemplates.MATERIAL_COLORS[2]
        lineWidth = 2f
    }

    val chartData = LineChartData(listOf(dataSet1, dataSet2))

    LineChart(
        data = chartData,
        modifier = Modifier.fillMaxSize(),
        xAxisConfig = XAxisConfig(),
        yAxisConfig = YAxisConfig()
    )
}

// ==================== 3. 填充式折线图 ====================
@Composable
private fun FilledLineChartExample() {
    val entries = listOf(
        LineEntry(0f, 20f),
        LineEntry(1f, 35f),
        LineEntry(2f, 30f),
        LineEntry(3f, 45f),
        LineEntry(4f, 40f),
        LineEntry(5f, 55f)
    )

    val dataSet = LineDataSet(label = "收入", entries = entries).apply {
        color = ColorTemplates.JOYFUL_COLORS[0]
        lineWidth = 2f
        isDrawFilledEnabled = true
        fillColor = ColorTemplates.JOYFUL_COLORS[0].copy(alpha = 0.3f)
    }

    val chartData = LineChartData(listOf(dataSet))
    val style = LineChartStyle(isDrawFilled = true)

    LineChart(
        data = chartData,
        modifier = Modifier.fillMaxSize(),
        style = style,
        xAxisConfig = XAxisConfig(),
        yAxisConfig = YAxisConfig()
    )
}

// ==================== 4. 柱状图 ====================
@Composable
private fun BarChartExample() {
    val entries = listOf(
        BarEntry(0f, 30f),
        BarEntry(1f, 45f),
        BarEntry(2f, 35f),
        BarEntry(3f, 50f),
        BarEntry(4f, 40f),
        BarEntry(5f, 60f)
    )

    val dataSet = BarDataSet(label = "销售额", entries = entries).apply {
        color = ColorTemplates.MATERIAL_COLORS[4]
    }

    val chartData = BarChartData(listOf(dataSet))

    BarChart(
        data = chartData,
        modifier = Modifier.fillMaxSize(),
        xAxisConfig = XAxisConfig(),
        yAxisConfig = YAxisConfig()
    )
}

// ==================== 5. 多数据集柱状图 ====================
@Composable
private fun MultiBarChartExample() {
    val entries1 = listOf(
        BarEntry(0f, 30f),
        BarEntry(1f, 45f),
        BarEntry(2f, 35f)
    )
    val entries2 = listOf(
        BarEntry(0f, 25f),
        BarEntry(1f, 35f),
        BarEntry(2f, 40f)
    )

    val dataSet1 = BarDataSet(label = "2023", entries = entries1).apply {
        color = ColorTemplates.MATERIAL_COLORS[0]
    }
    val dataSet2 = BarDataSet(label = "2024", entries = entries2).apply {
        color = ColorTemplates.MATERIAL_COLORS[2]
    }

    val chartData = BarChartData(listOf(dataSet1, dataSet2))

    BarChart(
        data = chartData,
        modifier = Modifier.fillMaxSize(),
        xAxisConfig = XAxisConfig(),
        yAxisConfig = YAxisConfig()
    )
}

// ==================== 6. 饼图 ====================
@Composable
private fun PieChartExample() {
    val entries = listOf(
        PieEntry(0f, 30f, "手机"),
        PieEntry(1f, 25f, "电脑"),
        PieEntry(2f, 20f, "平板"),
        PieEntry(3f, 15f, "配件"),
        PieEntry(4f, 10f, "其他")
    )

    val dataSet = PieDataSet(label = "销售额", entries = entries).apply {
        color = ColorTemplates.VORDIPLOM_COLORS[0]
    }

    val chartData = PieChartData(listOf(dataSet))

    PieChart(
        data = chartData,
        modifier = Modifier.fillMaxSize(),
        style = PieChartStyle()
    )
}

// ==================== 7. 散点图 ====================
@Composable
private fun ScatterChartExample() {
    val entries = listOf(
        LineEntry(10f, 20f),
        LineEntry(15f, 35f),
        LineEntry(20f, 30f),
        LineEntry(25f, 45f),
        LineEntry(30f, 40f),
        LineEntry(35f, 55f),
        LineEntry(40f, 50f),
        LineEntry(45f, 65f),
        LineEntry(50f, 60f)
    )

    val dataSet = ScatterDataSet(label = "数据点", entries = entries).apply {
        color = ColorTemplates.MATERIAL_COLORS[1]
        scatterShape = ScatterDataSet.ScatterShape.CIRCLE
        scatterShapeSize = 15f
    }

    val chartData = ScatterChartData(listOf(dataSet))

    ScatterChart(
        data = chartData,
        modifier = Modifier.fillMaxSize(),
        style = ScatterChartStyle(),
        xAxisConfig = XAxisConfig(),
        yAxisConfig = YAxisConfig()
    )
}

// ==================== 8. 雷达图 ====================
@Composable
private fun RadarChartExample() {
    val entries = listOf(
        RadarEntry(0f, 70f),
        RadarEntry(1f, 85f),
        RadarEntry(2f, 60f),
        RadarEntry(3f, 90f),
        RadarEntry(4f, 75f),
        RadarEntry(5f, 80f)
    )

    val dataSet = RadarDataSet(label = "能力值", entries = entries).apply {
        fillColor = ColorTemplates.MATERIAL_COLORS[5].copy(alpha = 0.3f)
        lineWidth = 2f
    }

    val chartData = RadarChartData(listOf(dataSet))

    RadarChart(
        data = chartData,
        modifier = Modifier.fillMaxSize(),
        style = RadarChartStyle()
    )
}

// ==================== 9. 气泡图 ====================
@Composable
private fun BubbleChartExample() {
    val entries = listOf(
        BubbleEntry(0f, 30f, size = 20f),
        BubbleEntry(1f, 45f, size = 35f),
        BubbleEntry(2f, 35f, size = 25f),
        BubbleEntry(3f, 50f, size = 40f),
        BubbleEntry(4f, 40f, size = 30f),
        BubbleEntry(5f, 60f, size = 45f)
    )

    val dataSet = BubbleDataSet(label = "气泡", entries = entries).apply {
        color = ColorTemplates.MATERIAL_COLORS[3]
    }

    val chartData = BubbleChartData(listOf(dataSet))

    BubbleChart(
        data = chartData,
        modifier = Modifier.fillMaxSize(),
        style = BubbleChartStyle(),
        xAxisConfig = XAxisConfig(),
        yAxisConfig = YAxisConfig()
    )
}

// ==================== 10. K线图 ====================
@Composable
private fun CandleStickChartExample() {
    val entries = listOf(
        CandleEntry(0f, high = 25f, low = 15f, open = 20f, close = 22f),
        CandleEntry(1f, high = 30f, low = 18f, open = 22f, close = 28f),
        CandleEntry(2f, high = 28f, low = 20f, open = 25f, close = 21f),
        CandleEntry(3f, high = 35f, low = 22f, open = 24f, close = 32f),
        CandleEntry(4f, high = 40f, low = 28f, open = 33f, close = 38f),
        CandleEntry(5f, high = 38f, low = 25f, open = 35f, close = 27f)
    )

    val dataSet = CandleDataSet(label = "股票", entries = entries).apply {
        increasingColor = Color(0xFF4CAF50)
        decreasingColor = Color(0xFFF44336)
        neutralColor = Color(0xFF2196F3)
    }

    val chartData = CandleChartData(listOf(dataSet))

    CandleStickChart(
        data = chartData,
        modifier = Modifier.fillMaxSize(),
        style = CandleChartStyle(),
        xAxisConfig = XAxisConfig(),
        yAxisConfig = YAxisConfig()
    )
}

// ==================== 11. 组合图 ====================
@Composable
private fun CombinedChartExample() {
    val lineEntries = listOf(
        LineEntry(0f, 35f),
        LineEntry(1f, 45f),
        LineEntry(2f, 40f),
        LineEntry(3f, 55f),
        LineEntry(4f, 50f),
        LineEntry(5f, 65f)
    )
    val lineDataSet = LineDataSet(label = "增长率", entries = lineEntries).apply {
        color = ColorTemplates.MATERIAL_COLORS[1]
        lineWidth = 2f
    }
    val lineData = LineChartData(listOf(lineDataSet))

    val barEntries = listOf(
        BarEntry(0f, 30f),
        BarEntry(1f, 45f),
        BarEntry(2f, 35f),
        BarEntry(3f, 50f),
        BarEntry(4f, 40f),
        BarEntry(5f, 60f)
    )
    val barDataSet = BarDataSet(label = "销售额", entries = barEntries).apply {
        color = ColorTemplates.MATERIAL_COLORS[4].copy(alpha = 0.7f)
    }
    val barData = BarChartData(listOf(barDataSet))

    val combinedData = CombinedChartData().apply {
        this.lineData = lineData
        this.barData = barData
    }

    CombinedChart(
        data = combinedData,
        modifier = Modifier.fillMaxSize(),
        lineStyle = LineChartStyle(),
        barStyle = BarChartStyle(),
        xAxisConfig = XAxisConfig(),
        yAxisConfig = YAxisConfig()
    )
}

@Preview(showBackground = true)
@Composable
fun AllChartsDemoPreview() {
    ComposeChartTheme {
        AllChartsDemo()
    }
}
