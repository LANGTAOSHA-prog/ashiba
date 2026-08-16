package com.example.viewmodel

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.data.database.AppDatabase
import com.example.data.entity.ScaffoldProjectEntity
import com.example.data.repository.ScaffoldRepository
import com.example.engine.ScaffoldEngine
import com.example.model.*
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch

enum class AppNavTab(val label: String) {
    PLAN("平面割付"),
    SECTION("断面・高さ割付"),
    BUILDING("4面・積算見積"),
    TOOLBOX("現場ツール"),
    PROJECTS("保存案件")
}

class ScaffoldViewModel(application: Application) : AndroidViewModel(application) {

    private val repository: ScaffoldRepository

    // Current navigation
    private val _currentTab = MutableStateFlow(AppNavTab.PLAN)
    val currentTab: StateFlow<AppNavTab> = _currentTab.asStateFlow()

    // 1. Plan Layout State
    private val _planInput = MutableStateFlow(PlanInput())
    val planInput: StateFlow<PlanInput> = _planInput.asStateFlow()

    private val _planCandidates = MutableStateFlow<List<PlanCandidate>>(emptyList())
    val planCandidates: StateFlow<List<PlanCandidate>> = _planCandidates.asStateFlow()

    private val _selectedCandidate = MutableStateFlow<PlanCandidate?>(null)
    val selectedCandidate: StateFlow<PlanCandidate?> = _selectedCandidate.asStateFlow()

    // 2. Section Layout State
    private val _sectionInput = MutableStateFlow(SectionInput())
    val sectionInput: StateFlow<SectionInput> = _sectionInput.asStateFlow()

    private val _sectionResult = MutableStateFlow<SectionResult?>(null)
    val sectionResult: StateFlow<SectionResult?> = _sectionResult.asStateFlow()

    // 3. Building BOM & Cost State
    private val _fourSidesInput = MutableStateFlow(FourSidesInput())
    val fourSidesInput: StateFlow<FourSidesInput> = _fourSidesInput.asStateFlow()

    private val _buildingResult = MutableStateFlow<BuildingCalculationResult?>(null)
    val buildingResult: StateFlow<BuildingCalculationResult?> = _buildingResult.asStateFlow()

    // 4. Toolbox State
    private val _roofPitchSun = MutableStateFlow(4.5)
    val roofPitchSun: StateFlow<Double> = _roofPitchSun.asStateFlow()
    private val _roofRunMm = MutableStateFlow(4500.0)
    val roofRunMm: StateFlow<Double> = _roofRunMm.asStateFlow()
    private val _roofResult = MutableStateFlow<RoofSlopeResult?>(null)
    val roofResult: StateFlow<RoofSlopeResult?> = _roofResult.asStateFlow()

    private val _wallTieVertM = MutableStateFlow(3.6)
    val wallTieVertM: StateFlow<Double> = _wallTieVertM.asStateFlow()
    private val _wallTieHorizM = MutableStateFlow(5.4)
    val wallTieHorizM: StateFlow<Double> = _wallTieHorizM.asStateFlow()
    private val _wallTieAreaM2 = MutableStateFlow(180.0)
    val wallTieAreaM2: StateFlow<Double> = _wallTieAreaM2.asStateFlow()
    private val _wallTieResult = MutableStateFlow<WallTieCheckResult?>(null)
    val wallTieResult: StateFlow<WallTieCheckResult?> = _wallTieResult.asStateFlow()

    private val _unitValue = MutableStateFlow(3640.0)
    val unitValue: StateFlow<Double> = _unitValue.asStateFlow()
    private val _unitType = MutableStateFlow("mm")
    val unitType: StateFlow<String> = _unitType.asStateFlow()
    private val _unitResult = MutableStateFlow<UnitConversionResult?>(null)
    val unitResult: StateFlow<UnitConversionResult?> = _unitResult.asStateFlow()

    // 5. Saved Projects
    val savedProjects: StateFlow<List<ScaffoldProjectEntity>>

    // User feedback snackbar/message
    private val _toastMessage = MutableSharedFlow<String>()
    val toastMessage: SharedFlow<String> = _toastMessage.asSharedFlow()

    init {
        val db = AppDatabase.getDatabase(application)
        repository = ScaffoldRepository(db.scaffoldProjectDao())

        savedProjects = repository.allProjects.stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = emptyList()
        )

        // Run initial calculations
        recalculatePlan()
        recalculateSection()
        recalculateBuilding()
        recalculateRoof()
        recalculateWallTie()
        recalculateUnit()
    }

    fun setNavTab(tab: AppNavTab) {
        _currentTab.value = tab
    }

    // --- Plan Layout Actions ---
    fun updatePlanWallWidth(widthMm: Double) {
        _planInput.value = _planInput.value.copy(wallWidthMm = widthMm)
        recalculatePlan()
    }

    fun updatePlanIdealOffset(offsetMm: Double) {
        _planInput.value = _planInput.value.copy(idealOffsetMm = offsetMm)
        recalculatePlan()
    }

    fun updatePlanModule(module: SystemModule) {
        _planInput.value = _planInput.value.copy(systemModule = module)
        recalculatePlan()
    }

    fun updatePlanAllow150(allow: Boolean) {
        _planInput.value = _planInput.value.copy(allow150 = allow)
        recalculatePlan()
    }

    fun selectCandidate(candidate: PlanCandidate) {
        _selectedCandidate.value = candidate
    }

    private fun recalculatePlan() {
        val candidates = ScaffoldEngine.calculatePlanCandidates(_planInput.value)
        _planCandidates.value = candidates
        _selectedCandidate.value = candidates.firstOrNull()
    }

    // --- Section Layout Actions ---
    fun updateSectionTargetHeight(heightMm: Double) {
        _sectionInput.value = _sectionInput.value.copy(targetHeightMm = heightMm)
        recalculateSection()
    }

    fun updateSectionFirstTierHeight(firstMm: Double) {
        _sectionInput.value = _sectionInput.value.copy(firstTierHeightMm = firstMm)
        recalculateSection()
    }

    fun updateSectionScaffoldType(type: ScaffoldType) {
        _sectionInput.value = _sectionInput.value.copy(scaffoldType = type)
        recalculateSection()
    }

    fun updateSectionTopSpec(spec: TopSafetySpec) {
        _sectionInput.value = _sectionInput.value.copy(topSafetySpec = spec)
        recalculateSection()
    }

    fun updateSectionJackBase(jackMm: Double) {
        _sectionInput.value = _sectionInput.value.copy(jackBaseSettingMm = jackMm)
        recalculateSection()
    }

    private fun recalculateSection() {
        _sectionResult.value = ScaffoldEngine.calculateSection(_sectionInput.value)
    }

    // --- Building BOM & Cost Actions ---
    fun updateBuildingSides(sideA: Double, sideB: Double, sideC: Double, sideD: Double, height: Double) {
        _fourSidesInput.value = _fourSidesInput.value.copy(
            sideA_Mm = sideA,
            sideB_Mm = sideB,
            sideC_Mm = sideC,
            sideD_Mm = sideD,
            heightMm = height
        )
        recalculateBuilding()
    }

    fun updateBuildingSiteName(name: String) {
        _fourSidesInput.value = _fourSidesInput.value.copy(siteName = name)
    }

    fun updateBuildingOffset(offsetMm: Double) {
        _fourSidesInput.value = _fourSidesInput.value.copy(idealOffsetMm = offsetMm)
        recalculateBuilding()
    }

    fun updateBuildingOptions(hasStairs: Boolean, meshSheet: Boolean, protectiveFan: Boolean, rentalMonths: Int, unitPrice: Double) {
        _fourSidesInput.value = _fourSidesInput.value.copy(
            hasStairs = hasStairs,
            includeMeshSheet = meshSheet,
            includeProtectiveFan = protectiveFan,
            rentalMonths = rentalMonths,
            unitPricePerM2 = unitPrice
        )
        recalculateBuilding()
    }

    private fun recalculateBuilding() {
        _buildingResult.value = ScaffoldEngine.calculateBuildingBOM(_fourSidesInput.value)
    }

    // --- Toolbox Actions ---
    fun updateRoofSlope(pitchSun: Double, runMm: Double) {
        _roofPitchSun.value = pitchSun
        _roofRunMm.value = runMm
        recalculateRoof()
    }

    private fun recalculateRoof() {
        _roofResult.value = ScaffoldEngine.calculateRoofSlope(_roofPitchSun.value, _roofRunMm.value)
    }

    fun updateWallTie(vertM: Double, horizM: Double, areaM2: Double) {
        _wallTieVertM.value = vertM
        _wallTieHorizM.value = horizM
        _wallTieAreaM2.value = areaM2
        recalculateWallTie()
    }

    private fun recalculateWallTie() {
        _wallTieResult.value = ScaffoldEngine.checkWallTies(_wallTieVertM.value, _wallTieHorizM.value, _wallTieAreaM2.value)
    }

    fun updateUnitConversion(value: Double, unit: String) {
        _unitValue.value = value
        _unitType.value = unit
        recalculateUnit()
    }

    private fun recalculateUnit() {
        _unitResult.value = ScaffoldEngine.convertUnit(_unitValue.value, _unitType.value)
    }

    // --- Database Project Actions ---
    fun saveCurrentProject(title: String, siteAddress: String, clientName: String, notes: String) {
        viewModelScope.launch {
            val bRes = _buildingResult.value
            val pInput = _planInput.value
            val sInput = _sectionInput.value

            val entity = ScaffoldProjectEntity(
                title = if (title.isBlank()) "現場割付 - ${_fourSidesInput.value.siteName}" else title,
                siteAddress = siteAddress,
                clientName = clientName,
                scaffoldType = sInput.scaffoldType.name,
                wallWidthMm = pInput.wallWidthMm,
                heightMm = sInput.targetHeightMm,
                idealOffsetMm = pInput.idealOffsetMm,
                totalAreaM2 = bRes?.scaffoldAreaM2 ?: 0.0,
                totalWeightKg = bRes?.totalWeightKg ?: 0.0,
                grandTotalYen = bRes?.costQuote?.grandTotal ?: 0.0,
                detailsJson = "SideA: ${_fourSidesInput.value.sideA_Mm}, SideB: ${_fourSidesInput.value.sideB_Mm}, Height: ${_fourSidesInput.value.heightMm}",
                notes = notes
            )
            repository.saveProject(entity)
            _toastMessage.emit("案件「${entity.title}」を保存しました")
        }
    }

    fun loadProject(project: ScaffoldProjectEntity) {
        _planInput.value = _planInput.value.copy(wallWidthMm = project.wallWidthMm, idealOffsetMm = project.idealOffsetMm)
        _sectionInput.value = _sectionInput.value.copy(targetHeightMm = project.heightMm)
        _fourSidesInput.value = _fourSidesInput.value.copy(
            siteName = project.title,
            sideA_Mm = project.wallWidthMm,
            heightMm = project.heightMm,
            idealOffsetMm = project.idealOffsetMm
        )
        recalculatePlan()
        recalculateSection()
        recalculateBuilding()
        _currentTab.value = AppNavTab.PLAN
    }

    fun deleteProject(id: Long) {
        viewModelScope.launch {
            repository.deleteProject(id)
            _toastMessage.emit("案件データを削除しました")
        }
    }

    fun generateShareReport(): String {
        val bRes = _buildingResult.value ?: return ""
        val input = _fourSidesInput.value
        val sec = _sectionResult.value

        return buildString {
            appendLine("【足場計算機・現場集計レポート】")
            appendLine("現場名: ${input.siteName}")
            appendLine("建物寸法: 正面 ${input.sideA_Mm}mm / 側面 ${input.sideB_Mm}mm / 高さ ${input.heightMm}mm")
            appendLine("架面積: ${String.format("%.1f", bRes.scaffoldAreaM2)} ㎡ (足場外周: ${String.format("%.1f", bRes.scaffoldPerimeterM)} m)")
            appendLine("段数: ${bRes.totalTiers}層 / 総スパン数: ${bRes.totalBays}スパン")
            if (sec != null) {
                appendLine("柱割付: 外柱[${sec.outerPostSummary}] / 内柱[${sec.innerPostSummary}]")
                appendLine("ジャッキレベル: ${sec.jackLevelMm}mm / スタートコマ: ${sec.startCommaLevel}コマ")
            }
            appendLine("総重量: ${String.format("%.0f", bRes.totalWeightKg)} kg")
            appendLine("運搬目安: ${bRes.truckEstimate.recommendation}")
            appendLine("概算見積合計: ¥${String.format("%,d", bRes.costQuote.grandTotal.toInt())} (税込)")
            appendLine("-------------------------")
            appendLine("主要部材:")
            bRes.bomList.take(6).forEach {
                appendLine("・${it.name}: ${it.quantity} ${it.unit} (${it.spec})")
            }
        }
    }
}
