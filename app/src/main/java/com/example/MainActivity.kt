package com.example

import android.os.Bundle
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.viewModels
import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material.icons.outlined.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.example.ui.screens.*
import com.example.ui.theme.MyApplicationTheme
import com.example.viewmodel.AppNavTab
import com.example.viewmodel.ScaffoldViewModel
import kotlinx.coroutines.flow.collectLatest

class MainActivity : ComponentActivity() {

    private val viewModel: ScaffoldViewModel by viewModels()

    @OptIn(ExperimentalMaterial3Api::class)
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        setContent {
            MyApplicationTheme {
                val currentTab by viewModel.currentTab.collectAsState()
                val snackbarHostState = remember { SnackbarHostState() }
                val context = LocalContext.current

                LaunchedEffect(Unit) {
                    viewModel.toastMessage.collectLatest { msg ->
                        snackbarHostState.showSnackbar(msg)
                    }
                }

                Scaffold(
                    modifier = Modifier.fillMaxSize(),
                    topBar = {
                        TopAppBar(
                            title = {
                                Column {
                                    Text(
                                        text = "足場計算機",
                                        style = MaterialTheme.typography.titleMedium,
                                        fontWeight = FontWeight.ExtraBold
                                    )
                                    Text(
                                        text = "Scaffolding Calculator & BOM",
                                        style = MaterialTheme.typography.labelSmall,
                                        color = MaterialTheme.colorScheme.onSurfaceVariant
                                    )
                                }
                            },
                            actions = {
                                IconButton(
                                    onClick = {
                                        val report = viewModel.generateShareReport()
                                        if (report.isNotBlank()) {
                                            val sendIntent = android.content.Intent().apply {
                                                action = android.content.Intent.ACTION_SEND
                                                putExtra(android.content.Intent.EXTRA_TEXT, report)
                                                type = "text/plain"
                                            }
                                            context.startActivity(android.content.Intent.createChooser(sendIntent, "足場計算レポートを共有"))
                                        } else {
                                            Toast.makeText(context, "レポートデータがありません", Toast.LENGTH_SHORT).show()
                                        }
                                    },
                                    modifier = Modifier.testTag("top_bar_share_btn")
                                ) {
                                    Icon(Icons.Default.Share, contentDescription = "共有")
                                }
                            },
                            colors = TopAppBarDefaults.topAppBarColors(
                                containerColor = MaterialTheme.colorScheme.surface
                            )
                        )
                    },
                    bottomBar = {
                        NavigationBar(
                            containerColor = MaterialTheme.colorScheme.surface,
                            tonalElevation = 6.dp
                        ) {
                            NavigationBarItem(
                                selected = currentTab == AppNavTab.PLAN,
                                onClick = { viewModel.setNavTab(AppNavTab.PLAN) },
                                icon = {
                                    Icon(
                                        if (currentTab == AppNavTab.PLAN) Icons.Filled.ViewQuilt else Icons.Outlined.ViewQuilt,
                                        contentDescription = "平面割付"
                                    )
                                },
                                label = { Text("平面割付", style = MaterialTheme.typography.labelSmall) },
                                modifier = Modifier.testTag("tab_plan")
                            )

                            NavigationBarItem(
                                selected = currentTab == AppNavTab.SECTION,
                                onClick = { viewModel.setNavTab(AppNavTab.SECTION) },
                                icon = {
                                    Icon(
                                        if (currentTab == AppNavTab.SECTION) Icons.Filled.Layers else Icons.Outlined.Layers,
                                        contentDescription = "断面割付"
                                    )
                                },
                                label = { Text("断面割付", style = MaterialTheme.typography.labelSmall) },
                                modifier = Modifier.testTag("tab_section")
                            )

                            NavigationBarItem(
                                selected = currentTab == AppNavTab.BUILDING,
                                onClick = { viewModel.setNavTab(AppNavTab.BUILDING) },
                                icon = {
                                    Icon(
                                        if (currentTab == AppNavTab.BUILDING) Icons.Filled.Calculate else Icons.Outlined.Calculate,
                                        contentDescription = "積算見積"
                                    )
                                },
                                label = { Text("4面積算", style = MaterialTheme.typography.labelSmall) },
                                modifier = Modifier.testTag("tab_building")
                            )

                            NavigationBarItem(
                                selected = currentTab == AppNavTab.TOOLBOX,
                                onClick = { viewModel.setNavTab(AppNavTab.TOOLBOX) },
                                icon = {
                                    Icon(
                                        if (currentTab == AppNavTab.TOOLBOX) Icons.Filled.Construction else Icons.Outlined.Construction,
                                        contentDescription = "現場ツール"
                                    )
                                },
                                label = { Text("現場ツール", style = MaterialTheme.typography.labelSmall) },
                                modifier = Modifier.testTag("tab_toolbox")
                            )

                            NavigationBarItem(
                                selected = currentTab == AppNavTab.PROJECTS,
                                onClick = { viewModel.setNavTab(AppNavTab.PROJECTS) },
                                icon = {
                                    Icon(
                                        if (currentTab == AppNavTab.PROJECTS) Icons.Filled.Folder else Icons.Outlined.Folder,
                                        contentDescription = "保存案件"
                                    )
                                },
                                label = { Text("保存案件", style = MaterialTheme.typography.labelSmall) },
                                modifier = Modifier.testTag("tab_projects")
                            )
                        }
                    },
                    snackbarHost = { SnackbarHost(snackbarHostState) }
                ) { innerPadding ->
                    Box(modifier = Modifier.padding(innerPadding)) {
                        when (currentTab) {
                            AppNavTab.PLAN -> PlanLayoutScreen(viewModel = viewModel)
                            AppNavTab.SECTION -> SectionLayoutScreen(viewModel = viewModel)
                            AppNavTab.BUILDING -> BuildingBOMScreen(viewModel = viewModel)
                            AppNavTab.TOOLBOX -> ToolboxScreen(viewModel = viewModel)
                            AppNavTab.PROJECTS -> ProjectsScreen(viewModel = viewModel)
                        }
                    }
                }
            }
        }
    }
}
