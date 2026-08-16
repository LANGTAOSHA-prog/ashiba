package com.example.data.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import com.example.data.entity.ScaffoldProjectEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface ScaffoldProjectDao {
    @Query("SELECT * FROM scaffold_projects ORDER BY updatedAt DESC")
    fun getAllProjects(): Flow<List<ScaffoldProjectEntity>>

    @Query("SELECT * FROM scaffold_projects WHERE id = :id")
    suspend fun getProjectById(id: Long): ScaffoldProjectEntity?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertProject(project: ScaffoldProjectEntity): Long

    @Update
    suspend fun updateProject(project: ScaffoldProjectEntity)

    @Query("DELETE FROM scaffold_projects WHERE id = :id")
    suspend fun deleteProjectById(id: Long)

    @Query("DELETE FROM scaffold_projects")
    suspend fun deleteAll()
}
