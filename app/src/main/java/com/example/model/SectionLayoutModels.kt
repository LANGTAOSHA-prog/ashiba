package com.example.model

enum class ScaffoldType(val displayName: String, val commaPitchMm: Int, val description: String) {
    KUSABI_A("クサビAタイプ (Kusabi A-Type)", 450, "コマピッチ 450mm / 住宅・中層建築の標準規格"),
    KUSABI_B("クサビBタイプ (Kusabi B-Type)", 475, "コマピッチ 475mm / 信和・平和技研規格"),
    FRAME_1700("枠組足場 (Frame 1700)", 1700, "建枠高さ 1700mm / ビル・大型建築用"),
    FRAME_1900("枠組足場 (Frame 1900)", 1900, "建枠高さ 1900mm / 快適作業空間規格")
}

enum class TopSafetySpec(val displayName: String, val extraTopHeightMm: Int, val description: String) {
    STANDARD_HANDRAIL("天端手すり (900mm)", 900, "最上層アンチ上に900mm先行手すり設置"),
    DOUBLE_HANDRAIL_TOEBOARD("手すり(900mm) + 中桟 + 巾木", 900, "墜落・落下物防止基準適合(手すり+巾木150mm)"),
    HIGH_SCREEN_SHEET("メッシュシート / 朝顔防護", 1800, "最上階から1800mm立ち上げシート養生")
}

data class SectionInput(
    val targetHeightMm: Double = 6200.0,
    val firstTierHeightMm: Double = 1800.0,
    val standardTierHeightMm: Double = 1800.0,
    val scaffoldType: ScaffoldType = ScaffoldType.KUSABI_A,
    val topSafetySpec: TopSafetySpec = TopSafetySpec.DOUBLE_HANDRAIL_TOEBOARD,
    val jackBaseSettingMm: Double = 150.0,
    val groundOffsetLevelMm: Double = 0.0
)

data class TierInfo(
    val tierNumber: Int,
    val plankElevationMm: Int,
    val handrailElevationMm: Int,
    val toeBoardElevationMm: Int,
    val label: String
)

data class SectionResult(
    val targetHeightMm: Double,
    val totalScaffoldHeightMm: Int,
    val totalTiers: Int,
    val jackLevelMm: Int,
    val startCommaLevel: Int,
    val startCommaDescription: String,
    val outerPosts: List<Int>, // e.g. [3600, 2700, 900]
    val innerPosts: List<Int>, // e.g. [1800, 3600, 1800] staggered
    val tiers: List<TierInfo>,
    val outerPostSummary: String,
    val innerPostSummary: String,
    val topEaveMarginMm: Int
)
