package com.kitchenvision.pro.viewmodel

import android.app.Application
import android.net.Uri
import android.widget.Toast
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.compose.ui.geometry.Offset
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.kitchenvision.pro.data.db.AppDatabase
import com.kitchenvision.pro.data.model.*
import com.kitchenvision.pro.data.repository.ProjectRepository
import com.kitchenvision.pro.utils.ExportUtils
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

class DesignViewModel(application: Application) : AndroidViewModel(application) {

    private val repository = ProjectRepository(
        AppDatabase.getInstance(application).projectDao()
    )
    private val appContext = application.applicationContext

    val projects = repository.getAllProjects()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    var currentProject by mutableStateOf<KitchenProject?>(null)
        private set
    var backgroundUri by mutableStateOf<Uri?>(null)
        private set
    var perspectiveLines by mutableStateOf<List<PerspectiveLine>>(emptyList())
        private set
    var elements by mutableStateOf<List<DesignElement>>(emptyList())
        private set
    var selectedElementId by mutableStateOf<String?>(null)
        private set
    var currentTool by mutableStateOf(Tool.SELECT)
        private set
    var isDrawingLine by mutableStateOf(false)
        private set
    var tempLineStart by mutableStateOf<Offset?>(null)
        private set
    var isExporting by mutableStateOf(false)
        private set
    var canvasSize by mutableStateOf(Pair(1080f, 1920f))
        private set
    var showLayersPanel by mutableStateOf(false)

    enum class Tool { SELECT, PERSPECTIVE, ADD_ELEMENT, MATERIAL, ERASER }

    val sortedElements: List<DesignElement>
        get() = elements.sortedBy { it.zIndex }

    fun startNewProject(name: String = "مطبخ جديد") {
        currentProject = KitchenProject(name = name)
        perspectiveLines = emptyList()
        elements = emptyList()
        backgroundUri = null
        selectedElementId = null
    }

    fun loadProject(project: KitchenProject) {
        currentProject = project
        perspectiveLines = project.lines
        elements = project.elements
        backgroundUri = project.backgroundImagePath?.let { Uri.parse(it) }
        canvasSize = Pair(project.canvasWidth, project.canvasHeight)
    }

    fun setBackground(uri: Uri) {
        backgroundUri = uri
        currentProject = currentProject?.copy(backgroundImagePath = uri.toString())
    }

    fun setCanvasSize(width: Float, height: Float) {
        canvasSize = Pair(width, height)
    }

    fun setTool(tool: Tool) {
        currentTool = tool
        if (tool != Tool.SELECT) selectedElementId = null
    }

    fun startLine(offset: Offset) {
        if (currentTool == Tool.PERSPECTIVE) {
            isDrawingLine = true
            tempLineStart = offset
        }
    }

    fun endLine(offset: Offset) {
        if (currentTool == Tool.PERSPECTIVE && tempLineStart != null) {
            perspectiveLines = perspectiveLines + PerspectiveLine(start = tempLineStart!!, end = offset)
            isDrawingLine = false
            tempLineStart = null
        }
    }

    fun cancelLine() {
        isDrawingLine = false
        tempLineStart = null
    }

    fun removeLastLine() {
        if (perspectiveLines.isNotEmpty()) perspectiveLines = perspectiveLines.dropLast(1)
    }

    fun addElement(type: ElementType, assetName: String, position: Offset) {
        val lib = ElementLibrary.find(assetName)
        val nextZ = (elements.maxOfOrNull { it.zIndex } ?: -1) + 1
        val element = DesignElement(
            type = type,
            assetName = assetName,
            position = position,
            width = lib?.defaultW ?: 160f,
            height = lib?.defaultH ?: 140f,
            zIndex = nextZ,
            name = lib?.labelAr ?: assetName
        )
        elements = elements + element
        selectedElementId = element.id
        currentTool = Tool.SELECT
    }

    fun selectElement(id: String?) { selectedElementId = id }

    fun updateElementPosition(id: String, newPos: Offset) {
        elements = elements.map {
            if (it.id == id && !it.locked) it.copy(position = newPos) else it
        }
    }

    fun updateElementScale(id: String, scale: Float) {
        elements = elements.map {
            if (it.id == id && !it.locked) it.copy(scale = scale.coerceIn(0.2f, 5f)) else it
        }
    }

    fun applyMaterial(elementId: String, materialId: String) {
        elements = elements.map {
            if (it.id == elementId) it.copy(materialId = materialId) else it
        }
    }

    fun deleteSelectedElement() {
        selectedElementId?.let { id ->
            elements = elements.filter { it.id != id }
            selectedElementId = null
        }
    }

    fun deleteElement(id: String) {
        elements = elements.filter { it.id != id }
        if (selectedElementId == id) selectedElementId = null
    }

    fun bringToFront(id: String) {
        val maxZ = elements.maxOfOrNull { it.zIndex } ?: 0
        elements = elements.map { if (it.id == id) it.copy(zIndex = maxZ + 1) else it }
    }

    fun sendToBack(id: String) {
        val minZ = elements.minOfOrNull { it.zIndex } ?: 0
        elements = elements.map { if (it.id == id) it.copy(zIndex = minZ - 1) else it }
    }

    fun moveLayerUp(id: String) {
        val sorted = elements.sortedBy { it.zIndex }
        val idx = sorted.indexOfFirst { it.id == id }
        if (idx < 0 || idx >= sorted.lastIndex) return
        val current = sorted[idx]
        val above = sorted[idx + 1]
        elements = elements.map {
            when (it.id) {
                current.id -> it.copy(zIndex = above.zIndex)
                above.id -> it.copy(zIndex = current.zIndex)
                else -> it
            }
        }
    }

    fun moveLayerDown(id: String) {
        val sorted = elements.sortedBy { it.zIndex }
        val idx = sorted.indexOfFirst { it.id == id }
        if (idx <= 0) return
        val current = sorted[idx]
        val below = sorted[idx - 1]
        elements = elements.map {
            when (it.id) {
                current.id -> it.copy(zIndex = below.zIndex)
                below.id -> it.copy(zIndex = current.zIndex)
                else -> it
            }
        }
    }

    fun toggleVisibility(id: String) {
        elements = elements.map { if (it.id == id) it.copy(visible = !it.visible) else it }
    }

    fun toggleLock(id: String) {
        elements = elements.map { if (it.id == id) it.copy(locked = !it.locked) else it }
    }

    fun saveCurrentProject() {
        val project = currentProject?.copy(
            lines = perspectiveLines,
            elements = elements,
            backgroundImagePath = backgroundUri?.toString(),
            canvasWidth = canvasSize.first,
            canvasHeight = canvasSize.second,
            updatedAt = System.currentTimeMillis()
        ) ?: return
        viewModelScope.launch {
            repository.saveProject(project)
            currentProject = project
            Toast.makeText(appContext, "تم حفظ المشروع", Toast.LENGTH_SHORT).show()
        }
    }

    fun deleteProject(id: String) {
        viewModelScope.launch { repository.deleteProject(id) }
    }

    fun exportFinalImage(shareAfterSave: Boolean = true) {
        if (isExporting) return
        isExporting = true
        viewModelScope.launch {
            try {
                val w = canvasSize.first.toInt().coerceAtLeast(720)
                val h = canvasSize.second.toInt().coerceAtLeast(1280)
                val bitmap = ExportUtils.renderDesignToBitmap(
                    context = appContext,
                    backgroundUri = backgroundUri,
                    lines = perspectiveLines,
                    elements = elements.filter { it.visible }.sortedBy { it.zIndex },
                    canvasWidth = w,
                    canvasHeight = h
                )
                val uri = ExportUtils.saveBitmapToGallery(
                    context = appContext,
                    bitmap = bitmap,
                    name = currentProject?.name ?: "KitchenDesign"
                )
                if (uri != null) {
                    ExportUtils.showToast(appContext, "تم حفظ الصورة في المعرض")
                    if (shareAfterSave) ExportUtils.shareImage(appContext, uri)
                } else {
                    ExportUtils.showToast(appContext, "فشل حفظ الصورة")
                }
                bitmap.recycle()
            } catch (e: Exception) {
                e.printStackTrace()
                ExportUtils.showToast(appContext, "خطأ: ${e.message}")
            } finally {
                isExporting = false
            }
        }
    }
}
