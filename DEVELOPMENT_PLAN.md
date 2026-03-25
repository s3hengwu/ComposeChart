# ComposeChart 开发计划

基于 MPAndroidChart v3.1.0 功能的 Jetpack Compose 图表库开发计划

## 项目概述

**目标**: 在 ComposeChart 工程中实现 MPAndroidChart 的所有核心图表功能，使用 Jetpack Compose 和 Material Design 3

**参考**: MPAndroidChart (https://github.com/PhilJay/MPAndroidChart)

---

## Phase 1: 核心数据模型和基础架构

### 1.1 数据模型层

| 类名 | 功能 | 对应MPAndroidChart |
|------|------|-------------------|
| `ChartEntry` | 基础数据条目 | `Entry` |
| `LineEntry` | 折线图数据 | `Entry` |
| `BarEntry` | 柱状图数据 | `BarEntry` |
| `PieEntry` | 饼图数据 | `PieEntry` |
| `ScatterEntry` | 散点图数据 | `Entry` |
| `CandleEntry` | K线图数据 | `CandleEntry` |
| `RadarEntry` | 雷达图数据 | `RadarEntry` |
| `BubbleEntry` | 气泡图数据 | `BubbleEntry` |
| `ChartData` | 图表数据容器 | `ChartData` |
| `DataSet` | 数据集 | `DataSet` |
| `LineDataSet` | 折线数据集 | `LineDataSet` |
| `BarDataSet` | 柱状数据集 | `BarDataSet` |

### 1.2 核心组件

| 组件 | 功能 |
|------|------|
| `ChartState` | 图表状态管理（选中、缩放、拖动） |
| `ChartConfig` | 图表配置（背景、边距、描述） |
| `AxisConfig` | 坐标轴配置 |
| `LegendConfig` | 图例配置 |
| `AnimationConfig` | 动画配置 |
| `MarkerData` | 点击标记数据 |

### 1.3 基础图表组件

```
chart/
├── ChartContainer.kt          # 图表容器组件
├── ChartGestureState.kt       # 手势状态管理
├── ChartCanvas.kt             # Canvas绘制入口
├── components/
│   ├── XAxis.kt              # X轴组件
│   ├── YAxis.kt              # Y轴组件
│   ├── Legend.kt             # 图例组件
│   ├── Description.kt        # 描述组件
│   ├── MarkerView.kt         # 标记视图
│   └── LimitLine.kt          # 限制线
└── config/
    ├── ChartStyle.kt         # 图表样式
    ├── AxisStyle.kt          # 坐标轴样式
    └── LegendStyle.kt        # 图例样式
```

---

## Phase 2: 基础图表实现

### 2.1 LineChart (折线图)

**功能列表**:
- [x] 基础折线图
- [x] 多数据集折线图
- [x] 曲线模式 (Cubic Bezier)
- [x] 阶梯模式 (Stepped)
- [x] 水平曲线模式 (Horizontal Bezier)
- [x] 填充区域 (Filled)
- [x] 虚线样式 (Dashed Line)
- [x] 圆点/空心圆点
- [x] 高亮选中效果
- [x] 触摸手势（缩放、拖动）
- [x] 动画效果

**API 设计**:
```kotlin
@Composable
fun LineChart(
    data: LineChartData,
    modifier: Modifier = Modifier,
    chartStyle: LineChartStyle = LineChartStyle(),
    animation: ChartAnimation = ChartAnimation.default(),
    onValueSelected: (Entry, Highlight) -> Unit = null
)
```

### 2.2 BarChart (柱状图)

**功能列表**:
- [x] 基础柱状图
- [x] 多数据集柱状图
- [x] 堆叠柱状图 (Stacked)
- [x] 横向柱状图 (Horizontal)
- [x] 正负值柱状图
- [x] 渐变填充
- [x] 柱状间距
- [x] 柱状宽度
- [x] 动画效果

### 2.3 PieChart (饼图)

**功能列表**:
- [x] 基础饼图
- [x] 半圆饼图 (Half Pie)
- [x] 折线饼图 (PiePolylineChart)
- [x] 中心文本
- [x] 扇形间隙
- [x] 圆角
- [x] 旋转手势
- [x] 百分比显示
- [x] 选中效果

---

## Phase 3: 高级图表实现

### 3.1 ScatterChart (散点图)

**功能列表**:
- [x] 基础散点图
- [x] 多种形状 (Square, Circle, Triangle, Cross)
- [x] 形状内孔 (Shape Hole)
- [x] 自定义形状渲染器
- [x] 形状大小
- [x] 高亮效果

### 3.2 CandleStickChart (K线图)

**功能列表**:
- [x] 基础K线图
- [x] 上涨颜色 (绿色/红色)
- [x] 下跌颜色
- [x] 影线样式
- [x] 选中效果

### 3.3 RadarChart (雷达图)

**功能列表**:
- [x] 基础雷达图
- [x] 多数据集
- [x] 填充区域
- [x] 旋转手势
- [x] X轴标签
- [x] Y轴标签
- [x] 高亮效果

### 3.4 BubbleChart (气泡图)

**功能列表**:
- [x] 基础气泡图
- [x] 多数据集
- [x] 气泡大小 (size)
- [x] 颜色透明度
- [x] 高亮效果

---

## Phase 4: 组合图表和动画

### 4.1 CombinedChart (组合图)

**功能列表**:
- [x] 折线 + 柱状组合
- [x] 散点 + K线组合
- [x] 气泡组合
- [x] 绘制顺序控制
- [x] 各类型数据独立配置

### 4.2 动画系统

**支持的动画**:
- `animateX()` - X轴方向动画
- `animateY()` - Y轴方向动画
- `animateXY()` - 双向动画
- `spin()` - 旋转动画 (PieChart)

**缓动函数**:
```kotlin
enum class Easing {
    Linear,
    EaseInQuad,
    EaseOutQuad,
    EaseInOutQuad,
    EaseInCubic,
    EaseOutCubic,
    EaseInOutCubic,
    // ... 更多缓动函数
}
```

---

## Phase 5: 通用功能

### 5.1 坐标轴系统

| 功能 | 描述 |
|------|------|
| XAxis | X轴配置（位置、标签、网格线） |
| YAxis | Y轴配置（位置、标签、网格线） |
| AxisDependency | 轴依赖关系 |
| LimitLine | 限制线 |
| IAxisValueFormatter | 自定义轴值格式化 |

### 5.2 图例系统

| 功能 | 描述 |
|------|------|
| Legend | 图例组件 |
| LegendEntry | 图例条目 |
| LegendForm | 图例形状（LINE, SQUARE, CIRCLE, EMPTY） |
| LegendVerticalAlignment | 垂直对齐 |
| LegendHorizontalAlignment | 水平对齐 |
| LegendOrientation | 方向（HORIZONTAL, VERTICAL） |

### 5.3 交互系统

| 功能 | 描述 |
|------|------|
| 触摸事件 | onPress, onMove, onRelease |
| 缩放 | PinchZoom, ScaleX, ScaleY |
| 拖动 | DragEnabled |
| 高亮 | Highlight |
| 选择监听 | OnChartValueSelectedListener |
| MarkerView | 点击显示的标记 |

### 5.4 样式系统

```kotlin
data class ChartColors(
    val primary: Color,
    val secondary: Color,
    val background: Color,
    val grid: Color,
    val text: Color,
    // ...
)

// 预设配色方案
object ColorTemplates {
    val VORDIPLOM_COLORS
    val JOYFUL_COLORS
    val COLORFUL_COLORS
    val LIBERTY_COLORS
    val PASTEL_COLORS
    val MATERIAL_COLORS
}
```

---

## 项目结构

```
app/src/main/java/com/chenxb/composechart/
├── composechart/
│   ├── ComposeChart.kt              # 主入口
│   ├── ChartContainer.kt            # 图表容器
│   ├── ChartState.kt                # 图表状态
│   │
│   ├── charts/
│   │   ├── LineChart.kt
│   │   ├── BarChart.kt
│   │   ├── PieChart.kt
│   │   ├── ScatterChart.kt
│   │   ├── CandleStickChart.kt
│   │   ├── RadarChart.kt
│   │   ├── BubbleChart.kt
│   │   └── CombinedChart.kt
│   │
│   ├── data/
│   │   ├── Entry.kt
│   │   ├── LineEntry.kt
│   │   ├── BarEntry.kt
│   │   ├── PieEntry.kt
│   │   ├── ScatterEntry.kt
│   │   ├── CandleEntry.kt
│   │   ├── RadarEntry.kt
│   │   ├── BubbleEntry.kt
│   │   ├── DataSet.kt
│   │   └── ChartData.kt
│   │
│   ├── components/
│   │   ├── Axis.kt
│   │   ├── Legend.kt
│   │   ├── LimitLine.kt
│   │   ├── MarkerView.kt
│   │   └── Description.kt
│   │
│   ├── style/
│   │   ├── ChartStyle.kt
│   │   ├── AxisStyle.kt
│   │   ├── LegendStyle.kt
│   │   └── ColorTemplates.kt
│   │
│   ├── animation/
│   │   ├── ChartAnimator.kt
│   │   └── Easing.kt
│   │
│   └── gestures/
│       ├── ChartGestures.kt
│       └── SelectionManager.kt
│
├── ui/
│   └── theme/
│
└── MainActivity.kt
```

---

## 开发优先级

### P0 - 核心功能 (MVP)
1. LineChart - 折线图
2. BarChart - 柱状图
3. PieChart - 饼图
4. 基础交互（缩放、拖动、选中）

### P1 - 重要功能
5. ScatterChart - 散点图
6. RadarChart - 雷达图
7. BubbleChart - 气泡图
8. 坐标轴和图例系统

### P2 - 高级功能
9. CandleStickChart - K线图
10. CombinedChart - 组合图
11. 动画系统
12. MarkerView

### P3 - 增强功能
13. 实时数据更新
14. 性能优化
15. 更多样式定制

---

## 技术栈

- **语言**: Kotlin
- **UI框架**: Jetpack Compose
- **最低SDK**: 28 (Android 9.0)
- **目标SDK**: 36
- **Compose BOM**: 2024.02.00
- **Material Design**: Material 3

---

## 参考文献

- [MPAndroidChart GitHub](https://github.com/PhilJay/MPAndroidChart)
- [Jetpack Compose 文档](https://developer.android.com/compose)
- [Material Design 3 指南](references/design-style-guide.md)
