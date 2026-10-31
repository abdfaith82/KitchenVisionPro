package com.kitchenvision.pro.ui.screens

import android.Manifest
import android.content.pm.PackageManager
import android.net.Uri
import android.os.Environment
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.gestures.detectDragGestures
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.gestures.detectTransformGestures
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.*
import androidx.compose.material.icons.outlined.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.PathEffect
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.layout.onSizeChanged
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.IntSize
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.content.ContextCompat
import androidx.core.content.FileProvider
import coil.compose.AsyncImage
import coil.request.ImageRequest
import com.kitchenvision.pro.data.model.ElementLibrary
import com.kitchenvision.pro.data.model.ElementType
import com.kitchenvision.pro.data.model.MaterialLibrary
import com.kitchenvision.pro.viewmodel.DesignViewModel
import com.kitchenvision.pro.ui.components.AssetImage
import java.io.File
import java.text.SimpleDateFormat
import java.util.*
import kotlin.math.roundToInt

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DesignCanvasScreen(
    viewModel: DesignViewModel,
    onBack: () -> Unit,
    onOpenLibrary: () -> Unit
) {
    val context = LocalContext.current
    val density = LocalDensity.current

    var showMaterialSheet by remember { mutableStateOf(false) }
    var showAddSheet by remember { mutableStateOf(false) }
    var showImageSourceSheet by remember { mutableStateOf(false) }
    var liveLineEnd by remember { mutableStateOf<Offset?>(null) }
    var canvasSizePx by remember { mutableStateOf(IntSize(1080, 1920)) }

    var tempCameraUri by remember { mutableStateOf<Uri?>(null) }

    fun createImageUri(): Uri {
        val timeStamp = SimpleDateFormat("yyyyMMdd_HHmmss", Locale.US).format(Date())
        val storageDir = context.getExternalFilesDir(Environment.DIRECTORY_PICTURES)
        val file = File.createTempFile("KV_${timeStamp}_", ".jpg", storageDir)
        return FileProvider.getUriForFile(context, "${context.packageName}.fileprovider", file)
    }

    val takePictureLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.TakePicture()
    ) { success ->
        if (success) tempCameraUri?.let { viewModel.setBackground(it) }
    }

    val cameraPermissionLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestPermission()
    ) { granted ->
        if (granted) {
            val uri = createImageUri()
            tempCameraUri = uri
            takePictureLauncher.launch(uri)
        }
    }

    fun launchCamera() {
        when {
            ContextCompat.checkSelfPermission(context, Manifest.permission.CAMERA) ==
                    PackageManager.PERMISSION_GRANTED -> {
                val uri = createImageUri()
                tempCameraUri = uri
                takePictureLauncher.launch(uri)
            }
            else -> cameraPermissionLauncher.launch(Manifest.permission.CAMERA)
        }
    }

    val photoPicker = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.GetContent()
    ) { uri: Uri? -> uri?.let { viewModel.setBackground(it) } }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(viewModel.currentProject?.name ?: "تصميم مطبخ", maxLines = 1) },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "رجوع")
                    }
                },
                actions = {
                    IconButton(onClick = { showImageSourceSheet = true }) {
                        Icon(Icons.Default.AddAPhoto, contentDescription = "صورة")
                    }
                    IconButton(onClick = { viewModel.showLayersPanel = !viewModel.showLayersPanel }) {
                        Icon(Icons.Default.Layers, contentDescription = "طبقات")
                    }
                    IconButton(onClick = { viewModel.saveCurrentProject() }) {
                        Icon(Icons.Default.Save, contentDescription = "حفظ")
                    }
                    IconButton(
                        onClick = { viewModel.exportFinalImage(true) },
                        enabled = !viewModel.isExporting
                    ) {
                        if (viewModel.isExporting) {
                            CircularProgressIndicator(Modifier.size(22.dp), strokeWidth = 2.dp)
                        } else {
                            Icon(Icons.Default.Share, contentDescription = "تصدير")
                        }
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.surface.copy(alpha = 0.96f)
                )
            )
        },
        bottomBar = {
            DesignBottomBar(
                currentTool = viewModel.currentTool,
                onToolSelected = { viewModel.setTool(it) },
                onAddElement = { showAddSheet = true },
                onMaterial = { showMaterialSheet = true },
                onUndoLine = { viewModel.removeLastLine() },
                onExport = { viewModel.exportFinalImage(true) }
            )
        }
    ) { padding ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .background(Color(0xFF1E1E1E))
                .onSizeChanged { size ->
                    canvasSizePx = size
                    viewModel.setCanvasSize(size.width.toFloat(), size.height.toFloat())
                }
        ) {
            // Background
            if (viewModel.backgroundUri != null) {
                AsyncImage(
                    model = ImageRequest.Builder(context)
                        .data(viewModel.backgroundUri)
                        .crossfade(true)
                        .build(),
                    contentDescription = null,
                    modifier = Modifier.fillMaxSize(),
                    contentScale = ContentScale.Crop
                )
            } else {
                Box(
                    Modifier.fillMaxSize().background(Color(0xFF2C2C2C)),
                    contentAlignment = Alignment.Center
                ) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Icon(Icons.Outlined.AddAPhoto, null, Modifier.size(72.dp), Color.White.copy(0.45f))
                        Spacer(Modifier.height(12.dp))
                        Text("أضف صورة الغرفة للبدء", color = Color.White.copy(0.7f), fontSize = 15.sp)
                        Spacer(Modifier.height(10.dp))
                        Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                            FilledTonalButton(onClick = { launchCamera() }) {
                                Icon(Icons.Default.PhotoCamera, null, Modifier.size(18.dp))
                                Spacer(Modifier.width(6.dp))
                                Text("كاميرا")
                            }
                            FilledTonalButton(onClick = { photoPicker.launch("image/*") }) {
                                Icon(Icons.Default.PhotoLibrary, null, Modifier.size(18.dp))
                                Spacer(Modifier.width(6.dp))
                                Text("معرض")
                            }
                        }
                    }
                }
            }

            // Perspective canvas
            Canvas(
                modifier = Modifier
                    .fillMaxSize()
                    .pointerInput(viewModel.currentTool, viewModel.selectedElementId, viewModel.elements) {
                        detectTapGestures { offset ->
                            when (viewModel.currentTool) {
                                DesignViewModel.Tool.SELECT -> {
                                    val hit = viewModel.sortedElements
                                        .filter { it.visible && !it.locked }
                                        .findLast { el ->
                                            val w = el.width * el.scale
                                            val h = el.height * el.scale
                                            offset.x in el.position.x..(el.position.x + w) &&
                                                    offset.y in el.position.y..(el.position.y + h)
                                        }
                                    viewModel.selectElement(hit?.id)
                                }
                                DesignViewModel.Tool.PERSPECTIVE -> {
                                    if (!viewModel.isDrawingLine) {
                                        viewModel.startLine(offset)
                                        liveLineEnd = offset
                                    } else {
                                        viewModel.endLine(offset)
                                        liveLineEnd = null
                                    }
                                }
                                else -> {}
                            }
                        }
                    }
                    .pointerInput(viewModel.currentTool, viewModel.selectedElementId) {
                        detectDragGestures(
                            onDragStart = { offset ->
                                if (viewModel.currentTool == DesignViewModel.Tool.PERSPECTIVE && !viewModel.isDrawingLine) {
                                    viewModel.startLine(offset)
                                    liveLineEnd = offset
                                }
                            },
                            onDrag = { change, dragAmount ->
                                when {
                                    viewModel.currentTool == DesignViewModel.Tool.PERSPECTIVE -> {
                                        liveLineEnd = change.position
                                    }
                                    viewModel.currentTool == DesignViewModel.Tool.SELECT &&
                                            viewModel.selectedElementId != null -> {
                                        val el = viewModel.elements.find { it.id == viewModel.selectedElementId }
                                        el?.let {
                                            if (!it.locked) {
                                                viewModel.updateElementPosition(
                                                    it.id,
                                                    Offset(
                                                        (it.position.x + dragAmount.x).coerceIn(0f, canvasSizePx.width - it.width * it.scale),
                                                        (it.position.y + dragAmount.y).coerceIn(0f, canvasSizePx.height - it.height * it.scale)
                                                    )
                                                )
                                            }
                                        }
                                    }
                                }
                            },
                            onDragEnd = {
                                if (viewModel.currentTool == DesignViewModel.Tool.PERSPECTIVE && liveLineEnd != null) {
                                    viewModel.endLine(liveLineEnd!!)
                                    liveLineEnd = null
                                }
                            },
                            onDragCancel = {
                                viewModel.cancelLine()
                                liveLineEnd = null
                            }
                        )
                    }
            ) {
                viewModel.perspectiveLines.forEach { line ->
                    drawLine(Color(0xFFFF5722), line.start, line.end, 3.5f,
                        pathEffect = PathEffect.dashPathEffect(floatArrayOf(14f, 9f)))
                    drawCircle(Color(0xFFFF5722), 9f, line.start)
                    drawCircle(Color(0xFFFF5722), 9f, line.end)
                }
                if (viewModel.isDrawingLine && viewModel.tempLineStart != null && liveLineEnd != null) {
                    drawLine(Color(0xFFFFAB91), viewModel.tempLineStart!!, liveLineEnd!!, 3f,
                        pathEffect = PathEffect.dashPathEffect(floatArrayOf(12f, 7f)))
                }
            }

            // Real PNG elements sorted by zIndex
            viewModel.sortedElements.filter { it.visible }.forEach { element ->
                val isSelected = element.id == viewModel.selectedElementId
                Box(
                    modifier = Modifier
                        .offset {
                            IntOffset(element.position.x.roundToInt(), element.position.y.roundToInt())
                        }
                        .size(
                            with(density) { (element.width * element.scale).toDp() },
                            with(density) { (element.height * element.scale).toDp() }
                        )
                        .graphicsLayer { rotationZ = element.rotation }
                        .then(
                            if (isSelected) Modifier.border(2.5.dp, Color(0xFF1A73E8), RoundedCornerShape(4.dp))
                            else Modifier
                        )
                        .alpha(if (element.locked) 0.85f else 1f)
                        .then(
                            if (isSelected && !element.locked) {
                                Modifier.pointerInput(element.id) {
                                    detectTransformGestures { _, _, zoom, _ ->
                                        viewModel.updateElementScale(element.id, element.scale * zoom)
                                    }
                                }
                            } else Modifier
                        )
                ) {
                    AssetImage(
                        assetName = element.assetName,
                        contentDescription = element.name,
                        modifier = Modifier.fillMaxSize(),
                        contentScale = ContentScale.Fit
                    )
                }
            }

            // Perspective tip
            if (viewModel.currentTool == DesignViewModel.Tool.PERSPECTIVE) {
                Surface(
                    Modifier.align(Alignment.TopCenter).padding(top = 10.dp),
                    color = Color(0xFFE64A19),
                    shape = RoundedCornerShape(20.dp),
                    shadowElevation = 4.dp
                ) {
                    Text(
                        "ارسم خطوط المنظور • اسحب أو اضغط مرتين",
                        color = Color.White, fontSize = 12.sp,
                        modifier = Modifier.padding(horizontal = 14.dp, vertical = 7.dp)
                    )
                }
            }

            // Selected element controls
            viewModel.selectedElementId?.let { id ->
                val el = viewModel.elements.find { it.id == id } ?: return@let
                Surface(
                    Modifier.align(Alignment.BottomCenter).padding(bottom = 8.dp),
                    color = MaterialTheme.colorScheme.surface.copy(alpha = 0.95f),
                    shape = RoundedCornerShape(12.dp),
                    shadowElevation = 6.dp
                ) {
                    Row(
                        Modifier.padding(horizontal = 10.dp, vertical = 6.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(4.dp)
                    ) {
                        Text(el.name.ifEmpty { el.assetName }, fontSize = 12.sp, fontWeight = FontWeight.Medium, maxLines = 1)
                        Text("×${"%.1f".format(el.scale)}", fontSize = 11.sp, color = Color.Gray)
                        IconButton(onClick = { viewModel.updateElementScale(el.id, el.scale + 0.12f) }, Modifier.size(30.dp)) {
                            Icon(Icons.Default.Add, null, Modifier.size(16.dp))
                        }
                        IconButton(onClick = { viewModel.updateElementScale(el.id, el.scale - 0.12f) }, Modifier.size(30.dp)) {
                            Icon(Icons.Default.Remove, null, Modifier.size(16.dp))
                        }
                        IconButton(onClick = { viewModel.bringToFront(el.id) }, Modifier.size(30.dp)) {
                            Icon(Icons.Default.FlipToFront, null, Modifier.size(16.dp))
                        }
                        IconButton(onClick = { viewModel.sendToBack(el.id) }, Modifier.size(30.dp)) {
                            Icon(Icons.Default.FlipToBack, null, Modifier.size(16.dp))
                        }
                        IconButton(onClick = { viewModel.toggleLock(el.id) }, Modifier.size(30.dp)) {
                            Icon(if (el.locked) Icons.Default.Lock else Icons.Default.LockOpen, null, Modifier.size(16.dp))
                        }
                        IconButton(onClick = { viewModel.deleteSelectedElement() }, Modifier.size(30.dp)) {
                            Icon(Icons.Default.Delete, null, Modifier.size(16.dp), tint = MaterialTheme.colorScheme.error)
                        }
                    }
                }
            }

            // Layers panel
            if (viewModel.showLayersPanel) {
                LayersPanel(
                    elements = viewModel.sortedElements.reversed(), // top layer first
                    selectedId = viewModel.selectedElementId,
                    onSelect = { viewModel.selectElement(it) },
                    onVisibility = { viewModel.toggleVisibility(it) },
                    onLock = { viewModel.toggleLock(it) },
                    onUp = { viewModel.moveLayerUp(it) },
                    onDown = { viewModel.moveLayerDown(it) },
                    onDelete = { viewModel.deleteElement(it) },
                    onClose = { viewModel.showLayersPanel = false },
                    modifier = Modifier
                        .align(Alignment.CenterEnd)
                        .fillMaxHeight()
                        .width(260.dp)
                        .padding(8.dp)
                )
            }
        }
    }

    // Sheets
    if (showImageSourceSheet) {
        ModalBottomSheet(onDismissRequest = { showImageSourceSheet = false }) {
            Column(Modifier.padding(20.dp)) {
                Text("مصدر الصورة", style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Bold)
                Spacer(Modifier.height(16.dp))
                ListItem(
                    headlineContent = { Text("التقاط بالكاميرا") },
                    leadingContent = { Icon(Icons.Default.PhotoCamera, null) },
                    modifier = Modifier.fillMaxWidth().clip(RoundedCornerShape(12.dp))
                        .background(MaterialTheme.colorScheme.surfaceVariant)
                        .pointerInput(Unit) { detectTapGestures { showImageSourceSheet = false; launchCamera() } }
                )
                Spacer(Modifier.height(8.dp))
                ListItem(
                    headlineContent = { Text("اختيار من المعرض") },
                    leadingContent = { Icon(Icons.Default.PhotoLibrary, null) },
                    modifier = Modifier.fillMaxWidth().clip(RoundedCornerShape(12.dp))
                        .background(MaterialTheme.colorScheme.surfaceVariant)
                        .pointerInput(Unit) { detectTapGestures { showImageSourceSheet = false; photoPicker.launch("image/*") } }
                )
                Spacer(Modifier.height(24.dp))
            }
        }
    }

    if (showAddSheet) {
        AddElementBottomSheet(
            onDismiss = { showAddSheet = false },
            onSelect = { type, asset ->
                viewModel.addElement(type, asset, Offset(canvasSizePx.width / 2f - 100f, canvasSizePx.height / 2f - 80f))
                showAddSheet = false
            }
        )
    }

    if (showMaterialSheet) {
        MaterialBottomSheet(
            onDismiss = { showMaterialSheet = false },
            onSelect = { mid ->
                viewModel.selectedElementId?.let { viewModel.applyMaterial(it, mid) }
                showMaterialSheet = false
            }
        )
    }
}

@Composable
private fun LayersPanel(
    elements: List<com.kitchenvision.pro.data.model.DesignElement>,
    selectedId: String?,
    onSelect: (String) -> Unit,
    onVisibility: (String) -> Unit,
    onLock: (String) -> Unit,
    onUp: (String) -> Unit,
    onDown: (String) -> Unit,
    onDelete: (String) -> Unit,
    onClose: () -> Unit,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    Surface(
        modifier = modifier,
        shape = RoundedCornerShape(16.dp),
        color = MaterialTheme.colorScheme.surface.copy(alpha = 0.97f),
        shadowElevation = 12.dp
    ) {
        Column(Modifier.fillMaxSize().padding(12.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(Icons.Default.Layers, null, tint = MaterialTheme.colorScheme.primary)
                Spacer(Modifier.width(8.dp))
                Text("الطبقات", fontWeight = FontWeight.Bold, modifier = Modifier.weight(1f))
                IconButton(onClick = onClose, modifier = Modifier.size(28.dp)) {
                    Icon(Icons.Default.Close, null, Modifier.size(18.dp))
                }
            }
            HorizontalDivider(Modifier.padding(vertical = 8.dp))
            if (elements.isEmpty()) {
                Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    Text("لا توجد عناصر", color = Color.Gray, fontSize = 13.sp)
                }
            } else {
                LazyColumn(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                    items(elements, key = { it.id }) { el ->
                        val selected = el.id == selectedId
                        Row(
                            Modifier
                                .fillMaxWidth()
                                .clip(RoundedCornerShape(10.dp))
                                .background(
                                    if (selected) MaterialTheme.colorScheme.primaryContainer
                                    else MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f)
                                )
                                .pointerInput(el.id) { detectTapGestures { onSelect(el.id) } }
                                .padding(8.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            AssetImage(
                                assetName = el.assetName,
                                contentDescription = null,
                                modifier = Modifier.size(36.dp).clip(RoundedCornerShape(6.dp)),
                                contentScale = ContentScale.Fit
                            )
                            Spacer(Modifier.width(8.dp))
                            Column(Modifier.weight(1f)) {
                                Text(el.name.ifEmpty { el.assetName }, fontSize = 12.sp, fontWeight = FontWeight.Medium, maxLines = 1)
                                Text("z=${el.zIndex}", fontSize = 10.sp, color = Color.Gray)
                            }
                            IconButton(onClick = { onVisibility(el.id) }, Modifier.size(28.dp)) {
                                Icon(
                                    if (el.visible) Icons.Default.Visibility else Icons.Default.VisibilityOff,
                                    null, Modifier.size(16.dp)
                                )
                            }
                            IconButton(onClick = { onLock(el.id) }, Modifier.size(28.dp)) {
                                Icon(
                                    if (el.locked) Icons.Default.Lock else Icons.Default.LockOpen,
                                    null, Modifier.size(16.dp)
                                )
                            }
                            IconButton(onClick = { onUp(el.id) }, Modifier.size(28.dp)) {
                                Icon(Icons.Default.KeyboardArrowUp, null, Modifier.size(16.dp))
                            }
                            IconButton(onClick = { onDown(el.id) }, Modifier.size(28.dp)) {
                                Icon(Icons.Default.KeyboardArrowDown, null, Modifier.size(16.dp))
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun DesignBottomBar(
    currentTool: DesignViewModel.Tool,
    onToolSelected: (DesignViewModel.Tool) -> Unit,
    onAddElement: () -> Unit,
    onMaterial: () -> Unit,
    onUndoLine: () -> Unit,
    onExport: () -> Unit
) {
    NavigationBar(tonalElevation = 8.dp) {
        NavigationBarItem(
            selected = currentTool == DesignViewModel.Tool.SELECT,
            onClick = { onToolSelected(DesignViewModel.Tool.SELECT) },
            icon = { Icon(Icons.Default.NearMe, null) },
            label = { Text("تحديد", fontSize = 10.sp) }
        )
        NavigationBarItem(
            selected = currentTool == DesignViewModel.Tool.PERSPECTIVE,
            onClick = { onToolSelected(DesignViewModel.Tool.PERSPECTIVE) },
            icon = { Icon(Icons.Default.Timeline, null) },
            label = { Text("منظور", fontSize = 10.sp) }
        )
        NavigationBarItem(
            selected = false, onClick = onAddElement,
            icon = { Icon(Icons.Default.AddBox, null) },
            label = { Text("عناصر", fontSize = 10.sp) }
        )
        NavigationBarItem(
            selected = false, onClick = onMaterial,
            icon = { Icon(Icons.Default.Palette, null) },
            label = { Text("مواد", fontSize = 10.sp) }
        )
        NavigationBarItem(
            selected = false, onClick = onUndoLine,
            icon = { Icon(Icons.Default.Undo, null) },
            label = { Text("تراجع", fontSize = 10.sp) }
        )
        NavigationBarItem(
            selected = false, onClick = onExport,
            icon = { Icon(Icons.Default.IosShare, null) },
            label = { Text("تصدير", fontSize = 10.sp) }
        )
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun AddElementBottomSheet(
    onDismiss: () -> Unit,
    onSelect: (ElementType, String) -> Unit
) {
    val context = LocalContext.current
    ModalBottomSheet(onDismissRequest = onDismiss) {
        Column(Modifier.padding(16.dp).fillMaxWidth()) {
            Text("إضافة عنصر", style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Bold)
            Spacer(Modifier.height(12.dp))

            listOf(
                "خزائن علوية" to ElementType.CABINET_UPPER,
                "خزائن سفلية" to ElementType.CABINET_LOWER,
                "أجهزة" to ElementType.APPLIANCE,
                "ديكور" to ElementType.DECOR,
                "إضاءة" to ElementType.LIGHTING,
                "أسطح" to ElementType.COUNTERTOP
            ).forEach { (title, type) ->
                Text(title, style = MaterialTheme.typography.titleSmall, color = MaterialTheme.colorScheme.primary)
                Spacer(Modifier.height(6.dp))
                Row(
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    ElementLibrary.byType(type).take(5).forEach { item ->
                        Column(
                            horizontalAlignment = Alignment.CenterHorizontally,
                            modifier = Modifier
                                .width(72.dp)
                                .clip(RoundedCornerShape(10.dp))
                                .background(MaterialTheme.colorScheme.surfaceVariant)
                                .pointerInput(item.assetName) {
                                    detectTapGestures { onSelect(item.type, item.assetName) }
                                }
                                .padding(6.dp)
                        ) {
                            AssetImage(
                                assetName = item.assetName,
                                contentDescription = item.labelAr,
                                modifier = Modifier.size(48.dp),
                                contentScale = ContentScale.Fit
                            )
                            Text(item.labelAr, fontSize = 9.sp, maxLines = 1)
                        }
                    }
                }
                Spacer(Modifier.height(12.dp))
            }
            Spacer(Modifier.height(16.dp))
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun MaterialBottomSheet(
    onDismiss: () -> Unit,
    onSelect: (String) -> Unit
) {
    ModalBottomSheet(onDismissRequest = onDismiss) {
        Column(Modifier.padding(16.dp)) {
            Text("اختر مادة", style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Bold)
            Spacer(Modifier.height(16.dp))
            MaterialLibrary.all.chunked(4).forEach { row ->
                Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                    row.forEach { mat ->
                        Column(
                            horizontalAlignment = Alignment.CenterHorizontally,
                            modifier = Modifier.weight(1f).pointerInput(mat.id) {
                                detectTapGestures { onSelect(mat.id) }
                            }
                        ) {
                            Box(
                                Modifier.size(52.dp).clip(CircleShape)
                                    .background(Color(mat.color))
                                    .border(1.5.dp, Color.Gray.copy(0.4f), CircleShape)
                            )
                            Spacer(Modifier.height(4.dp))
                            Text(mat.nameAr, fontSize = 11.sp, maxLines = 1)
                        }
                    }
                    repeat(4 - row.size) { Spacer(Modifier.weight(1f)) }
                }
                Spacer(Modifier.height(14.dp))
            }
            Spacer(Modifier.height(20.dp))
        }
    }
}
