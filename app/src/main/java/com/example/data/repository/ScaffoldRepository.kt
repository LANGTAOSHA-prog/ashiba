package com.example.data.repository

import com.example.data.dao.ScaffoldProjectDao
import com.example.data.entity.ScaffoldProjectEntity
import kotlinx.coroutines.flow.Flow

class ScaffoldRepository(private val dao: ScaffoldProjectDao) {
    val allProjects: Flow<List<ScaffoldProjectEntity>> = dao.getAllProjects()

    suspend fun getProjectById(id: Long): ScaffoldProjectEntity? {
        return dao.getProjectById(id)
    }

    suspend fun saveProject(project: ScaffoldProjectEntity): Long {
        return dao.insertProject(project)
    }

    suspend fun updateProject(project: ScaffoldProjectEntity) {
        dao.updateProject(project)
    }

    suspend fun deleteProject(id: Long) {
        dao.deleteProjectById(id)
    }
}
