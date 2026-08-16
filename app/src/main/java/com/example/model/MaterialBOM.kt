package com.example.model

enum class MaterialCategory(val displayName: String) {
    POSTS("支柱・建枠 (Posts/Columns)"),
    PLANKS("踏板・アンチ (Planks/Catwalks)"),
    HANDRAILS("手すり・梁 (Handrails/Ledgers)"),
    BASES("ジャッキ・敷板 (Jack Bases/Pads)"),
    BRACES("筋交い・ブレス (Diagonal Braces)"),
    WALL_TIES("壁つなぎ・控え (Wall Ties/Anchors)"),
    SAFETY("巾木・メッシュ・朝顔 (Safety/Nettings/Toe Boards)"),
    ACCESS("昇降階段・タラップ (Access Stairs/Ladders)")
}

data class BOMItem(
    val id: String,
    val name: String,
    val spec: String,
    val category: MaterialCategory,
    val unit: String,
    val quantity: Int,
    val unitWeightKg: Double,
    val unitCostYen: Double = 0.0
) {
    val totalWeightKg: Double get() = quantity * unitWeightKg
    val totalCostYen: Double get() = quantity * unitCostYen
}

data class TruckEstimate(
    val totalWeightKg: Double,
    val twoTonTrucks: Double,
    val fourTonTrucks: Double,
    val tenTonTrucks: Double,
    val recommendation: String
)

data class FourSidesInput(
    val siteName: String = "新築住宅仮設足場工事",
    val sideA_Mm: Double = 7200.0, // 正面 (Front)
    val sideB_Mm: Double = 9000.0, // 右側面 (Right)
    val sideC_Mm: Double = 7200.0, // 背面 (Back)
    val sideD_Mm: Double = 9000.0, // 左側面 (Left)
    val heightMm: Double = 6200.0,
    val idealOffsetMm: Double = 250.0,
    val hasStairs: Boolean = true,
    val includeMeshSheet: Boolean = true,
    val includeProtectiveFan: Boolean = false, // 朝顔
    val rentalMonths: Int = 1,
    val unitPricePerM2: Double = 1200.0, // 施工単価 (組立解体)
    val rentalPricePerM2Month: Double = 350.0 // リース単価
)

data class CostQuote(
    val erectionAndDismantleCost: Double,
    val rentalCost: Double,
    val meshSheetCost: Double,
    val transportAndMiscCost: Double,
    val subtotal: Double,
    val consumptionTax: Double,
    val grandTotal: Double
)

data class BuildingCalculationResult(
    val buildingPerimeterM: Double,
    val scaffoldPerimeterM: Double,
    val scaffoldAreaM2: Double,
    val wallAreaM2: Double,
    val meshSheetAreaM2: Double,
    val totalTiers: Int,
    val totalBays: Int,
    val bomList: List<BOMItem>,
    val totalWeightKg: Double,
    val truckEstimate: TruckEstimate,
    val costQuote: CostQuote
)
