package com.example.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.SquareFoot
import androidx.compose.material.icons.filled.Tune
import androidx.compose.material.icons.filled.WidthFull
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.example.model.PlanCandidate
import com.example.model.SystemModule
import com.example.ui.components.MetricStatCard
import com.example.ui.components.PlanVisualCanvas
import com.example.ui.components.StepperNumberField
import com.example.viewmodel.ScaffoldViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PlanLayoutScreen(
    viewModel: ScaffoldViewModel,
    modifier: Modifier = Modifier
) {
    val planInput by viewModel.planInput.collectAsState()
    val candidates by viewModel.planCandidates.collectAsState()
    val selectedCandidate by viewModel.selectedCandidate.collectAsState()

    LazyColumn(
        modifier = modifier
            .fillMaxSize()
            .padding(horizontal = 16.dp),
        verticalArrangement = Arrangement.spacedBy(14.dp),
        contentPadding = PaddingValues(top = 12.dp, bottom = 24.dp)
    ) {
        // Section Header
        item {
            Column {
                Text(
                    text = "平面割付 (Plan Layout)",
                    style = MaterialTheme.typography.headlineSmall,
                    fontWeight = FontWeight.Bold
                )
                Text(
                    text = "施工幅と離れを入力し、最適な手すり・アンチ割付パターンを自動算出",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }

        // Interactive Visual Canvas
        item {
            PlanVisualCanvas(
                wallWidthMm = planInput.wallWidthMm,
                idealOffsetMm = planInput.idealOffsetMm,
                candidate = selectedCandidate
            )
        }

        // Input Fields
        item {
            StepperNumberField(
                label = "外壁施工幅 (Building Wall Width)",
                value = planInput.wallWidthMm,
                onValueChange = { viewModel.updatePlanWallWidth(it) },
                step = 100.0,
                min = 900.0,
                max = 50000.0,
                unit = "mm",
                quickSteps = listOf(100.0, 450.0, 900.0, 1800.0),
                modifier = Modifier.testTag("plan_wall_width_input")
            )
        }

        item {
            StepperNumberField(
                label = "理想の壁離れ (Ideal Clearance Offset)",
                value = planInput.idealOffsetMm,
                onValueChange = { viewModel.updatePlanIdealOffset(it) },
                step = 25.0,
                min = 100.0,
                max = 600.0,
                unit = "mm",
                quickSteps = listOf(25.0, 50.0, 100.0),
                modifier = Modifier.testTag("plan_offset_input")
            )
        }

        // Module selection & options
        item {
            Card(
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                shape = RoundedCornerShape(16.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(modifier = Modifier.padding(14.dp)) {
                    Text(
                        text = "割付規格 & 調整部材設定",
                        style = MaterialTheme.typography.titleSmall,
                        fontWeight = FontWeight.SemiBold
                    )
                    Spacer(modifier = Modifier.height(10.dp))

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        SystemModule.values().forEach { module ->
                            val isSelected = planInput.systemModule == module
                            FilterChip(
                                selected = isSelected,
                                onClick = { viewModel.updatePlanModule(module) },
                                label = {
                                    Text(
                                        text = if (module == SystemModule.METRIC_STANDARD) "メートル (1800)" else module.displayName.take(10),
                                        style = MaterialTheme.typography.labelSmall
                                    )
                                },
                                modifier = Modifier.weight(1f)
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(8.dp))
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = "150mm 端部調整スパンを許可",
                            style = MaterialTheme.typography.bodyMedium
                        )
                        Switch(
                            checked = planInput.allow150,
                            onCheckedChange = { viewModel.updatePlanAllow150(it) }
                        )
                    }
                }
            }
        }

        // Key Result Summary Cards (if candidate is selected)
        if (selectedCandidate != null) {
            val cand = selectedCandidate!!
            item {
                Text(
                    text = "割付計算サマリー",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold
                )
            }

            item {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    MetricStatCard(
                        title = "足場全幅 (Total Width)",
                        value = "${cand.totalScaffoldWidthMm}",
                        unit = "mm",
                        subtitle = "スパン数: ${cand.spans.size} bays",
                        icon = Icons.Default.WidthFull,
                        modifier = Modifier.weight(1f)
                    )
                    MetricStatCard(
                        title = "実離れ (Actual Offset)",
                        value = "${cand.actualOffsetMm.toInt()}",
                        unit = "mm",
                        subtitle = if (cand.offsetDiffMm >= 0) "+${cand.offsetDiffMm.toInt()}mm (余裕)" else "${cand.offsetDiffMm.toInt()}mm (詰まり)",
                        icon = Icons.Default.Tune,
                        badgeColor = if (cand.offsetDiffMm >= 0) MaterialTheme.colorScheme.primaryContainer else MaterialTheme.colorScheme.errorContainer,
                        modifier = Modifier.weight(1f)
                    )
                }
            }

            item {
                Card(
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.35f)),
                    shape = RoundedCornerShape(14.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(modifier = Modifier.padding(14.dp)) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = "スパン内訳 (Span Composition)",
                                style = MaterialTheme.typography.labelLarge,
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.primary
                            )
                            Surface(
                                color = MaterialTheme.colorScheme.primary,
                                shape = RoundedCornerShape(8.dp)
                            ) {
                                Text(
                                    text = "${cand.spans.size} スパン",
                                    color = MaterialTheme.colorScheme.onPrimary,
                                    style = MaterialTheme.typography.labelSmall,
                                    modifier = Modifier.padding(horizontal = 8.dp, vertical = 3.dp)
                                )
                            }
                        }
                        Spacer(modifier = Modifier.height(6.dp))
                        Text(
                            text = cand.spanBreakdownText,
                            style = MaterialTheme.typography.titleLarge,
                            fontWeight = FontWeight.ExtraBold,
                            color = MaterialTheme.colorScheme.onSurface
                        )
                        Spacer(modifier = Modifier.height(8.dp))
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(16.dp)
                        ) {
                            Text(
                                text = "支柱: ${cand.postCount} 本 (片面2列)",
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                            Text(
                                text = "アンチ: ${cand.plankCountPerTier} 枚/層",
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                            Text(
                                text = "手すり: ${cand.handrailCountPerTier} 本/層",
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }
                }
            }
        }

        // Candidate List
        item {
            Spacer(modifier = Modifier.height(6.dp))
            Text(
                text = "割付候補一覧 (${candidates.size} パターン)",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold
            )
        }

        items(candidates) { candidate ->
            val isSelected = candidate.id == selectedCandidate?.id
            PlanCandidateCard(
                candidate = candidate,
                isSelected = isSelected,
                onClick = { viewModel.selectCandidate(candidate) }
            )
        }
    }
}

@Composable
fun PlanCandidateCard(
    candidate: PlanCandidate,
    isSelected: Boolean,
    onClick: () -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(14.dp))
            .clickable { onClick() }
            .then(
                if (isSelected) Modifier.border(2.dp, MaterialTheme.colorScheme.primary, RoundedCornerShape(14.dp))
                else Modifier
            ),
        colors = CardDefaults.cardColors(
            containerColor = if (isSelected) MaterialTheme.colorScheme.surfaceVariant else MaterialTheme.colorScheme.surface
        ),
        shape = RoundedCornerShape(14.dp)
    ) {
        Column(modifier = Modifier.padding(14.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    if (isSelected) {
                        Icon(
                            imageVector = Icons.Default.CheckCircle,
                            contentDescription = "選択中",
                            tint = MaterialTheme.colorScheme.primary,
                            modifier = Modifier.size(20.dp)
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                    }
                    Text(
                        text = candidate.title,
                        style = MaterialTheme.typography.titleSmall,
                        fontWeight = FontWeight.Bold
                    )
                }

                if (candidate.isOptimal) {
                    Surface(
                        color = MaterialTheme.colorScheme.tertiary,
                        shape = RoundedCornerShape(6.dp)
                    ) {
                        Text(
                            text = "推奨",
                            color = MaterialTheme.colorScheme.onTertiary,
                            style = MaterialTheme.typography.labelSmall,
                            modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(4.dp))
            Text(
                text = candidate.description,
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )

            Spacer(modifier = Modifier.height(10.dp))
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Column {
                    Text(
                        text = "スパン構成",
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Text(
                        text = candidate.spanBreakdownText,
                        style = MaterialTheme.typography.bodyMedium,
                        fontWeight = FontWeight.SemiBold
                    )
                }
                Column(horizontalAlignment = Alignment.End) {
                    Text(
                        text = "全幅 / 実離れ",
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Text(
                        text = "${candidate.totalScaffoldWidthMm}mm / 離れ${candidate.actualOffsetMm.toInt()}mm",
                        style = MaterialTheme.typography.bodyMedium,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.primary
                    )
                }
            }
        }
    }
}
