package com.example.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.example.model.BOMItem
import com.example.model.MaterialCategory
import com.example.ui.components.MetricStatCard
import com.example.ui.components.StepperNumberField
import com.example.viewmodel.ScaffoldViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun BuildingBOMScreen(
    viewModel: ScaffoldViewModel,
    modifier: Modifier = Modifier
) {
    val input by viewModel.fourSidesInput.collectAsState()
    val result by viewModel.buildingResult.collectAsState()

    var showOptionsDialog by remember { mutableStateOf(false) }

    LazyColumn(
        modifier = modifier
            .fillMaxSize()
            .padding(horizontal = 16.dp),
        verticalArrangement = Arrangement.spacedBy(14.dp),
        contentPadding = PaddingValues(top = 12.dp, bottom = 24.dp)
    ) {
        // Title Header
        item {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = "4面・全体積算 & 見積 (BOM & Quote)",
                        style = MaterialTheme.typography.headlineSmall,
                        fontWeight = FontWeight.Bold
                    )
                    Text(
                        text = "建物4面寸法から外周・架面積・部材拾い出し・積載車両・概算金額を一括算出",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
                IconButton(
                    onClick = { showOptionsDialog = true },
                    modifier = Modifier.testTag("bom_options_btn")
                ) {
                    Icon(Icons.Default.Settings, contentDescription = "積算条件設定")
                }
            }
        }

        // Site Name
        item {
            OutlinedTextField(
                value = input.siteName,
                onValueChange = { viewModel.updateBuildingSiteName(it) },
                label = { Text("現場名 / 案件名") },
                modifier = Modifier.fillMaxWidth().testTag("bom_site_name"),
                singleLine = true,
                shape = RoundedCornerShape(12.dp),
                leadingIcon = {
                    Icon(Icons.Default.Business, contentDescription = null)
                }
            )
        }

        // 4-Sides Dimensions Inputs
        item {
            Card(
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                shape = RoundedCornerShape(16.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(modifier = Modifier.padding(14.dp)) {
                    Text(
                        text = "建物4面寸法 (4-Side Building Dimensions)",
                        style = MaterialTheme.typography.titleSmall,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.primary
                    )
                    Spacer(modifier = Modifier.height(10.dp))

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        OutlinedTextField(
                            value = input.sideA_Mm.toInt().toString(),
                            onValueChange = { str ->
                                val v = str.toDoubleOrNull() ?: input.sideA_Mm
                                viewModel.updateBuildingSides(v, input.sideB_Mm, input.sideC_Mm, input.sideD_Mm, input.heightMm)
                            },
                            label = { Text("正面 (A面)") },
                            suffix = { Text("mm") },
                            modifier = Modifier.weight(1f),
                            singleLine = true,
                            shape = RoundedCornerShape(10.dp)
                        )
                        OutlinedTextField(
                            value = input.sideB_Mm.toInt().toString(),
                            onValueChange = { str ->
                                val v = str.toDoubleOrNull() ?: input.sideB_Mm
                                viewModel.updateBuildingSides(input.sideA_Mm, v, input.sideC_Mm, input.sideD_Mm, input.heightMm)
                            },
                            label = { Text("右側面 (B面)") },
                            suffix = { Text("mm") },
                            modifier = Modifier.weight(1f),
                            singleLine = true,
                            shape = RoundedCornerShape(10.dp)
                        )
                    }

                    Spacer(modifier = Modifier.height(10.dp))

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        OutlinedTextField(
                            value = input.sideC_Mm.toInt().toString(),
                            onValueChange = { str ->
                                val v = str.toDoubleOrNull() ?: input.sideC_Mm
                                viewModel.updateBuildingSides(input.sideA_Mm, input.sideB_Mm, v, input.sideD_Mm, input.heightMm)
                            },
                            label = { Text("背面 (C面)") },
                            suffix = { Text("mm") },
                            modifier = Modifier.weight(1f),
                            singleLine = true,
                            shape = RoundedCornerShape(10.dp)
                        )
                        OutlinedTextField(
                            value = input.sideD_Mm.toInt().toString(),
                            onValueChange = { str ->
                                val v = str.toDoubleOrNull() ?: input.sideD_Mm
                                viewModel.updateBuildingSides(input.sideA_Mm, input.sideB_Mm, input.sideC_Mm, v, input.heightMm)
                            },
                            label = { Text("左側面 (D面)") },
                            suffix = { Text("mm") },
                            modifier = Modifier.weight(1f),
                            singleLine = true,
                            shape = RoundedCornerShape(10.dp)
                        )
                    }

                    Spacer(modifier = Modifier.height(10.dp))

                    OutlinedTextField(
                        value = input.heightMm.toInt().toString(),
                        onValueChange = { str ->
                            val v = str.toDoubleOrNull() ?: input.heightMm
                            viewModel.updateBuildingSides(input.sideA_Mm, input.sideB_Mm, input.sideC_Mm, input.sideD_Mm, v)
                        },
                        label = { Text("建物軒高 (Height)") },
                        suffix = { Text("mm") },
                        modifier = Modifier.fillMaxWidth(),
                        singleLine = true,
                        shape = RoundedCornerShape(10.dp)
                    )
                }
            }
        }

        // Summary Cards
        if (result != null) {
            val res = result!!
            item {
                Text(
                    text = "積算集計サマリー",
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
                        title = "架面積 (Scaffold Area)",
                        value = String.format("%.1f", res.scaffoldAreaM2),
                        unit = "㎡",
                        subtitle = "外壁実面積: ${String.format("%.1f", res.wallAreaM2)}㎡",
                        icon = Icons.Default.AspectRatio,
                        modifier = Modifier.weight(1f)
                    )
                    MetricStatCard(
                        title = "足場外周長",
                        value = String.format("%.1f", res.scaffoldPerimeterM),
                        unit = "m",
                        subtitle = "建物外周: ${String.format("%.1f", res.buildingPerimeterM)}m",
                        icon = Icons.Default.Straighten,
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
                        title = "総部材重量 (Total Weight)",
                        value = String.format("%.0f", res.totalWeightKg),
                        unit = "kg",
                        subtitle = res.truckEstimate.recommendation,
                        icon = Icons.Default.LocalShipping,
                        badgeColor = MaterialTheme.colorScheme.secondaryContainer,
                        badgeTextColor = MaterialTheme.colorScheme.onSecondaryContainer,
                        modifier = Modifier.weight(1f)
                    )
                    MetricStatCard(
                        title = "概算見積金額 (税込)",
                        value = "¥${String.format("%,d", res.costQuote.grandTotal.toInt())}",
                        unit = "",
                        subtitle = "施工+リース(${input.rentalMonths}ヶ月)+諸経費",
                        icon = Icons.Default.AttachMoney,
                        badgeColor = MaterialTheme.colorScheme.tertiaryContainer,
                        badgeTextColor = MaterialTheme.colorScheme.onTertiaryContainer,
                        modifier = Modifier.weight(1f)
                    )
                }
            }

            // Cost Quote Card
            item {
                Card(
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                    shape = RoundedCornerShape(14.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(modifier = Modifier.padding(14.dp)) {
                        Text(
                            text = "概算見積内訳書 (Estimate Breakdown)",
                            style = MaterialTheme.typography.titleSmall,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.primary
                        )
                        Spacer(modifier = Modifier.height(10.dp))

                        CostRow(label = "足場組立解体工費 (${input.unitPricePerM2}円/㎡)", amount = res.costQuote.erectionAndDismantleCost)
                        CostRow(label = "資材リース料 (${input.rentalPricePerM2Month}円/㎡・${input.rentalMonths}ヶ月)", amount = res.costQuote.rentalCost)
                        if (input.includeMeshSheet) {
                            CostRow(label = "メッシュシート損料", amount = res.costQuote.meshSheetCost)
                        }
                        CostRow(label = "運搬搬入出・諸経費", amount = res.costQuote.transportAndMiscCost)

                        Divider(modifier = Modifier.padding(vertical = 8.dp))

                        CostRow(label = "小計 (税抜)", amount = res.costQuote.subtotal, isBold = true)
                        CostRow(label = "消費税 (10%)", amount = res.costQuote.consumptionTax)

                        Spacer(modifier = Modifier.height(4.dp))
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .background(MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.4f), RoundedCornerShape(8.dp))
                                .padding(10.dp),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = "お見積合計 (税込)",
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.onSurface
                            )
                            Text(
                                text = "¥${String.format("%,d", res.costQuote.grandTotal.toInt())}",
                                style = MaterialTheme.typography.titleLarge,
                                fontWeight = FontWeight.ExtraBold,
                                color = MaterialTheme.colorScheme.primary
                            )
                        }
                    }
                }
            }

            // BOM List Header
            item {
                Text(
                    text = "必要部材拾い出し一覧 (BOM - ${res.bomList.size} 項目)",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold
                )
            }

            // Group by category
            items(res.bomList) { item ->
                BOMItemRow(item = item)
            }
        }
    }

    // Options Dialog
    if (showOptionsDialog) {
        var tempStairs by remember { mutableStateOf(input.hasStairs) }
        var tempMesh by remember { mutableStateOf(input.includeMeshSheet) }
        var tempFan by remember { mutableStateOf(input.includeProtectiveFan) }
        var tempMonths by remember { mutableStateOf(input.rentalMonths.toString()) }
        var tempUnitPrice by remember { mutableStateOf(input.unitPricePerM2.toInt().toString()) }

        AlertDialog(
            onDismissRequest = { showOptionsDialog = false },
            title = { Text("積算・見積オプション設定") },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text("昇降階段ユニット設置")
                        Switch(checked = tempStairs, onCheckedChange = { tempStairs = it })
                    }
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text("防炎メッシュシート全周")
                        Switch(checked = tempMesh, onCheckedChange = { tempMesh = it })
                    }
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text("朝顔 (落下物防護棚)")
                        Switch(checked = tempFan, onCheckedChange = { tempFan = it })
                    }
                    OutlinedTextField(
                        value = tempMonths,
                        onValueChange = { tempMonths = it },
                        label = { Text("リース期間 (ヶ月)") },
                        singleLine = true
                    )
                    OutlinedTextField(
                        value = tempUnitPrice,
                        onValueChange = { tempUnitPrice = it },
                        label = { Text("組立解体単価 (円/㎡)") },
                        singleLine = true
                    )
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        val m = tempMonths.toIntOrNull() ?: input.rentalMonths
                        val p = tempUnitPrice.toDoubleOrNull() ?: input.unitPricePerM2
                        viewModel.updateBuildingOptions(tempStairs, tempMesh, tempFan, m, p)
                        showOptionsDialog = false
                    }
                ) {
                    Text("適用")
                }
            },
            dismissButton = {
                TextButton(onClick = { showOptionsDialog = false }) {
                    Text("キャンセル")
                }
            }
        )
    }
}

@Composable
fun CostRow(label: String, amount: Double, isBold: Boolean = false) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 3.dp),
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Text(
            text = label,
            style = if (isBold) MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.Bold) else MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
        Text(
            text = "¥${String.format("%,d", amount.toInt())}",
            style = if (isBold) MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.Bold) else MaterialTheme.typography.bodySmall,
            fontWeight = if (isBold) FontWeight.Bold else FontWeight.Normal
        )
    }
}

@Composable
fun BOMItemRow(item: BOMItem) {
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
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = item.name,
                    style = MaterialTheme.typography.titleSmall,
                    fontWeight = FontWeight.Bold
                )
                Text(
                    text = "${item.spec} / 重量: ${String.format("%.1f", item.unitWeightKg)}kg",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
            Column(horizontalAlignment = Alignment.End) {
                Text(
                    text = "${item.quantity} ${item.unit}",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.ExtraBold,
                    color = MaterialTheme.colorScheme.primary
                )
                Text(
                    text = "計 ${String.format("%.0f", item.totalWeightKg)}kg",
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }
    }
}
