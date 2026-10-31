package com.kitchenvision.pro

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.Surface
import androidx.compose.ui.Modifier
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.kitchenvision.pro.ui.screens.DesignCanvasScreen
import com.kitchenvision.pro.ui.screens.HomeScreen
import com.kitchenvision.pro.ui.screens.LibraryScreen
import com.kitchenvision.pro.ui.theme.KitchenVisionTheme
import com.kitchenvision.pro.viewmodel.DesignViewModel

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            KitchenVisionTheme {
                Surface(modifier = Modifier.fillMaxSize()) {
                    val navController = rememberNavController()
                    val viewModel: DesignViewModel = viewModel()

                    NavHost(
                        navController = navController,
                        startDestination = "home"
                    ) {
                        composable("home") {
                            HomeScreen(
                                viewModel = viewModel,
                                onNewDesign = {
                                    viewModel.startNewProject()
                                    navController.navigate("design")
                                },
                                onOpenProject = { project ->
                                    viewModel.loadProject(project)
                                    navController.navigate("design")
                                }
                            )
                        }
                        composable("design") {
                            DesignCanvasScreen(
                                viewModel = viewModel,
                                onBack = { navController.popBackStack() },
                                onOpenLibrary = { navController.navigate("library") }
                            )
                        }
                        composable("library") {
                            LibraryScreen(
                                onSelectElement = { type, asset ->
                                    // Will be handled via shared ViewModel or callback
                                    navController.popBackStack()
                                },
                                onBack = { navController.popBackStack() }
                            )
                        }
                    }
                }
            }
        }
    }
}
