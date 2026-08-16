package com.example.model

enum class SystemModule(val displayName: String, val standardSpans: List<Int>) {
    METRIC_STANDARD("メートル規格 (Metric 1800)", listOf(1800, 1500, 1200, 900, 600, 450, 300, 150)),
    INCH_STANDARD("インチ規格 (Inch 1829)", listOf(1829, 1524, 1219, 914, 610, 305)),
    KUSABI_COMPACT("狭小地・短スパン (Compact)", listOf(1200, 900, 600, 450, 300, 150))
}

data class PlanInput(
    val wallWidthMm: Double = 7200.0,
    val idealOffsetMm: Double = 250.0,
    val systemModule: SystemModule = SystemModule.METRIC_STANDARD,
    val allow150: Boolean = true,
    val plankWidthMm: Int = 400 // 400mm / 500mm / 240mm
)

data class PlanCandidate(
    val id: String,
    val title: String,
    val description: String,
    val spans: List<Int>,
    val totalScaffoldWidthMm: Int,
    val actualOffsetMm: Double,
    val offsetDiffMm: Double, // actualOffset - idealOffset
    val postCount: Int,
    val plankCountPerTier: Int,
    val handrailCountPerTier: Int,
    val toeBoardCountPerTier: Int,
    val isOptimal: Boolean = false
) {
    val spanBreakdownText: String
        get() {
            if (spans.isEmpty()) return "なし"
            val counts = spans.groupingBy { it }.eachCount().toSortedMap(compareByDescending { it })
            return counts.entries.joinToString(" + ") { "${it.key}×${it.value}" }
        }
}
