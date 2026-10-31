package com.kitchenvision.pro.ui.screens

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.kitchenvision.pro.data.model.ElementLibrary
import com.kitchenvision.pro.data.model.ElementType

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun LibraryScreen(
    onSelectElement: (ElementType, String) -> Unit,
    onBack: () -> Unit
) {
    var selectedTab by remember { mutableIntStateOf(0) }
    val tabs = listOf("خزائن", "أجهزة", "ديكور", "إضاءة")

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("مكتبة العناصر") },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "رجوع")
                    }
                }
            )
        }
    ) { padding ->
        Column(Modifier.padding(padding)) {
            TabRow(selectedTabIndex = selectedTab) {
                tabs.forEachIndexed { index, title ->
                    Tab(
                        selected = selectedTab == index,
                        onClick = { selectedTab = index },
                        text = { Text(title) }
                    )
                }
            }

            val items = when (selectedTab) {
                0 -> (ElementLibrary.cabinetsUpper + ElementLibrary.cabinetsLower)
                    .map { it to if (it.contains("upper")) ElementType.CABINET_UPPER else ElementType.CABINET_LOWER }
                1 -> ElementLibrary.appliances.map { it to ElementType.APPLIANCE }
                2 -> ElementLibrary.decor.map { it to ElementType.DECOR }
                else -> ElementLibrary.lighting.map { it to ElementType.LIGHTING }
            }

            LazyVerticalGrid(
                columns = GridCells.Fixed(3),
                contentPadding = PaddingValues(16.dp),
                horizontalArrangement = Arrangement.spacedBy(12.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                items(items) { (name, type) ->
                    Card(
                        onClick = { onSelectElement(type, name) },
                        modifier = Modifier
                            .aspectRatio(1f)
                            .fillMaxWidth()
                    ) {
                        Box(
                            contentAlignment = Alignment.Center,
                            modifier = Modifier.fillMaxSize().padding(8.dp)
                        ) {
                            Text(
                                name.replace("_", " "),
                                style = MaterialTheme.typography.labelMedium,
                                fontWeight = FontWeight.Medium
                            )
                        }
                    }
                }
            }
        }
    }
}
