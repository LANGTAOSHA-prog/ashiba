package com.example.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.example.ui.components.MetricStatCard
import com.example.ui.components.StepperNumberField
import com.example.viewmodel.ScaffoldViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ToolboxScreen(
    viewModel: ScaffoldViewModel,
    modifier: Modifier = Modifier
) {
    val roofPitch by viewModel.roofPitchSun.collectAsState()
    val roofRun by viewModel.roofRunMm.collectAsState()
    val roofResult by viewModel.roofResult.collectAsState()

    val wallVert by viewModel.wallTieVertM.collectAsState()
    val wallHoriz by viewModel.wallTieHorizM.collectAsState()
    val wallArea by viewModel.wallTieAreaM2.collectAsState()
    val wallResult by viewModel.wallTieResult.collectAsState()

    val unitVal by viewModel.unitValue.collectAsState()
    val unitType by viewModel.unitType.collectAsState()
    val unitResult by viewModel.unitResult.collectAsState()

    var selectedToolIndex by remember { mutableStateOf(0) }
    val toolTabs = listOf("屋根勾配足場", "壁つなぎ点検", "建築単位換算")

    LazyColumn(
        modifier = modifier
            .fillMaxSize()
            .padding(horizontal = 16.dp),
        verticalArrangement = Arrangement.spacedBy(14.dp),
        contentPadding = PaddingValues(top = 12.dp, bottom = 24.dp)
    ) {
        item {
            Column {
                Text(
                    text = "現場実務ツール (Site Toolbox)",
                    style = MaterialTheme.typography.headlineSmall,
                    fontWeight = FontWeight.Bold
                )
                Text(
                    text = "安全基準点検・勾配足場要否判定・寸法単位相互換算",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }

        // Tool selector tabs
        item {
            PrimaryTabRow(
                selectedTabIndex = selectedToolIndex,
                modifier = Modifier.fillMaxWidth()
            ) {
                toolTabs.forEachIndexed { index, title ->
                    Tab(
                        selected = selectedToolIndex == index,
                        onClick = { selectedToolIndex = index },
                        text = { Text(title, fontWeight = FontWeight.Bold) }
                    )
                }
            }
        }

        when (selectedToolIndex) {
            0 -> {
                // Tool 1: Roof Pitch
                item {
                    Text(
                        text = "屋根勾配 & 屋根足場要否計算",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold
                    )
                }

                item {
                    StepperNumberField(
                        label = "屋根寸勾配 (Roof Pitch Sump)",
                        value = roofPitch,
                        onValueChange = { viewModel.updateRoofSlope(it, roofRun) },
                        step = 0.5,
                        min = 1.0,
                        max = 15.0,
                        unit = "寸",
                        quickSteps = listOf(0.5, 1.0, 2.0),
                        modifier = Modifier.testTag("roof_pitch_input")
                    )
                }

                item {
                    StepperNumberField(
                        label = "屋根水平長さ (Run Length)",
                        value = roofRun,
                        onValueChange = { viewModel.updateRoofSlope(roofPitch, it) },
                        step = 500.0,
                        min = 500.0,
                        max = 20000.0,
                        unit = "mm",
                        quickSteps = listOf(500.0, 910.0, 1820.0),
                        modifier = Modifier.testTag("roof_run_input")
                    )
                }

                if (roofResult != null) {
                    val r = roofResult!!
                    item {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(10.dp)
                        ) {
                            MetricStatCard(
                                title = "勾配角度 (Angle)",
                                value = String.format("%.1f", r.angleDegree),
                                unit = "°",
                                subtitle = "${r.slopeSump}寸勾配",
                                icon = Icons.Default.ChangeHistory,
                                modifier = Modifier.weight(1f)
                            )
                            MetricStatCard(
                                title = "屋根斜長 (Rafter)",
                                value = "${r.rafterLengthMm.toInt()}",
                                unit = "mm",
                                subtitle = "立上り棟高: ${r.riseHeightMm.toInt()}mm",
                                icon = Icons.Default.Straighten,
                                modifier = Modifier.weight(1f)
                            )
                        }
                    }

                    item {
                        Card(
                            colors = CardDefaults.cardColors(
                                containerColor = if (r.requiresRoofScaffold) MaterialTheme.colorScheme.errorContainer
                                else MaterialTheme.colorScheme.primaryContainer
                            ),
                            shape = RoundedCornerShape(14.dp),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Column(modifier = Modifier.padding(14.dp)) {
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Icon(
                                        imageVector = if (r.requiresRoofScaffold) Icons.Default.Warning else Icons.Default.CheckCircle,
                                        contentDescription = null,
                                        tint = if (r.requiresRoofScaffold) MaterialTheme.colorScheme.error else MaterialTheme.colorScheme.primary
                                    )
                                    Spacer(modifier = Modifier.width(8.dp))
                                    Text(
                                        text = if (r.requiresRoofScaffold) "屋根足場の設置が法令上必須です (6寸以上)" else "屋根足場は不要です (6寸未満)",
                                        style = MaterialTheme.typography.titleSmall,
                                        fontWeight = FontWeight.Bold
                                    )
                                }
                                Spacer(modifier = Modifier.height(6.dp))
                                Text(
                                    text = r.safetyNotice,
                                    style = MaterialTheme.typography.bodySmall
                                )
                            }
                        }
                    }
                }
            }

            1 -> {
                // Tool 2: Wall Tie Check
                item {
                    Text(
                        text = "壁つなぎ配置 & 負担面積安全チェック",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold
                    )
                }

                item {
                    StepperNumberField(
                        label = "垂直設置間隔 (Vertical Span)",
                        value = wallVert,
                        onValueChange = { viewModel.updateWallTie(it, wallHoriz, wallArea) },
                        step = 0.5,
                        min = 1.0,
                        max = 10.0,
                        unit = "m",
                        quickSteps = listOf(0.5, 1.0),
                        modifier = Modifier.testTag("wall_vert_input")
                    )
                }

                item {
                    StepperNumberField(
                        label = "水平設置間隔 (Horizontal Span)",
                        value = wallHoriz,
                        onValueChange = { viewModel.updateWallTie(wallVert, it, wallArea) },
                        step = 0.5,
                        min = 1.0,
                        max = 10.0,
                        unit = "m",
                        quickSteps = listOf(0.5, 1.0),
                        modifier = Modifier.testTag("wall_horiz_input")
                    )
                }

                item {
                    StepperNumberField(
                        label = "対象足場架面積 (Total Scaffold Area)",
                        value = wallArea,
                        onValueChange = { viewModel.updateWallTie(wallVert, wallHoriz, it) },
                        step = 20.0,
                        min = 10.0,
                        max = 5000.0,
                        unit = "㎡",
                        quickSteps = listOf(20.0, 50.0, 100.0),
                        modifier = Modifier.testTag("wall_area_input")
                    )
                }

                if (wallResult != null) {
                    val w = wallResult!!
                    item {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(10.dp)
                        ) {
                            MetricStatCard(
                                title = "1箇所あたり負担面積",
                                value = String.format("%.1f", w.calculatedAreaPerTieM2),
                                unit = "㎡",
                                subtitle = "基準限度: 25.0 ㎡以内",
                                icon = Icons.Default.Dashboard,
                                badgeColor = if (w.isCompliant) MaterialTheme.colorScheme.primaryContainer else MaterialTheme.colorScheme.errorContainer,
                                modifier = Modifier.weight(1f)
                            )
                            MetricStatCard(
                                title = "必要壁つなぎ本数",
                                value = "${w.totalRequiredTies}",
                                unit = "組",
                                subtitle = "全体配置計画数",
                                icon = Icons.Default.Pin,
                                modifier = Modifier.weight(1f)
                            )
                        }
                    }

                    item {
                        Card(
                            colors = CardDefaults.cardColors(
                                containerColor = if (w.isCompliant) MaterialTheme.colorScheme.surfaceVariant
                                else MaterialTheme.colorScheme.errorContainer
                            ),
                            shape = RoundedCornerShape(14.dp),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Column(modifier = Modifier.padding(14.dp)) {
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Icon(
                                        imageVector = if (w.isCompliant) Icons.Default.CheckCircle else Icons.Default.Error,
                                        contentDescription = null,
                                        tint = if (w.isCompliant) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.error
                                    )
                                    Spacer(modifier = Modifier.width(8.dp))
                                    Text(
                                        text = if (w.isCompliant) "安全基準適合 (Compliant)" else "安全基準超過注意 (Non-Compliant)",
                                        style = MaterialTheme.typography.titleSmall,
                                        fontWeight = FontWeight.Bold
                                    )
                                }
                                Spacer(modifier = Modifier.height(6.dp))
                                Text(
                                    text = w.recommendation,
                                    style = MaterialTheme.typography.bodySmall
                                )
                            }
                        }
                    }
                }
            }

            2 -> {
                // Tool 3: Unit Converter
                item {
                    Text(
                        text = "建築寸法・面積単位換算 (Unit Converter)",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold
                    )
                }

                item {
                    Card(
                        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                        shape = RoundedCornerShape(16.dp),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Column(modifier = Modifier.padding(14.dp)) {
                            Text(
                                text = "換算元単位を選択",
                                style = MaterialTheme.typography.titleSmall,
                                fontWeight = FontWeight.SemiBold
                            )
                            Spacer(modifier = Modifier.height(8.dp))
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.spacedBy(6.dp)
                            ) {
                                listOf("mm" to "mm", "m" to "メートル", "shaku" to "尺 (Shaku)", "ken" to "間 (Ken)", "sun" to "寸 (Sun)").forEach { (k, label) ->
                                    FilterChip(
                                        selected = unitType == k,
                                        onClick = { viewModel.updateUnitConversion(unitVal, k) },
                                        label = { Text(label, style = MaterialTheme.typography.labelSmall) }
                                    )
                                }
                            }
                        }
                    }
                }

                item {
                    StepperNumberField(
                        label = "換算入力値",
                        value = unitVal,
                        onValueChange = { viewModel.updateUnitConversion(it, unitType) },
                        step = 100.0,
                        min = 0.1,
                        max = 100000.0,
                        unit = unitType,
                        quickSteps = listOf(100.0, 910.0, 1820.0),
                        modifier = Modifier.testTag("unit_val_input")
                    )
                }

                if (unitResult != null) {
                    val u = unitResult!!
                    item {
                        Card(
                            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                            shape = RoundedCornerShape(14.dp),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Column(modifier = Modifier.padding(14.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                                Text(
                                    text = "換算結果 (Calculated Equivalents)",
                                    style = MaterialTheme.typography.titleSmall,
                                    fontWeight = FontWeight.Bold,
                                    color = MaterialTheme.colorScheme.primary
                                )
                                Divider()
                                UnitRow(label = "ミリメートル (mm)", value = String.format("%,.1f mm", u.millimeters))
                                UnitRow(label = "メートル (m)", value = String.format("%.3f m", u.meters))
                                UnitRow(label = "尺 (1尺=約303mm)", value = String.format("%.2f 尺", u.shaku))
                                UnitRow(label = "間 (1間=6尺=約1.82m)", value = String.format("%.2f 間", u.ken))
                                UnitRow(label = "寸 (1寸=約30.3mm)", value = String.format("%.1f 寸", u.sun))
                                UnitRow(label = "平方メートル (㎡)", value = String.format("%.2f ㎡", u.squareMeters))
                                UnitRow(label = "坪数 (1坪=約3.3㎡)", value = String.format("%.2f 坪", u.tsubo))
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun UnitRow(label: String, value: String) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Text(text = label, style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.onSurfaceVariant)
        Text(text = value, style = MaterialTheme.typography.bodyMedium, fontWeight = FontWeight.Bold)
    }
}
