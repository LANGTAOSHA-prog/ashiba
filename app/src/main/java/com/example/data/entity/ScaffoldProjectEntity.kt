package com.example.data.entity

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "scaffold_projects")
data class ScaffoldProjectEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val title: String,
    val siteAddress: String = "",
    val clientName: String = "",
    val scaffoldType: String, // "KUSABI_A", "KUSABI_B", "FRAME_1700"
    val wallWidthMm: Double,
    val heightMm: Double,
    val idealOffsetMm: Double,
    val totalAreaM2: Double,
    val totalWeightKg: Double,
    val grandTotalYen: Double,
    val detailsJson: String = "", // stored summary or config details
    val notes: String = "",
    val createdAt: Long = System.currentTimeMillis(),
    val updatedAt: Long = System.currentTimeMillis()
)
