package com.example.ui.components

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
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
import com.example.model.SectionResult
import kotlin.math.max

@Composable
fun SectionVisualCanvas(
    result: SectionResult?,
    modifier: Modifier = Modifier
) {
    Box(
        modifier = modifier
            .fillMaxWidth()
            .height(280.dp)
            .background(Color(0xFF0F172A), RoundedCornerShape(16.dp))
            .padding(8.dp)
    ) {
        if (result == null) {
            Text(
                text = "高さを入力して断面割付を表示",
                color = Color.White.copy(alpha = 0.6f),
                modifier = Modifier.align(Alignment.Center)
            )
        } else {
            Canvas(modifier = Modifier.fillMaxSize()) {
                val canvasWidth = size.width
                val canvasHeight = size.height

                // Grid background
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

                val groundY = canvasHeight - 28.dp.toPx()
                val totalHeightMm = max(result.targetHeightMm.toFloat(), result.totalScaffoldHeightMm.toFloat()) + 600f
                val availableHeight = groundY - 30.dp.toPx()
                val scale = availableHeight / totalHeightMm

                // Ground Line (GL)
                drawLine(
                    color = Color(0xFF64748B),
                    start = Offset(10.dp.toPx(), groundY),
                    end = Offset(canvasWidth - 10.dp.toPx(), groundY),
                    strokeWidth = 2.5.dp.toPx()
                )
                drawNativeText(
                    text = "GL ±0",
                    x = 30.dp.toPx(),
                    y = groundY + 16.dp.toPx(),
                    color = android.graphics.Color.LTGRAY,
                    textSize = 9.dp.toPx()
                )

                // Building Profile (on the right)
                val buildingLeftX = canvasWidth * 0.65f
                val buildingHeightPx = result.targetHeightMm.toFloat() * scale
                val buildingTopY = groundY - buildingHeightPx

                drawRect(
                    color = Color(0xFF1E293B),
                    topLeft = Offset(buildingLeftX, buildingTopY),
                    size = Size(canvasWidth - buildingLeftX - 10.dp.toPx(), buildingHeightPx)
                )
                // Building Eave Line
                drawLine(
                    color = Color(0xFFEF4444),
                    start = Offset(buildingLeftX - 15.dp.toPx(), buildingTopY),
                    end = Offset(canvasWidth - 10.dp.toPx(), buildingTopY),
                    strokeWidth = 2.dp.toPx(),
                    pathEffect = PathEffect.dashPathEffect(floatArrayOf(8f, 6f), 0f)
                )
                drawNativeText(
                    text = "軒高 ${result.targetHeightMm.toInt()}mm",
                    x = buildingLeftX + 50.dp.toPx(),
                    y = buildingTopY - 6.dp.toPx(),
                    color = android.graphics.Color.parseColor("#EF4444"),
                    textSize = 9.dp.toPx()
                )

                // Scaffolding Section (Outer post & Inner post)
                val scaffoldBayWidthPx = 65.dp.toPx()
                val innerPostX = buildingLeftX - 25.dp.toPx()
                val outerPostX = innerPostX - scaffoldBayWidthPx

                // Base Jacks
                val jackHeightPx = result.jackLevelMm.toFloat() * scale
                val jackTopY = groundY - jackHeightPx

                // Outer jack
                drawRect(
                    color = Color(0xFF94A3B8),
                    topLeft = Offset(outerPostX - 8.dp.toPx(), groundY - 3.dp.toPx()),
                    size = Size(16.dp.toPx(), 3.dp.toPx())
                )
                drawLine(
                    color = Color(0xFFCBD5E1),
                    start = Offset(outerPostX, groundY),
                    end = Offset(outerPostX, jackTopY),
                    strokeWidth = 3.dp.toPx()
                )
                // Inner jack
                drawRect(
                    color = Color(0xFF94A3B8),
                    topLeft = Offset(innerPostX - 8.dp.toPx(), groundY - 3.dp.toPx()),
                    size = Size(16.dp.toPx(), 3.dp.toPx())
                )
                drawLine(
                    color = Color(0xFFCBD5E1),
                    start = Offset(innerPostX, groundY),
                    end = Offset(innerPostX, jackTopY),
                    strokeWidth = 3.dp.toPx()
                )

                // Outer & Inner vertical post standards
                val topScaffoldY = groundY - (result.totalScaffoldHeightMm.toFloat() * scale)
                drawLine(
                    color = Color(0xFF38BDF8),
                    start = Offset(outerPostX, jackTopY),
                    end = Offset(outerPostX, topScaffoldY),
                    strokeWidth = 2.5.dp.toPx()
                )
                drawLine(
                    color = Color(0xFF38BDF8),
                    start = Offset(innerPostX, jackTopY),
                    end = Offset(innerPostX, topScaffoldY),
                    strokeWidth = 2.5.dp.toPx()
                )

                // Base ties (根がらみ)
                drawLine(
                    color = Color(0xFFF59E0B),
                    start = Offset(outerPostX, jackTopY),
                    end = Offset(innerPostX, jackTopY),
                    strokeWidth = 2.dp.toPx()
                )

                // Draw each tier
                var prevPlankY = jackTopY
                result.tiers.forEach { tier ->
                    val deckY = groundY - (tier.plankElevationMm.toFloat() * scale)
                    val handrailY = groundY - (tier.handrailElevationMm.toFloat() * scale)
                    val toeBoardY = groundY - (tier.toeBoardElevationMm.toFloat() * scale)

                    // Plank deck (アンチ)
                    drawRect(
                        color = Color(0xFFF59E0B),
                        topLeft = Offset(outerPostX, deckY - 2.dp.toPx()),
                        size = Size(scaffoldBayWidthPx, 4.dp.toPx())
                    )

                    // Handrail
                    drawLine(
                        color = Color(0xFFFBBF24),
                        start = Offset(outerPostX, handrailY),
                        end = Offset(innerPostX, handrailY),
                        strokeWidth = 1.5.dp.toPx()
                    )

                    // Diagonal Brace between tiers
                    drawLine(
                        color = Color(0x8806B6D4),
                        start = Offset(outerPostX, prevPlankY),
                        end = Offset(innerPostX, deckY),
                        strokeWidth = 1.2.dp.toPx()
                    )
                    drawLine(
                        color = Color(0x8806B6D4),
                        start = Offset(innerPostX, prevPlankY),
                        end = Offset(outerPostX, deckY),
                        strokeWidth = 1.2.dp.toPx()
                    )

                    // Tier Elevation Callout on left side
                    drawLine(
                        color = Color(0x66FFFFFF),
                        start = Offset(outerPostX - 10.dp.toPx(), deckY),
                        end = Offset(outerPostX, deckY),
                        strokeWidth = 1.dp.toPx()
                    )
                    drawNativeText(
                        text = "${tier.tierNumber}層 +${tier.plankElevationMm}mm",
                        x = outerPostX - 45.dp.toPx(),
                        y = deckY + 3.dp.toPx(),
                        color = android.graphics.Color.WHITE,
                        textSize = 8.5.dp.toPx()
                    )

                    prevPlankY = deckY
                }

                // Jack annotation
                drawNativeText(
                    text = "Jack ${result.jackLevelMm}mm",
                    x = outerPostX + (scaffoldBayWidthPx / 2f),
                    y = groundY - 6.dp.toPx(),
                    color = android.graphics.Color.parseColor("#38BDF8"),
                    textSize = 8.5.dp.toPx()
                )
            }
        }
    }
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
