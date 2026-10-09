package com.example

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.BackHandler
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.viewModels
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.Assignment
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ui.*
import com.example.ui.theme.CyberBlue
import com.example.ui.theme.CyberCyan
import com.example.ui.theme.MyApplicationTheme

class MainActivity : ComponentActivity() {

    private val viewModel: MainViewModel by viewModels()

    @OptIn(ExperimentalMaterial3Api::class)
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            MyApplicationTheme {
                val currentNav by viewModel.currentNav.collectAsState()
                val selectedThreat by viewModel.selectedThreat.collectAsState()
                val currentProfile by viewModel.currentProfile.collectAsState()

                var showChallengeBrief by remember { mutableStateOf(false) }
                var showPresentationSlides by remember { mutableStateOf(false) }

                BackHandler(enabled = currentNav != AppNavDestination.DASHBOARD) {
                    viewModel.setNav(AppNavDestination.DASHBOARD)
                }

                Scaffold(
                    modifier = Modifier.fillMaxSize(),
                    topBar = {
                        TopAppBar(
                            title = {
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Icon(
                                        imageVector = Icons.Default.Shield,
                                        contentDescription = "Sentinel DRP",
                                        tint = CyberCyan,
                                        modifier = Modifier.size(24.dp)
                                    )
                                    Spacer(modifier = Modifier.width(10.dp))
                                    Column {
                                        Text(
                                            text = "Sentinel DRP",
                                            fontWeight = FontWeight.Bold,
                                            fontSize = 18.sp
                                        )
                                        Text(
                                            text = "Digital Risk Protection Platform",
                                            fontSize = 11.sp,
                                            color = MaterialTheme.colorScheme.onSurfaceVariant
                                        )
                                    }
                                }
                            },
                            actions = {
                                IconButton(
                                    onClick = { showChallengeBrief = true },
                                    modifier = Modifier.testTag("open_brief_button")
                                ) {
                                    Icon(
                                        imageVector = Icons.AutoMirrored.Filled.Assignment,
                                        contentDescription = "Challenge Brief & Required Points",
                                        tint = CyberCyan
                                    )
                                }
                                IconButton(
                                    onClick = { showPresentationSlides = true },
                                    modifier = Modifier.testTag("open_slides_button")
                                ) {
                                    Icon(
                                        imageVector = Icons.Default.Slideshow,
                                        contentDescription = "Demo Pitch Deck & Slides",
                                        tint = CyberBlue
                                    )
                                }
                                IconButton(
                                    onClick = { viewModel.resetToDemoDefaults() },
                                    modifier = Modifier.testTag("reset_demo_button")
                                ) {
                                    Icon(
                                        imageVector = Icons.Default.RestartAlt,
                                        contentDescription = "Reset Demo Data",
                                        tint = MaterialTheme.colorScheme.onSurfaceVariant
                                    )
                                }
                            },
                            colors = TopAppBarDefaults.topAppBarColors(
                                containerColor = MaterialTheme.colorScheme.surface,
                                titleContentColor = MaterialTheme.colorScheme.onSurface
                            )
                        )
                    },
                    bottomBar = {
                        NavigationBar(
                            modifier = Modifier
                                .navigationBarsPadding()
                                .testTag("main_navigation_bar"),
                            containerColor = MaterialTheme.colorScheme.surface,
                            contentColor = MaterialTheme.colorScheme.onSurface
                        ) {
                            val items = listOf(
                                Triple(AppNavDestination.DASHBOARD, Icons.Default.Radar, "Threats"),
                                Triple(AppNavDestination.BRAND_PROFILE, Icons.Default.VerifiedUser, "Brand"),
                                Triple(AppNavDestination.NOTIFICATION_SHIELD, Icons.Default.NotificationsActive, "Notifs"),
                                Triple(AppNavDestination.LOOKALIKE_LAB, Icons.Default.FindReplace, "Look-alike"),
                                Triple(AppNavDestination.SECURITY_GATE, Icons.Default.LocalPolice, "Security"),
                                Triple(AppNavDestination.ARCHITECTURE, Icons.Default.AccountTree, "Arch")
                            )

                            items.forEach { (destination, icon, label) ->
                                val selected = currentNav == destination
                                NavigationBarItem(
                                    selected = selected,
                                    onClick = { viewModel.setNav(destination) },
                                    icon = {
                                        Icon(
                                            imageVector = icon,
                                            contentDescription = destination.label
                                        )
                                    },
                                    label = { Text(label, fontSize = 11.sp) },
                                    modifier = Modifier.testTag("nav_tab_${destination.name.lowercase()}"),
                                    colors = NavigationBarItemDefaults.colors(
                                        selectedIconColor = MaterialTheme.colorScheme.surface,
                                        selectedTextColor = CyberCyan,
                                        indicatorColor = CyberCyan
                                    )
                                )
                            }
                        }
                    }
                ) { innerPadding ->
                    Box(
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(innerPadding)
                    ) {
                        when (currentNav) {
                            AppNavDestination.DASHBOARD -> DashboardScreen(viewModel = viewModel)
                            AppNavDestination.BRAND_PROFILE -> BrandProfileScreen(viewModel = viewModel)
                            AppNavDestination.NOTIFICATION_SHIELD -> NotificationShieldScreen(viewModel = viewModel)
                            AppNavDestination.LOOKALIKE_LAB -> LookalikeTesterScreen(viewModel = viewModel)
                            AppNavDestination.SECURITY_GATE -> SecurityGateScreen(viewModel = viewModel)
                            AppNavDestination.ARCHITECTURE -> ArchitectureScreen()
                        }
                    }

                    // Threat Detail & Takedown Sheet
                    if (selectedThreat != null) {
                        ThreatDetailSheet(
                            asset = selectedThreat!!,
                            brandProfile = currentProfile,
                            onDismiss = { viewModel.selectThreat(null) },
                            onTriage = { status -> viewModel.updateTriage(selectedThreat!!.id, status) }
                        )
                    }

                    // Problem Statement & Required Points Sheet
                    if (showChallengeBrief) {
                        ChallengeBriefSheet(
                            onDismiss = { showChallengeBrief = false }
                        )
                    }

                    // Executive Pitch Deck / Presentation Slides Sheet
                    if (showPresentationSlides) {
                        PresentationSlidesSheet(
                            onDismiss = { showPresentationSlides = false }
                        )
                    }
                }
            }
        }
    }
}
