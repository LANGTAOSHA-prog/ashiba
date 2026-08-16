package com.example.model

data class RoofSlopeResult(
    val slopeSump: Double, // 寸勾配 (e.g. 4.5寸)
    val angleDegree: Double,
    val runLengthMm: Double,
    val riseHeightMm: Double,
    val rafterLengthMm: Double,
    val requiresRoofScaffold: Boolean,
    val safetyNotice: String
)

data class WallTieCheckResult(
    val verticalSpanM: Double,
    val horizontalSpanM: Double,
    val calculatedAreaPerTieM2: Double,
    val maxAllowedAreaM2: Double,
    val isCompliant: Boolean,
    val recommendation: String,
    val totalRequiredTies: Int
)

data class UnitConversionResult(
    val millimeters: Double,
    val meters: Double,
    val shaku: Double, // 尺 (1尺 ≈ 303.03mm)
    val ken: Double,   // 間 (1間 = 6尺 ≈ 1818.18mm)
    val sun: Double,   // 寸 (1寸 = 1/10尺 ≈ 30.303mm)
    val tsubo: Double, // 坪 (1坪 ≈ 3.30578 m²)
    val squareMeters: Double
)
