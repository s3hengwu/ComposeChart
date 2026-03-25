# ComposeChart

基于 Jetpack Compose 的 Android 图表库，参考 [MPAndroidChart](https://github.com/PhilJay/MPAndroidChart) 设计制作。

## 功能特性

### 支持的图表类型

| 图表类型 | 说明 |
|---------|------|
| **LineChart** | 折线图，支持线性、贝塞尔曲线、阶梯等模式 |
| **BarChart** | 柱状图，支持分组、堆叠 |
| **PieChart** | 饼图，支持环形、中心文字、切片间距 |
| **RadarChart** | 雷达图/蛛网图 |
| **ScatterChart** | 散点图，支持多种形状（圆形、方形、三角形、十字等） |
| **BubbleChart** | 气泡图 |
| **CandleStickChart** | K线图/蜡烛图 |
| **CombinedChart** | 组合图，支持多种图表类型叠加显示 |

### 主要功能

- **坐标轴配置**
  - X轴/Y轴自定义标签
  - 网格线样式
  - 坐标轴反转
  - 标签旋转角度
  - 限制线 (LimitLine)

- **图例**
  - 多种显示形式
  - 自定义位置和样式

- **交互**
  - 点击选中
  - 缩放平移
  - 高亮显示

- **动画**
  - 入场动画
  - 多种缓动函数

- **样式**
  - 自定义颜色
  - 线条样式
  - 填充效果
  - 数值标签

## 快速开始

### 添加依赖

```groovy
dependencies {
    implementation("com.chenxb:composechart:1.0.0")
}
```

### 基本用法

#### 折线图

```kotlin
val entries = listOf(
    LineEntry(0f, 25f),
    LineEntry(1f, 30f),
    LineEntry(2f, 45f),
    LineEntry(3f, 40f)
)

val dataSet = LineDataSet(label = "销量", entries = entries).apply {
    color = Color.Blue
    lineWidth = 2f
    circleRadius = 4f
}

val chartData = LineChartData(listOf(dataSet))

LineChart(
    data = chartData,
    modifier = Modifier.fillMaxSize()
)
```

#### 柱状图

```kotlin
val entries = listOf(
    BarEntry(0f, 10f),
    BarEntry(1f, 20f),
    BarEntry(2f, 15f)
)

val dataSet = BarDataSet(label = "数据", entries = entries)
val chartData = BarChartData(listOf(dataSet))

BarChart(
    data = chartData,
    modifier = Modifier.fillMaxSize()
)
```

#### 限制线

```kotlin
val limitLines = LimitLines()
    .addLimitLine(
        LimitLine(
            limit = 60f,
            label = "目标线",
            lineColor = Color.Red,
            lineWidth = 2f
        )
    )

LineChart(
    data = chartData,
    yAxisConfig = YAxisConfig(limitLines = limitLines)
)
```

## 项目结构

```
composechart/
├── charts/              # 图表组件
│   ├── LineChart.kt
│   ├── BarChart.kt
│   ├── PieChart.kt
│   ├── RadarChart.kt
│   ├── ScatterChart.kt
│   ├── BubbleChart.kt
│   ├── CandleStickChart.kt
│   └── CombinedChart.kt
├── components/          # 公共组件
│   ├── Axis.kt          # 坐标轴配置
│   ├── LimitLine.kt     # 限制线
│   ├── Legend.kt        # 图例
│   └── Highlight.kt     # 高亮
├── data/               # 数据模型
├── style/              # 样式配置
├── animation/          # 动画
└── gestures/           # 手势处理
```

## 参考

本项目参考了以下开源项目的设计思路和 API 风格：

- [MPAndroidChart](https://github.com/PhilJay/MPAndroidChart) - Android 平台最流行的图表库

## License

MIT License
