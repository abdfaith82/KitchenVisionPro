package com.kitchenvision.pro.ui.screens

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.kitchenvision.pro.data.model.ElementLibrary
import com.kitchenvision.pro.data.model.ElementType
import com.kitchenvision.pro.ui.components.AssetImage

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun LibraryScreen(
    onSelectElement: (ElementType, String) -> Unit,
    onBack: () -> Unit
) {
    var selectedTab by remember { mutableIntStateOf(0) }
    val tabs = listOf(
        "خزائن علوية" to ElementType.CABINET_UPPER,
        "خزائن سفلية" to ElementType.CABINET_LOWER,
        "أجهزة" to ElementType.APPLIANCE,
        "ديكور" to ElementType.DECOR,
        "إضاءة" to ElementType.LIGHTING,
        "أسطح" to ElementType.COUNTERTOP
    )

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("مكتبة العناصر") },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.Default.ArrowBack, contentDescription = "رجوع")
                    }
                }
            )
        }
    ) { padding ->
        Column(Modifier.padding(padding)) {
            ScrollableTabRow(selectedTabIndex = selectedTab) {
                tabs.forEachIndexed { index, (title, _) ->
                    Tab(
                        selected = selectedTab == index,
                        onClick = { selectedTab = index },
                        text = { Text(title) }
                    )
                }
            }

            val type = tabs[selectedTab].second
            val libraryItems = ElementLibrary.byType(type)

            LazyVerticalGrid(
                columns = GridCells.Fixed(3),
                contentPadding = PaddingValues(16.dp),
                horizontalArrangement = Arrangement.spacedBy(12.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                items(libraryItems, key = { it.assetName }) { item ->
                    Card(
                        onClick = { onSelectElement(item.type, item.assetName) },
                        modifier = Modifier
                            .aspectRatio(1f)
                            .fillMaxWidth()
                    ) {
                        Column(
                            horizontalAlignment = Alignment.CenterHorizontally,
                            modifier = Modifier
                                .fillMaxSize()
                                .padding(8.dp),
                            verticalArrangement = Arrangement.Center
                        ) {
                            AssetImage(
                                assetName = item.assetName,
                                contentDescription = item.labelAr,
                                modifier = Modifier
                                    .weight(1f)
                                    .fillMaxWidth(),
                                contentScale = ContentScale.Fit
                            )
                            Spacer(Modifier.height(4.dp))
                            Text(
                                item.labelAr,
                                style = MaterialTheme.typography.labelSmall,
                                fontWeight = FontWeight.Medium,
                                maxLines = 1
                            )
                        }
                    }
                }
            }
        }
    }
}
