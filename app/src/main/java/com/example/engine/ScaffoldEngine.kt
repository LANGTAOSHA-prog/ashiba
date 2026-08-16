package com.example.engine

import com.example.model.*
import kotlin.math.*

object ScaffoldEngine {

    /**
     * Calculates candidate plan layout allocations based on wall width and ideal clearance.
     */
    fun calculatePlanCandidates(input: PlanInput): List<PlanCandidate> {
        val targetScaffoldWidth = input.wallWidthMm + (input.idealOffsetMm * 2.0)
        val availableSpans = input.systemModule.standardSpans.filter { if (!input.allow150) it >= 300 else true }

        val candidates = mutableListOf<PlanCandidate>()

        // 1. Greedy standard allocation with 1800mm max spans
        val standardSpans = buildGreedySpans(targetScaffoldWidth.toInt(), availableSpans)
        val standardWidth = standardSpans.sum()
        val standardOffset = (standardWidth - input.wallWidthMm) / 2.0
        candidates.add(
            PlanCandidate(
                id = "cand_standard",
                title = "標準割付 (Standard 1800最優先)",
                description = "1800mmスパンを最大限採用し、端部のみ調整材を使用する施工効率重視の配置",
                spans = standardSpans,
                totalScaffoldWidthMm = standardWidth,
                actualOffsetMm = standardOffset,
                offsetDiffMm = standardOffset - input.idealOffsetMm,
                postCount = (standardSpans.size + 1) * 2,
                plankCountPerTier = standardSpans.size,
                handrailCountPerTier = standardSpans.size * 2,
                toeBoardCountPerTier = standardSpans.size,
                isOptimal = true
            )
        )

        // 2. Symmetrical layout (balanced ends)
        val symSpans = buildSymmetricalSpans(targetScaffoldWidth.toInt(), availableSpans)
        val symWidth = symSpans.sum()
        val symOffset = (symWidth - input.wallWidthMm) / 2.0
        candidates.add(
            PlanCandidate(
                id = "cand_symmetrical",
                title = "左右対称割付 (Balanced Symmetrical)",
                description = "両端のスパン長を均等に揃え、建物の美観と荷重バランスを保つ配置",
                spans = symSpans,
                totalScaffoldWidthMm = symWidth,
                actualOffsetMm = symOffset,
                offsetDiffMm = symOffset - input.idealOffsetMm,
                postCount = (symSpans.size + 1) * 2,
                plankCountPerTier = symSpans.size,
                handrailCountPerTier = symSpans.size * 2,
                toeBoardCountPerTier = symSpans.size,
                isOptimal = false
            )
        )

        // 3. Compact / tight layout (minimizing clearance)
        val tightTarget = input.wallWidthMm + (150.0 * 2.0)
        val tightSpans = buildGreedySpans(tightTarget.toInt(), availableSpans)
        val tightWidth = tightSpans.sum()
        val tightOffset = (tightWidth - input.wallWidthMm) / 2.0
        if (tightWidth != standardWidth) {
            candidates.add(
                PlanCandidate(
                    id = "cand_tight",
                    title = "狭小地割付 (Tight Clearance)",
                    description = "隣地境界が狭い現場向けに離れを最小限に抑えたコンパクト配置",
                    spans = tightSpans,
                    totalScaffoldWidthMm = tightWidth,
                    actualOffsetMm = tightOffset,
                    offsetDiffMm = tightOffset - input.idealOffsetMm,
                    postCount = (tightSpans.size + 1) * 2,
                    plankCountPerTier = tightSpans.size,
                    handrailCountPerTier = tightSpans.size * 2,
                    toeBoardCountPerTier = tightSpans.size,
                    isOptimal = false
                )
            )
        }

        // 4. Extended safety layout
        val wideTarget = input.wallWidthMm + ((input.idealOffsetMm + 100.0) * 2.0)
        val wideSpans = buildGreedySpans(wideTarget.toInt(), availableSpans)
        val wideWidth = wideSpans.sum()
        val wideOffset = (wideWidth - input.wallWidthMm) / 2.0
        if (wideWidth != standardWidth && wideWidth != tightWidth) {
            candidates.add(
                PlanCandidate(
                    id = "cand_wide",
                    title = "余裕安全割付 (Extra Clearance)",
                    description = "軒出・雨樋や外壁凸凹部を避けて十分な作業クリアランスを確保する配置",
                    spans = wideSpans,
                    totalScaffoldWidthMm = wideWidth,
                    actualOffsetMm = wideOffset,
                    offsetDiffMm = wideOffset - input.idealOffsetMm,
                    postCount = (wideSpans.size + 1) * 2,
                    plankCountPerTier = wideSpans.size,
                    handrailCountPerTier = wideSpans.size * 2,
                    toeBoardCountPerTier = wideSpans.size,
                    isOptimal = false
                )
            )
        }

        return candidates
    }

    private fun buildGreedySpans(target: Int, spans: List<Int>): List<Int> {
        val result = mutableListOf<Int>()
        var remaining = target
        val sorted = spans.sortedDescending()

        while (remaining > 0) {
            val best = sorted.firstOrNull { it <= remaining + (sorted.last() / 2) } ?: sorted.last()
            result.add(best)
            remaining -= best
            if (remaining <= -(sorted.last() / 2)) break
        }

        return if (result.isEmpty()) listOf(1800) else result
    }

    private fun buildSymmetricalSpans(target: Int, spans: List<Int>): List<Int> {
        val maxSpan = spans.firstOrNull { it >= 1800 } ?: (spans.maxOrNull() ?: 1800)
        val numCenter1800 = max(0, (target - (2 * 900)) / maxSpan)
        val remainingEnds = max(0, target - (numCenter1800 * maxSpan))
        val halfEnd = remainingEnds / 2

        // find closest span for ends
        val endSpan = spans.minByOrNull { abs(it - halfEnd) } ?: 900
        val result = mutableListOf<Int>()
        result.add(endSpan)
        for (i in 0 until numCenter1800) {
            result.add(maxSpan)
        }
        result.add(endSpan)
        return result
    }

    /**
     * Calculates elevation section breakdown, post combinations, jack level, and tier schedule.
     */
    fun calculateSection(input: SectionInput): SectionResult {
        val jackBase = input.jackBaseSettingMm.toInt()
        val standardTier = input.standardTierHeightMm.toInt().coerceAtLeast(1200)
        val firstTier = input.firstTierHeightMm.toInt().coerceAtLeast(1200)
        val pitch = input.scaffoldType.commaPitchMm

        // Total working tiers to reach or exceed target height
        val remainingHeight = max(0.0, input.targetHeightMm - (jackBase + firstTier))
        val additionalTiers = ceil(remainingHeight / standardTier).toInt()
        val totalTiers = max(1, 1 + additionalTiers)

        val tiers = mutableListOf<TierInfo>()
        var currentDeckElevation = jackBase + firstTier

        for (i in 1..totalTiers) {
            val deckElev = if (i == 1) jackBase + firstTier else jackBase + firstTier + ((i - 1) * standardTier)
            val handrailElev = deckElev + 900
            val toeBoardElev = deckElev + 150
            tiers.add(
                TierInfo(
                    tierNumber = i,
                    plankElevationMm = deckElev,
                    handrailElevationMm = handrailElev,
                    toeBoardElevationMm = toeBoardElev,
                    label = "第${i}層 (アンチ高: ${deckElev}mm / 手すり高: ${handrailElev}mm)"
                )
            )
            currentDeckElevation = deckElev
        }

        val totalScaffoldHeight = currentDeckElevation + input.topSafetySpec.extraTopHeightMm

        // Post length schedule: Kusabi standard posts are typically 3600mm, 2700mm, 1800mm, 900mm
        // Outer posts and Inner posts staggered (千鳥継ぎ)
        val outerPosts = resolveStaggeredPosts(totalScaffoldHeight, isOuter = true)
        val innerPosts = resolveStaggeredPosts(totalScaffoldHeight, isOuter = false)

        val startCommaLevel = (firstTier / pitch).coerceAtLeast(1)
        val startCommaDesc = "${startCommaLevel}コマ目 (${startCommaLevel * pitch}mm位置に根がらみ・第1層)"

        val outerSummary = outerPosts.joinToString(" + ") { "${it}mm支柱" }
        val innerSummary = innerPosts.joinToString(" + ") { "${it}mm支柱" }

        val topMargin = totalScaffoldHeight - input.targetHeightMm.toInt()

        return SectionResult(
            targetHeightMm = input.targetHeightMm,
            totalScaffoldHeightMm = totalScaffoldHeight,
            totalTiers = totalTiers,
            jackLevelMm = jackBase,
            startCommaLevel = startCommaLevel,
            startCommaDescription = startCommaDesc,
            outerPosts = outerPosts,
            innerPosts = innerPosts,
            tiers = tiers,
            outerPostSummary = outerSummary,
            innerPostSummary = innerSummary,
            topEaveMarginMm = topMargin
        )
    }

    private fun resolveStaggeredPosts(totalHeight: Int, isOuter: Boolean): List<Int> {
        val result = mutableListOf<Int>()
        var remaining = totalHeight

        if (isOuter) {
            // Outer posts start with 3600mm base post
            if (remaining >= 3600) {
                result.add(3600)
                remaining -= 3600
            } else if (remaining >= 2700) {
                result.add(2700)
                remaining -= 2700
            } else {
                result.add(1800)
                remaining -= 1800
            }
        } else {
            // Inner posts start with 1800mm base post to create staggered joint
            if (remaining >= 1800) {
                result.add(1800)
                remaining -= 1800
            }
        }

        while (remaining > 0) {
            when {
                remaining >= 3600 -> {
                    result.add(3600)
                    remaining -= 3600
                }
                remaining >= 2700 -> {
                    result.add(2700)
                    remaining -= 2700
                }
                remaining >= 1800 -> {
                    result.add(1800)
                    remaining -= 1800
                }
                else -> {
                    result.add(900)
                    remaining -= 900
                }
            }
        }

        return if (result.isEmpty()) listOf(3600) else result
    }

    /**
     * Calculates 4-side whole building scaffolding area, BOM (Bill of Materials), truck loads, and estimated quote.
     */
    fun calculateBuildingBOM(input: FourSidesInput): BuildingCalculationResult {
        val perimeterBuildingM = (input.sideA_Mm + input.sideB_Mm + input.sideC_Mm + input.sideD_Mm) / 1000.0
        val offsetM = input.idealOffsetMm / 1000.0
        val scaffoldPerimeterM = perimeterBuildingM + (8 * offsetM)
        val heightM = input.heightMm / 1000.0
        val totalScaffoldHeightM = heightM + 0.9 // Top handrail elevation

        val scaffoldAreaM2 = scaffoldPerimeterM * totalScaffoldHeightM
        val wallAreaM2 = perimeterBuildingM * heightM
        val meshAreaM2 = if (input.includeMeshSheet) scaffoldPerimeterM * totalScaffoldHeightM else 0.0

        val totalTiers = max(1, ceil(heightM / 1.8).toInt())
        val baysA = max(1, ceil(input.sideA_Mm / 1800.0).toInt())
        val baysB = max(1, ceil(input.sideB_Mm / 1800.0).toInt())
        val baysC = max(1, ceil(input.sideC_Mm / 1800.0).toInt())
        val baysD = max(1, ceil(input.sideD_Mm / 1800.0).toInt())
        val totalBays = baysA + baysB + baysC + baysD

        val totalColumns = totalBays * 2 // Outer and inner columns
        val postStacksPerColumn = max(1, ceil(totalScaffoldHeightM / 3.6).toInt())
        val total3600Posts = (totalColumns * postStacksPerColumn * 0.7).toInt()
        val total1800Posts = (totalColumns * postStacksPerColumn * 0.3).toInt()

        val bomList = mutableListOf<BOMItem>()

        // 1. Posts (支柱)
        bomList.add(
            BOMItem(
                id = "post_3600",
                name = "支柱 3600",
                spec = "クサビ式・48.6φ・コマ数8",
                category = MaterialCategory.POSTS,
                unit = "本",
                quantity = max(4, total3600Posts),
                unitWeightKg = 12.8,
                unitCostYen = 280.0
            )
        )
        bomList.add(
            BOMItem(
                id = "post_1800",
                name = "支柱 1800",
                spec = "クサビ式・48.6φ・コマ数4",
                category = MaterialCategory.POSTS,
                unit = "本",
                quantity = max(4, total1800Posts),
                unitWeightKg = 6.8,
                unitCostYen = 160.0
            )
        )

        // 2. Base Jacks (ジャッキベース)
        bomList.add(
            BOMItem(
                id = "jack_base",
                name = "ジャッキベース",
                spec = "調整範囲 50〜350mm",
                category = MaterialCategory.BASES,
                unit = "本",
                quantity = totalColumns,
                unitWeightKg = 3.3,
                unitCostYen = 120.0
            )
        )
        bomList.add(
            BOMItem(
                id = "base_pad",
                name = "敷板・アンダーベース",
                spec = "樹脂製・木製120角",
                category = MaterialCategory.BASES,
                unit = "枚",
                quantity = totalColumns,
                unitWeightKg = 1.2,
                unitCostYen = 50.0
            )
        )

        // 3. Planks (踏板 / アンチ)
        bomList.add(
            BOMItem(
                id = "plank_1800",
                name = "踏板 (アンチ) 1800×400",
                spec = "鋼製エキスパンド・許容積載200kg",
                category = MaterialCategory.PLANKS,
                unit = "枚",
                quantity = totalBays * totalTiers,
                unitWeightKg = 12.0,
                unitCostYen = 320.0
            )
        )

        // 4. Handrails (手すり / 梁間手すり)
        bomList.add(
            BOMItem(
                id = "handrail_1800",
                name = "手すり 1800",
                spec = "クサビ式先行手すり兼中桟",
                category = MaterialCategory.HANDRAILS,
                unit = "本",
                quantity = totalBays * (totalTiers + 1) * 2,
                unitWeightKg = 3.5,
                unitCostYen = 90.0
            )
        )
        bomList.add(
            BOMItem(
                id = "beam_handrail_600",
                name = "妻手すり・梁間 600",
                spec = "4隅・開口部用",
                category = MaterialCategory.HANDRAILS,
                unit = "本",
                quantity = 4 * totalTiers * 2,
                unitWeightKg = 1.9,
                unitCostYen = 75.0
            )
        )

        // 5. Diagonal Braces (筋交い / ブレス)
        bomList.add(
            BOMItem(
                id = "brace_1800",
                name = "筋交い (ブレース) 1800×1800",
                spec = "防振・剛性補強",
                category = MaterialCategory.BRACES,
                unit = "本",
                quantity = max(4, (totalBays * totalTiers * 0.4).toInt()),
                unitWeightKg = 4.2,
                unitCostYen = 110.0
            )
        )

        // 6. Wall ties (壁つなぎ)
        val tieCount = max(4, ceil(scaffoldAreaM2 / 20.0).toInt())
        bomList.add(
            BOMItem(
                id = "wall_tie",
                name = "壁つなぎ金具・アンカー",
                spec = "控え金具2本組＋専用金具",
                category = MaterialCategory.WALL_TIES,
                unit = "組",
                quantity = tieCount,
                unitWeightKg = 2.4,
                unitCostYen = 150.0
            )
        )

        // 7. Safety (巾木 / メッシュシート / 朝顔)
        bomList.add(
            BOMItem(
                id = "toeboard_1800",
                name = "巾木 (幅木) 1800×150",
                spec = "落下物防止アルミ製/鋼製",
                category = MaterialCategory.SAFETY,
                unit = "枚",
                quantity = totalBays * totalTiers,
                unitWeightKg = 2.1,
                unitCostYen = 80.0
            )
        )

        if (input.includeMeshSheet) {
            val sheetCount = max(4, ceil(meshAreaM2 / (1.8 * 5.4)).toInt())
            bomList.add(
                BOMItem(
                    id = "mesh_sheet",
                    name = "防炎メッシュシート 1.8×5.4m",
                    spec = "1類防炎・黒/ブルー/グレー",
                    category = MaterialCategory.SAFETY,
                    unit = "枚",
                    quantity = sheetCount,
                    unitWeightKg = 3.8,
                    unitCostYen = 220.0
                )
            )
        }

        if (input.includeProtectiveFan) {
            bomList.add(
                BOMItem(
                    id = "protective_fan",
                    name = "朝顔 (落下物防護棚)",
                    spec = "アルミ製ユニット式",
                    category = MaterialCategory.SAFETY,
                    unit = "台",
                    quantity = max(2, baysA),
                    unitWeightKg = 24.0,
                    unitCostYen = 1500.0
                )
            )
        }

        // 8. Access (昇降階段)
        if (input.hasStairs) {
            bomList.add(
                BOMItem(
                    id = "stair_unit",
                    name = "階段枠・ステップユニット",
                    spec = "段高1800mm専用アルミステップ",
                    category = MaterialCategory.ACCESS,
                    unit = "基",
                    quantity = totalTiers,
                    unitWeightKg = 18.5,
                    unitCostYen = 800.0
                )
            )
            bomList.add(
                BOMItem(
                    id = "stair_handrail",
                    name = "階段用斜め手すり",
                    spec = "昇降安全ガイド",
                    category = MaterialCategory.ACCESS,
                    unit = "本",
                    quantity = totalTiers * 2,
                    unitWeightKg = 3.2,
                    unitCostYen = 120.0
                )
            )
        }

        val totalWeight = bomList.sumOf { it.totalWeightKg }

        // Truck estimation
        val twoTon = totalWeight / 2000.0
        val fourTon = totalWeight / 4000.0
        val tenTon = totalWeight / 10000.0

        val truckRec = when {
            totalWeight <= 2000.0 -> "2t車 × 1台 で運搬可能 (総重量: ${String.format("%.0f", totalWeight)}kg)"
            totalWeight <= 4000.0 -> "4t車 × 1台 (または2t車 × 2台) で運搬可能"
            totalWeight <= 8000.0 -> "4t車 × 2台 (または10t車 × 1台) 必要"
            else -> "10t車 × ${ceil(tenTon).toInt()}台 規模の大型運搬が必要"
        }

        val truckEstimate = TruckEstimate(
            totalWeightKg = totalWeight,
            twoTonTrucks = twoTon,
            fourTonTrucks = fourTon,
            tenTonTrucks = tenTon,
            recommendation = truckRec
        )

        // Cost estimation
        val erectDismantle = scaffoldAreaM2 * input.unitPricePerM2
        val rentalCost = scaffoldAreaM2 * input.rentalPricePerM2Month * input.rentalMonths
        val sheetCost = if (input.includeMeshSheet) meshAreaM2 * 80.0 * input.rentalMonths else 0.0
        val transportCost = 35000.0 + (ceil(fourTon) * 15000.0)
        val subtotal = erectDismantle + rentalCost + sheetCost + transportCost
        val tax = subtotal * 0.10
        val grandTotal = subtotal + tax

        val costQuote = CostQuote(
            erectionAndDismantleCost = erectDismantle,
            rentalCost = rentalCost,
            meshSheetCost = sheetCost,
            transportAndMiscCost = transportCost,
            subtotal = subtotal,
            consumptionTax = tax,
            grandTotal = grandTotal
        )

        return BuildingCalculationResult(
            buildingPerimeterM = perimeterBuildingM,
            scaffoldPerimeterM = scaffoldPerimeterM,
            scaffoldAreaM2 = scaffoldAreaM2,
            wallAreaM2 = wallAreaM2,
            meshSheetAreaM2 = meshAreaM2,
            totalTiers = totalTiers,
            totalBays = totalBays,
            bomList = bomList,
            totalWeightKg = totalWeight,
            truckEstimate = truckEstimate,
            costQuote = costQuote
        )
    }

    /**
     * Roof slope calculator (屋根勾配・屋根足場要否判定)
     */
    fun calculateRoofSlope(pitchSun: Double, runMm: Double): RoofSlopeResult {
        // 1寸 = 1/10 gradient. pitchSun / 10 = tan(theta)
        val tanTheta = pitchSun / 10.0
        val angleRad = atan(tanTheta)
        val angleDeg = Math.toDegrees(angleRad)
        val riseMm = runMm * tanTheta
        val rafterMm = sqrt(runMm.pow(2) + riseMm.pow(2))

        // In Japanese construction safety regulations: slopes 6寸 (approx 31°) or steeper require roof safety scaffolding (屋根足場)
        val requiresScaffold = pitchSun >= 6.0
        val notice = if (requiresScaffold) {
            "【要 屋根足場】勾配が6寸（約31°）以上のため、労働安全衛生規則に基づき屋根足場（親綱・屋根ステップ）の設置が必須です。"
        } else {
            "【屋根足場 不要/任意】勾配が6寸未満のため標準足場＋親綱で施工可能です（急勾配時の滑り止め配慮を推奨）。"
        }

        return RoofSlopeResult(
            slopeSump = pitchSun,
            angleDegree = angleDeg,
            runLengthMm = runMm,
            riseHeightMm = riseMm,
            rafterLengthMm = rafterMm,
            requiresRoofScaffold = requiresScaffold,
            safetyNotice = notice
        )
    }

    /**
     * Wall ties spacing safety checker (壁つなぎ安全チェック)
     */
    fun checkWallTies(verticalSpanM: Double, horizontalSpanM: Double, totalAreaM2: Double): WallTieCheckResult {
        val areaPerTie = verticalSpanM * horizontalSpanM
        // For Kusabi scaffolding, standard rule: vertical <= 5.0m, horizontal <= 5.5m, area per tie <= 25 m2 (frame is <= 45m2)
        val maxAllowedArea = 25.0
        val isCompliant = verticalSpanM <= 5.0 && horizontalSpanM <= 5.5 && areaPerTie <= maxAllowedArea

        val requiredTies = ceil(totalAreaM2 / min(areaPerTie, maxAllowedArea)).toInt().coerceAtLeast(1)

        val rec = if (isCompliant) {
            "安全基準適合: 1箇所あたり負担面積 ${String.format("%.1f", areaPerTie)}㎡ (基準25㎡以下)、垂直間隔 ${verticalSpanM}m(≦5.0m)、水平間隔 ${horizontalSpanM}m(≦5.5m) を満たしています。"
        } else {
            "安全基準超過: 壁つなぎ間隔が規定値（垂直5.0m以内・水平5.5m以内・負担面積25㎡以内）を超えています。壁つなぎ本数を増やしてください。"
        }

        return WallTieCheckResult(
            verticalSpanM = verticalSpanM,
            horizontalSpanM = horizontalSpanM,
            calculatedAreaPerTieM2 = areaPerTie,
            maxAllowedAreaM2 = maxAllowedArea,
            isCompliant = isCompliant,
            recommendation = rec,
            totalRequiredTies = requiredTies
        )
    }

    /**
     * Architectural unit converter (Meters, Shaku, Ken, Sun, Tsubo, m2)
     */
    fun convertUnit(value: Double, unitType: String): UnitConversionResult {
        val mm = when (unitType) {
            "mm" -> value
            "m" -> value * 1000.0
            "shaku" -> value * (10000.0 / 33.0) // 1尺 = 303.0303 mm
            "ken" -> value * 6.0 * (10000.0 / 33.0) // 1間 = 6尺 = 1818.1818 mm
            "sun" -> value * (1000.0 / 33.0) // 1寸 = 30.3030 mm
            else -> value
        }

        val m = mm / 1000.0
        val shaku = mm / (10000.0 / 33.0)
        val ken = shaku / 6.0
        val sun = shaku * 10.0
        val sqM = m * m
        val tsubo = sqM / 3.30578

        return UnitConversionResult(
            millimeters = mm,
            meters = m,
            shaku = shaku,
            ken = ken,
            sun = sun,
            tsubo = tsubo,
            squareMeters = sqM
        )
    }
}
