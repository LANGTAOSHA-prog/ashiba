package com.example.ui.components

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.PathEffect
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.graphics.nativeCanvas
import androidx.compose.ui.unit.dp
import com.example.model.PlanCandidate
import kotlin.math.max

@Composable
fun PlanVisualCanvas(
    wallWidthMm: Double,
    idealOffsetMm: Double,
    candidate: PlanCandidate?,
    modifier: Modifier = Modifier
) {
    Box(
        modifier = modifier
            .fillMaxWidth()
            .height(220.dp)
            .background(Color(0xFF0F172A), RoundedCornerShape(16.dp))
            .padding(8.dp)
    ) {
        if (candidate == null || candidate.spans.isEmpty()) {
            Text(
                text = "寸法を入力して割付結果を表示",
                color = Color.White.copy(alpha = 0.6f),
                modifier = Modifier.align(Alignment.Center)
            )
        } else {
            Canvas(modifier = Modifier.fillMaxSize()) {
                val canvasWidth = size.width
                val canvasHeight = size.height

                // Draw blueprint grid lines
                val gridSpacing = 20.dp.toPx()
                for (x in 0..(canvasWidth / gridSpacing).toInt()) {
                    drawLine(
                        color = Color(0x1A3B82F6),
                        start = Offset(x * gridSpacing, 0f),
                        end = Offset(x * gridSpacing, canvasHeight),
                        strokeWidth = 1f
                    )
                }
                for (y in 0..(canvasHeight / gridSpacing).toInt()) {
                    drawLine(
                        color = Color(0x1A3B82F6),
                        start = Offset(0f, y * gridSpacing),
                        end = Offset(canvasWidth, y * gridSpacing),
                        strokeWidth = 1f
                    )
                }

                // Coordinate scale
                val totalSpanSum = candidate.totalScaffoldWidthMm.toFloat()
                val totalLength = max(wallWidthMm.toFloat() + 1000f, totalSpanSum + 1000f)
                val paddingX = 40.dp.toPx()
                val availableWidth = canvasWidth - (paddingX * 2)
                val scale = availableWidth / totalLength

                val wallPixelWidth = (wallWidthMm.toFloat()) * scale
                val wallStartX = (canvasWidth - wallPixelWidth) / 2f
                val wallEndX = wallStartX + wallPixelWidth
                val wallY = canvasHeight * 0.42f

                // Draw Wall (Outer Building Face)
                drawRect(
                    color = Color(0xFF334155),
                    topLeft = Offset(wallStartX, wallY - 18.dp.toPx()),
                    size = Size(wallPixelWidth, 18.dp.toPx())
                )
                drawLine(
                    color = Color(0xFF94A3B8),
                    start = Offset(wallStartX, wallY),
                    end = Offset(wallEndX, wallY),
                    strokeWidth = 3.dp.toPx()
                )

                // Wall dimension line
                drawDimensionLine(
                    startX = wallStartX,
                    endX = wallEndX,
                    y = wallY - 26.dp.toPx(),
                    label = "外壁幅: ${wallWidthMm.toInt()} mm",
                    color = Color(0xFFCBD5E1)
                )

                // Scaffolding Line & Bays
                val scaffoldPixelWidth = totalSpanSum * scale
                val scaffoldStartX = (canvasWidth - scaffoldPixelWidth) / 2f
                val scaffoldY = canvasHeight * 0.72f

                // Draw Scaffolding Run
                drawLine(
                    color = Color(0xFFF59E0B),
                    start = Offset(scaffoldStartX, scaffoldY),
                    end = Offset(scaffoldStartX + scaffoldPixelWidth, scaffoldY),
                    strokeWidth = 3.dp.toPx()
                )

                // Draw Inner Plank / Scaffold Deck area
                val plankPixelDepth = 14.dp.toPx()
                drawRect(
                    color = Color(0x33F59E0B),
                    topLeft = Offset(scaffoldStartX, scaffoldY - plankPixelDepth),
                    size = Size(scaffoldPixelWidth, plankPixelDepth)
                )

                // Draw Individual Spans and Post Nodes
                var currentX = scaffoldStartX
                // Draw initial post node
                drawCircle(
                    color = Color(0xFF38BDF8),
                    radius = 4.dp.toPx(),
                    center = Offset(currentX, scaffoldY)
                )

                candidate.spans.forEachIndexed { index, spanMm ->
                    val spanPx = spanMm.toFloat() * scale
                    val nextX = currentX + spanPx

                    // Span divider & node
                    drawLine(
                        color = Color(0x66F59E0B),
                        start = Offset(nextX, scaffoldY - plankPixelDepth),
                        end = Offset(nextX, scaffoldY + 6.dp.toPx()),
                        strokeWidth = 1.5.dp.toPx()
                    )
                    drawCircle(
                        color = Color(0xFF38BDF8),
                        radius = 4.dp.toPx(),
                        center = Offset(nextX, scaffoldY)
                    )

                    // Span label
                    val midX = currentX + (spanPx / 2f)
                    drawNativeText(
                        text = "$spanMm",
                        x = midX,
                        y = scaffoldY + 16.dp.toPx(),
                        color = android.graphics.Color.WHITE,
                        textSize = 10.dp.toPx()
                    )

                    currentX = nextX
                }

                // Scaffolding Total Dimension Line
                drawDimensionLine(
                    startX = scaffoldStartX,
                    endX = scaffoldStartX + scaffoldPixelWidth,
                    y = scaffoldY + 28.dp.toPx(),
                    label = "足場全幅: ${candidate.totalScaffoldWidthMm} mm",
                    color = Color(0xFF38BDF8)
                )

                // Clearance / Offset Dimension Line (Left & Right)
                val leftOffsetPx = (wallStartX - scaffoldStartX)
                if (leftOffsetPx > 0) {
                    drawLine(
                        color = Color(0xFF10B981),
                        start = Offset(scaffoldStartX, wallY),
                        end = Offset(wallStartX, wallY),
                        strokeWidth = 1.5.dp.toPx(),
                        pathEffect = PathEffect.dashPathEffect(floatArrayOf(6f, 6f), 0f)
                    )
                    drawNativeText(
                        text = "離れ${candidate.actualOffsetMm.toInt()}mm",
                        x = scaffoldStartX + (leftOffsetPx / 2f),
                        y = wallY + 12.dp.toPx(),
                        color = android.graphics.Color.parseColor("#10B981"),
                        textSize = 9.dp.toPx()
                    )
                }
            }
        }
    }
}

private fun DrawScope.drawDimensionLine(
    startX: Float,
    endX: Float,
    y: Float,
    label: String,
    color: Color
) {
    val tickSize = 4.dp.toPx()
    // Horizontal main line
    drawLine(
        color = color,
        start = Offset(startX, y),
        end = Offset(endX, y),
        strokeWidth = 1.2.dp.toPx()
    )
    // Left & right ticks
    drawLine(color = color, start = Offset(startX, y - tickSize), end = Offset(startX, y + tickSize), strokeWidth = 1.5.dp.toPx())
    drawLine(color = color, start = Offset(endX, y - tickSize), end = Offset(endX, y + tickSize), strokeWidth = 1.5.dp.toPx())

    // Dimension label centered
    val midX = (startX + endX) / 2f
    drawNativeText(
        text = label,
        x = midX,
        y = y - 4.dp.toPx(),
        color = android.graphics.Color.argb(
            (color.alpha * 255).toInt(),
            (color.red * 255).toInt(),
            (color.green * 255).toInt(),
            (color.blue * 255).toInt()
        ),
        textSize = 10.dp.toPx()
    )
}

private fun DrawScope.drawNativeText(
    text: String,
    x: Float,
    y: Float,
    color: Int,
    textSize: Float
) {
    val paint = android.graphics.Paint().apply {
        this.color = color
        this.textSize = textSize
        this.textAlign = android.graphics.Paint.Align.CENTER
        this.isAntiAlias = true
        this.typeface = android.graphics.Typeface.DEFAULT_BOLD
    }
    drawContext.canvas.nativeCanvas.drawText(text, x, y, paint)
}
