package com.example.ui.components

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Remove
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun StepperNumberField(
    label: String,
    value: Double,
    onValueChange: (Double) -> Unit,
    step: Double = 100.0,
    min: Double = 0.0,
    max: Double = 50000.0,
    unit: String = "mm",
    quickSteps: List<Double> = listOf(100.0, 450.0, 900.0, 1800.0),
    modifier: Modifier = Modifier
) {
    var textValue by remember(value) { mutableStateOf(if (value % 1.0 == 0.0) value.toInt().toString() else value.toString()) }

    Card(
        modifier = modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        shape = RoundedCornerShape(16.dp),
        elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
    ) {
        Column(modifier = Modifier.padding(14.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = label,
                    style = MaterialTheme.typography.titleSmall,
                    fontWeight = FontWeight.SemiBold
                )
                Text(
                    text = "$textValue $unit",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.primary
                )
            }

            Spacer(modifier = Modifier.height(10.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                FilledTonalIconButton(
                    onClick = {
                        val next = (value - step).coerceAtLeast(min)
                        onValueChange(next)
                    },
                    modifier = Modifier.size(44.dp).testTag("stepper_minus")
                ) {
                    Icon(Icons.Default.Remove, contentDescription = "減少")
                }

                OutlinedTextField(
                    value = textValue,
                    onValueChange = { str ->
                        textValue = str
                        val parsed = str.toDoubleOrNull()
                        if (parsed != null && parsed in min..max) {
                            onValueChange(parsed)
                        }
                    },
                    modifier = Modifier
                        .weight(1f)
                        .testTag("stepper_input"),
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                    singleLine = true,
                    shape = RoundedCornerShape(12.dp),
                    trailingIcon = {
                        Text(
                            text = unit,
                            style = MaterialTheme.typography.bodySmall,
                            modifier = Modifier.padding(end = 12.dp),
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                )

                FilledTonalIconButton(
                    onClick = {
                        val next = (value + step).coerceAtMost(max)
                        onValueChange(next)
                    },
                    modifier = Modifier.size(44.dp).testTag("stepper_plus")
                ) {
                    Icon(Icons.Default.Add, contentDescription = "増加")
                }
            }

            if (quickSteps.isNotEmpty()) {
                Spacer(modifier = Modifier.height(8.dp))
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    quickSteps.forEach { qStep ->
                        val labelText = if (qStep % 1.0 == 0.0) "+${qStep.toInt()}" else "+$qStep"
                        SuggestionChip(
                            onClick = {
                                val next = (value + qStep).coerceAtMost(max)
                                onValueChange(next)
                            },
                            label = { Text(labelText, style = MaterialTheme.typography.labelSmall) },
                            modifier = Modifier.weight(1f)
                        )
                    }
                }
            }
        }
    }
}
