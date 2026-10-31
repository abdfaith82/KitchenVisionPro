package com.kitchenvision.pro.data.repository

import com.kitchenvision.pro.data.db.ProjectDao
import com.kitchenvision.pro.data.model.*
import com.google.gson.Gson
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

class ProjectRepository(private val dao: ProjectDao) {
    private val gson = Gson()

    fun getAllProjects(): Flow<List<KitchenProject>> {
        return dao.getAllProjects().map { entities ->
            entities.map { it.toDomain() }
        }
    }

    suspend fun getProject(id: String): KitchenProject? {
        return dao.getProjectById(id)?.toDomain()
    }

    suspend fun saveProject(project: KitchenProject) {
        dao.insertProject(project.toEntity())
    }

    suspend fun deleteProject(id: String) {
        dao.deleteById(id)
    }

    private fun ProjectEntity.toDomain(): KitchenProject {
        val linesType = object : com.google.gson.reflect.TypeToken<List<PerspectiveLine>>() {}.type
        val elementsType = object : com.google.gson.reflect.TypeToken<List<DesignElement>>() {}.type
        return KitchenProject(
            id = id,
            name = name,
            backgroundImagePath = backgroundImagePath,
            createdAt = createdAt,
            updatedAt = updatedAt,
            lines = gson.fromJson(linesJson, linesType) ?: emptyList(),
            elements = gson.fromJson(elementsJson, elementsType) ?: emptyList(),
            canvasWidth = canvasWidth,
            canvasHeight = canvasHeight
        )
    }

    private fun KitchenProject.toEntity(): ProjectEntity {
        return ProjectEntity(
            id = id,
            name = name,
            backgroundImagePath = backgroundImagePath,
            createdAt = createdAt,
            updatedAt = System.currentTimeMillis(),
            linesJson = gson.toJson(lines),
            elementsJson = gson.toJson(elements),
            canvasWidth = canvasWidth,
            canvasHeight = canvasHeight
        )
    }
}
