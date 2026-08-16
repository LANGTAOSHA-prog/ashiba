package com.example.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.FormatLineSpacing
import androidx.compose.material.icons.filled.Height
import androidx.compose.material.icons.filled.Layers
import androidx.compose.material.icons.filled.VerticalAlignTop
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.example.model.ScaffoldType
import com.example.model.TopSafetySpec
import com.example.ui.components.MetricStatCard
import com.example.ui.components.SectionVisualCanvas
import com.example.ui.components.StepperNumberField
import com.example.viewmodel.ScaffoldViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SectionLayoutScreen(
    viewModel: ScaffoldViewModel,
    modifier: Modifier = Modifier
) {
    val sectionInput by viewModel.sectionInput.collectAsState()
    val sectionResult by viewModel.sectionResult.collectAsState()

    LazyColumn(
        modifier = modifier
            .fillMaxSize()
            .padding(horizontal = 16.dp),
        verticalArrangement = Arrangement.spacedBy(14.dp),
        contentPadding = PaddingValues(top = 12.dp, bottom = 24.dp)
    ) {
        // Header
        item {
            Column {
                Text(
                    text = "断面・高さ割付 (Section Layout)",
                    style = MaterialTheme.typography.headlineSmall,
                    fontWeight = FontWeight.Bold
                )
                Text(
                    text = "建物軒高・ジャッキレベル・コマ位置から段数と柱の千鳥継ぎ構成を算出",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }

        // Section Visual Canvas
        item {
            SectionVisualCanvas(result = sectionResult)
        }

        // Target Height Input
        item {
            StepperNumberField(
                label = "建物対象軒高 (Target Eave Height)",
                value = sectionInput.targetHeightMm,
                onValueChange = { viewModel.updateSectionTargetHeight(it) },
                step = 100.0,
                min = 1800.0,
                max = 60000.0,
                unit = "mm",
                quickSteps = listOf(100.0, 450.0, 900.0, 1800.0),
                modifier = Modifier.testTag("section_height_input")
            )
        }

        // Jack Base Height
        item {
            StepperNumberField(
                label = "ジャッキベース調整高 (Jack Base Level)",
                value = sectionInput.jackBaseSettingMm,
                onValueChange = { viewModel.updateSectionJackBase(it) },
                step = 25.0,
                min = 50.0,
                max = 350.0,
                unit = "mm",
                quickSteps = listOf(25.0, 50.0, 100.0),
                modifier = Modifier.testTag("section_jack_input")
            )
        }

        // Scaffold Type & Top Safety Spec Selectors
        item {
            Card(
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                shape = RoundedCornerShape(16.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(modifier = Modifier.padding(14.dp)) {
                    Text(
                        text = "足場規格タイプ (Scaffold Standard)",
                        style = MaterialTheme.typography.titleSmall,
                        fontWeight = FontWeight.SemiBold
                    )
                    Spacer(modifier = Modifier.height(8.dp))

                    ScaffoldType.values().forEach { type ->
                        val isSelected = sectionInput.scaffoldType == type
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clip(RoundedCornerShape(8.dp))
                                .background(
                                    if (isSelected) MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.4f)
                                    else MaterialTheme.colorScheme.surface
                                )
                                .padding(vertical = 4.dp, horizontal = 6.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            RadioButton(
                                selected = isSelected,
                                onClick = { viewModel.updateSectionScaffoldType(type) }
                            )
                            Column(modifier = Modifier.padding(start = 6.dp)) {
                                Text(
                                    text = type.displayName,
                                    style = MaterialTheme.typography.bodyMedium,
                                    fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal
                                )
                                Text(
                                    text = type.description,
                                    style = MaterialTheme.typography.bodySmall,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }
                        }
                    }

                    Divider(modifier = Modifier.padding(vertical = 12.dp))

                    Text(
                        text = "最上部安全仕様 (Top Safety Spec)",
                        style = MaterialTheme.typography.titleSmall,
                        fontWeight = FontWeight.SemiBold
                    )
                    Spacer(modifier = Modifier.height(8.dp))

                    TopSafetySpec.values().forEach { spec ->
                        val isSelected = sectionInput.topSafetySpec == spec
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clip(RoundedCornerShape(8.dp))
                                .background(
                                    if (isSelected) MaterialTheme.colorScheme.secondaryContainer.copy(alpha = 0.4f)
                                    else MaterialTheme.colorScheme.surface
                                )
                                .padding(vertical = 4.dp, horizontal = 6.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            RadioButton(
                                selected = isSelected,
                                onClick = { viewModel.updateSectionTopSpec(spec) }
                            )
                            Column(modifier = Modifier.padding(start = 6.dp)) {
                                Text(
                                    text = spec.displayName,
                                    style = MaterialTheme.typography.bodyMedium,
                                    fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal
                                )
                                Text(
                                    text = spec.description,
                                    style = MaterialTheme.typography.bodySmall,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }
                        }
                    }
                }
            }
        }

        // Section Result Output
        if (sectionResult != null) {
            val res = sectionResult!!
            item {
                Text(
                    text = "高さ割付結果サマリー",
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
                        title = "総段数 (Total Tiers)",
                        value = "${res.totalTiers}",
                        unit = "層",
                        subtitle = "最上アンチ高: ${res.tiers.lastOrNull()?.plankElevationMm ?: 0}mm",
                        icon = Icons.Default.Layers,
                        modifier = Modifier.weight(1f)
                    )
                    MetricStatCard(
                        title = "足場全高 (Total Height)",
                        value = "${res.totalScaffoldHeightMm}",
                        unit = "mm",
                        subtitle = "軒先余裕: +${res.topEaveMarginMm}mm",
                        icon = Icons.Default.Height,
                        modifier = Modifier.weight(1f)
                    )
                }
            }

            item {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    MetricStatCard(
                        title = "ジャッキレベル",
                        value = "${res.jackLevelMm}",
                        unit = "mm",
                        subtitle = "調整範囲内 (50〜350mm)",
                        icon = Icons.Default.VerticalAlignTop,
                        badgeColor = MaterialTheme.colorScheme.tertiaryContainer,
                        badgeTextColor = MaterialTheme.colorScheme.onTertiaryContainer,
                        modifier = Modifier.weight(1f)
                    )
                    MetricStatCard(
                        title = "スタートコマ位置",
                        value = "${res.startCommaLevel}",
                        unit = "コマ目",
                        subtitle = res.startCommaDescription,
                        icon = Icons.Default.FormatLineSpacing,
                        badgeColor = MaterialTheme.colorScheme.secondaryContainer,
                        badgeTextColor = MaterialTheme.colorScheme.onSecondaryContainer,
                        modifier = Modifier.weight(1f)
                    )
                }
            }

            // Post Stagger Schedule
            item {
                Card(
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                    shape = RoundedCornerShape(14.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(modifier = Modifier.padding(14.dp)) {
                        Text(
                            text = "支柱の千鳥継ぎ割付 (Post Stagger Combination)",
                            style = MaterialTheme.typography.titleSmall,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.primary
                        )
                        Spacer(modifier = Modifier.height(10.dp))

                        // Outer posts
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = "外柱 (Outer Column):",
                                style = MaterialTheme.typography.bodyMedium,
                                fontWeight = FontWeight.SemiBold
                            )
                            Text(
                                text = res.outerPostSummary,
                                style = MaterialTheme.typography.bodyMedium,
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.primary
                            )
                        }

                        Spacer(modifier = Modifier.height(8.dp))

                        // Inner posts
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = "内柱 (Inner Column):",
                                style = MaterialTheme.typography.bodyMedium,
                                fontWeight = FontWeight.SemiBold
                            )
                            Text(
                                text = res.innerPostSummary,
                                style = MaterialTheme.typography.bodyMedium,
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.secondary
                            )
                        }

                        Spacer(modifier = Modifier.height(8.dp))
                        Text(
                            text = "※ 内柱を1800mmスタートとすることで、ジョイントピン位置が同一層で揃わない安全な千鳥構造となります。",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
            }

            // Tier Schedule
            item {
                Text(
                    text = "層別高さスケジュール (${res.tiers.size} 層)",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold
                )
            }

            items(res.tiers.reversed()) { tier ->
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                    shape = RoundedCornerShape(12.dp)
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(12.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column {
                            Text(
                                text = "第 ${tier.tierNumber} 層 (Stage ${tier.tierNumber})",
                                style = MaterialTheme.typography.titleSmall,
                                fontWeight = FontWeight.Bold
                            )
                            Text(
                                text = "手すり高: +${tier.handrailElevationMm} mm / 巾木高: +${tier.toeBoardElevationMm} mm",
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                        Surface(
                            color = MaterialTheme.colorScheme.primaryContainer,
                            shape = RoundedCornerShape(8.dp)
                        ) {
                            Text(
                                text = "+${tier.plankElevationMm} mm",
                                style = MaterialTheme.typography.titleSmall,
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.onPrimaryContainer,
                                modifier = Modifier.padding(horizontal = 10.dp, vertical = 5.dp)
                            )
                        }
                    }
                }
            }
        }
    }
}
