# ComposeChart 功能完善计划

## 当前进度

- 核心功能完成度: **80-85%**
- 8种图表类型全部实现
- 基础交互、动画、样式已具备

---

## 阶段一：高优先级功能 (核心体验)

### 1.1 高亮十字线增强
**文件**: `components/Highlight.kt`, `charts/LineChart.kt`, `charts/BarChart.kt`

**新增配置**:
```kotlin
// HighlightStyle.kt (新建)
data class HighlightStyle(
    val drawHorizontalHighlightLine: Boolean = true,
    val drawVerticalHighlightLine: Boolean = true,
    val highlightLineWidth: Float = 1f,
    val highlightLineColor: Color = Color.Gray,
    val highlightLineDashPathEffect: PathEffect? = null
)
```

**任务**:
- [ ] 创建 `HighlightStyle.kt`
- [ ] LineChart 添加十字高亮线绘制
- [ ] BarChart 添加垂直高亮线
- [ ] CombinedChart 支持高亮
- [ ] ScatterChart 支持高亮
- [ ] CandleStickChart 添加垂直高亮线

---

### 1.2 惯性拖拽
**文件**: `gestures/ChartGestures.kt`

**新增配置**:
```kotlin
data class ChartGestureState(
    // 现有字段...
    val dragDecelerationEnabled: Boolean = true,
    val dragDecelerationFrictionCoef: Float = 0.935f
)
```

**任务**:
- [ ] 实现 Fling 手势的惯性衰减
- [ ] 添加摩擦系数配置
- [ ] 各图表应用惯性效果

---

### 1.3 空数据提示
**文件**: `components/EmptyDataView.kt` (新建)

**新增配置**:
```kotlin
data class EmptyDataConfig(
    val text: String = "No chart data available",
    val textColor: Color = Color.Gray,
    val textSize: Float = 12f,
    val icon: ImageVector? = null
)
```

**任务**:
- [ ] 创建 `EmptyDataView.kt`
- [ ] 所有图表添加空数据检测
- [ ] 显示空数据提示

---

### 1.4 Description 描述文字
**文件**: `components/ChartDescription.kt` (新建)

**新增配置**:
```kotlin
data class ChartDescription(
    val text: String = "",
    val textColor: Color = Color.Gray,
    val textSize: Float = 8f,
    val position: DescriptionPosition = DescriptionPosition.BOTTOM_RIGHT,
    val offsetX: Float = 0f,
    val offsetY: Float = 0f
)
```

**任务**:
- [ ] 创建 `ChartDescription.kt`
- [ ] 各图表添加 description 参数
- [ ] 实现描述文字绘制

---

## 阶段二：中优先级功能 (功能完善)

### 2.1 水平柱状图
**文件**: `charts/HorizontalBarChart.kt` (新建)

**任务**:
- [ ] 创建 `HorizontalBarChart.kt`
- [ ] 实现 X/Y 轴交换逻辑
- [ ] 调整坐标计算
- [ ] 添加 Demo 示例

---

### 2.2 缩放/移动动画
**文件**: `gestures/ChartGestures.kt`

**新增方法**:
```kotlin
fun zoomAndCenterAnimated(
    scaleX: Float,
    scaleY: Float,
    centerX: Float,
    centerY: Float,
    durationMillis: Int = 500
)

fun moveViewToAnimated(
    x: Float,
    y: Float,
    durationMillis: Int = 500
)
```

**任务**:
- [ ] 实现缩放动画
- [ ] 实现移动动画
- [ ] 添加动画缓动

---

### 2.3 独立轴缩放控制
**文件**: `gestures/ChartGestures.kt`, 各图表组件

**新增配置**:
```kotlin
data class ChartGestureState(
    // 现有字段...
    val scaleXEnabled: Boolean = true,
    val scaleYEnabled: Boolean = true,
    val dragXEnabled: Boolean = true,
    val dragYEnabled: Boolean = true
)
```

**任务**:
- [ ] 添加独立缩放开关
- [ ] 添加独立拖拽方向开关
- [ ] 更新手势处理逻辑

---

### 2.4 零线绘制
**文件**: `components/Axis.kt`, `components/YAxisView.kt`

**新增配置**:
```kotlin
data class YAxisConfig(
    // 现有字段...
    val zeroLineColor: Color = Color.Gray,
    val zeroLineWidth: Float = 1f,
    val isDrawZeroLineEnabled: Boolean = false
)
```

**任务**:
- [ ] YAxisConfig 添加零线配置
- [ ] YAxisView 实现零线绘制
- [ ] 各图表应用零线

---

### 2.5 饼图值引导线
**文件**: `charts/PieChart.kt`

**新增配置**:
```kotlin
data class PieChartStyle(
    // 现有字段...
    val isDrawValueLinesEnabled: Boolean = false,
    val valueLineColor: Color = Color.Gray,
    val valueLineWidth: Float = 1f,
    val valueLineLength: Float = 20f,
    val valueLinePart1Length: Float = 0.3f,
    val valueLinePart2Length: Float = 0.4f
)
```

**任务**:
- [ ] 添加引导线配置
- [ ] 实现引导线绘制逻辑
- [ ] 标签位置调整

---

## 阶段三：低优先级功能 (锦上添花)

### 3.1 图表截图导出
**文件**: `ChartSaver.kt` (已存在，需增强)

**新增方法**:
```kotlin
suspend fun saveToGallery(
    bitmap: Bitmap,
    context: Context,
    title: String = "Chart"
): Boolean

suspend fun saveToPath(
    bitmap: Bitmap,
    path: String,
    format: Bitmap.CompressFormat = Bitmap.CompressFormat.PNG
): Boolean
```

**任务**:
- [ ] 添加保存到相册功能
- [ ] 添加保存到指定路径功能
- [ ] 添加权限处理

---

### 3.2 多点高亮
**文件**: `components/Highlight.kt`

**任务**:
- [ ] 支持同时高亮多个数据点
- [ ] 添加高亮列表管理
- [ ] 更新高亮绘制逻辑

---

### 3.3 高亮拖拽
**文件**: `charts/LineChart.kt`, `gestures/ChartGestures.kt`

**任务**:
- [ ] 拖拽时持续高亮最近数据点
- [ ] 添加高亮拖拽开关
- [ ] 实现平滑过渡

---

### 3.4 折线图渐变颜色
**文件**: `data/LineDataSet.kt`, `charts/LineChart.kt`

**新增配置**:
```kotlin
data class LineDataSet(
    // 现有字段...
    val isGradientLineColorEnabled: Boolean = false,
    val gradientLineColorStart: Color = Color.Blue,
    val gradientLineColorEnd: Color = Color.Cyan
)
```

**任务**:
- [ ] 添加渐变颜色配置
- [ ] 实现渐变线条绘制

---

### 3.5 自定义填充渲染器
**文件**: `components/FillFormatter.kt` (新建)

```kotlin
interface FillFormatter {
    fun getFillLinePosition(dataSet: LineDataSet, chartData: LineChartData, maxY: Float, minY: Float): Float
}
```

**任务**:
- [ ] 创建 FillFormatter 接口
- [ ] 实现默认填充格式化器
- [ ] LineChart 支持自定义填充

---

### 3.6 饼图切片圆角
**文件**: `charts/PieChart.kt`

**任务**:
- [ ] 实现 `isDrawRoundedSlicesEnabled` 绘制
- [ ] 调整切片绘制路径

---

### 3.7 自定义轴依赖
**文件**: `data/LineDataSet.kt`

**新增配置**:
```kotlin
enum class AxisDependency { LEFT, RIGHT }

data class LineDataSet(
    // 现有字段...
    val axisDependency: AxisDependency = AxisDependency.LEFT
)
```

**任务**:
- [ ] 添加轴依赖配置
- [ ] 根据依赖选择 Y 轴范围计算

---

### 3.8 图标替代数据点
**文件**: `charts/LineChart.kt`, `charts/ScatterChart.kt`

**任务**:
- [ ] Entry 支持 icon 字段
- [ ] 实现图标绘制
- [ ] 添加图标大小配置

---

### 3.9 首尾标签裁剪避免
**文件**: `components/XAxisView.kt`

**任务**:
- [ ] 添加 `avoidFirstLastClipping` 配置
- [ ] 调整首尾标签位置

---

### 3.10 屏幕旋转保持位置
**文件**: 各图表组件

**任务**:
- [ ] 添加 `keepPositionOnRotation` 配置
- [ ] 保存/恢复图表状态

---

## 阶段四：代码质量与测试

### 4.1 单元测试
**文件**: `test/` 目录

**任务**:
- [ ] 数据模型测试
- [ ] 坐标计算测试
- [ ] 范围计算测试
- [ ] 格式化器测试

---

### 4.2 性能优化
**任务**:
- [ ] 大数据集性能测试
- [ ] 内存泄漏检查
- [ ] 绘制性能优化

---

### 4.3 文档完善
**任务**:
- [ ] API 文档
- [ ] 使用示例
- [ ] 迁移指南

---

## 实施顺序

```
Week 1: 阶段一 (1.1-1.4)
├── 高亮十字线
├── 惯性拖拽
├── 空数据提示
└── Description 描述

Week 2: 阶段二 (2.1-2.3)
├── 水平柱状图
├── 缩放/移动动画
└── 独立轴缩放

Week 3: 阶段二 (2.4-2.5)
├── 零线绘制
└── 饼图引导线

Week 4: 阶段三 (3.1-3.5)
├── 图表截图
├── 多点高亮
├── 高亮拖拽
├── 渐变颜色
└── 填充渲染器

Week 5: 阶段三 (3.6-3.10) + 阶段四
├── 剩余功能
├── 测试
└── 文档
```

---

## 文件变更预估

| 类型 | 数量 |
|-----|------|
| 新建文件 | 6 个 |
| 修改文件 | 15 个 |
| 新增代码行 | ~2000 行 |

---

## 验收标准

- [ ] 所有新功能有 Demo 展示
- [ ] 编译无警告
- [ ] 性能无明显下降
- [ ] API 命名与 MPAndroidChart 保持一致
