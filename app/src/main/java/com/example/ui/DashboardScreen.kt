package com.example.ui

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.model.*
import com.example.ui.theme.*

@Composable
fun DashboardScreen(
    viewModel: MainViewModel,
    modifier: Modifier = Modifier
) {
    val currentProfile by viewModel.currentProfile.collectAsState()
    val filteredAssets by viewModel.filteredAssets.collectAsState()
    val searchQuery by viewModel.searchQuery.collectAsState()
    val selectedFilter by viewModel.selectedFilter.collectAsState()

    val totalScanned by viewModel.totalScanned.collectAsState()
    val criticalCount by viewModel.criticalThreatsCount.collectAsState()
    val officialExcludedCount by viewModel.officialExcludedCount.collectAsState()
    val blockedCount by viewModel.blockedCount.collectAsState()

    var showScanDialog by remember { mutableStateOf(false) }

    Column(
        modifier = modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
    ) {
        // Top Brand Header Banner
        Surface(
            color = MaterialTheme.colorScheme.surfaceVariant,
            modifier = Modifier.fillMaxWidth()
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 12.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Box(
                        modifier = Modifier
                            .size(36.dp)
                            .clip(RoundedCornerShape(8.dp))
                            .background(CyberCyan.copy(alpha = 0.2f))
                            .border(1.dp, CyberCyan, RoundedCornerShape(8.dp)),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = Icons.Default.Shield,
                            contentDescription = "Active Protection",
                            tint = CyberCyan,
                            modifier = Modifier.size(20.dp)
                        )
                    }
                    Spacer(modifier = Modifier.width(12.dp))
                    Column {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Text(
                                text = currentProfile.name,
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.onSurface
                            )
                            Spacer(modifier = Modifier.width(6.dp))
                            Surface(
                                shape = RoundedCornerShape(4.dp),
                                color = CyberCyan.copy(alpha = 0.15f)
                            ) {
                                Text(
                                    text = "PROTECTED",
                                    fontSize = 10.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = CyberCyan,
                                    modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                                )
                            }
                        }
                        Text(
                            text = "Brand Monitoring Active • Engine v2.4",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }

                Row(horizontalArrangement = Arrangement.spacedBy(8.dp), verticalAlignment = Alignment.CenterVertically) {
                    Button(
                        onClick = { showScanDialog = true },
                        modifier = Modifier.testTag("scan_custom_asset_button"),
                        contentPadding = PaddingValues(horizontal = 12.dp, vertical = 6.dp),
                        colors = ButtonDefaults.buttonColors(containerColor = CyberCyan, contentColor = CyberNavy)
                    ) {
                        Icon(
                            imageVector = Icons.Default.AddModerator,
                            contentDescription = "Scan Custom Asset",
                            modifier = Modifier.size(16.dp)
                        )
                        Spacer(modifier = Modifier.width(4.dp))
                        Text("Scan Asset", fontSize = 12.sp, fontWeight = FontWeight.Bold)
                    }

                    FilledTonalButton(
                        onClick = { viewModel.repository.reScanWithProfile(currentProfile) },
                        modifier = Modifier.testTag("rescan_button"),
                        contentPadding = PaddingValues(horizontal = 12.dp, vertical = 6.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.Refresh,
                            contentDescription = "Scan",
                            modifier = Modifier.size(16.dp)
                        )
                        Spacer(modifier = Modifier.width(4.dp))
                        Text("Re-Scan", fontSize = 12.sp)
                    }
                }
            }
        }

        // Metrics Row
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 8.dp),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            MetricCard(
                title = "Total Scanned",
                count = totalScanned.toString(),
                icon = Icons.Default.Radar,
                accentColor = CyberBlue,
                modifier = Modifier.weight(1f)
            )
            MetricCard(
                title = "Critical Threats",
                count = criticalCount.toString(),
                icon = Icons.Default.Warning,
                accentColor = CyberRed,
                modifier = Modifier.weight(1f)
            )
            MetricCard(
                title = "Official Whitelisted",
                count = officialExcludedCount.toString(),
                icon = Icons.Default.VerifiedUser,
                accentColor = CyberGreen,
                modifier = Modifier.weight(1f)
            )
            MetricCard(
                title = "Blocked Payloads",
                count = blockedCount.toString(),
                icon = Icons.Default.Block,
                accentColor = CyberAmber,
                modifier = Modifier.weight(1f)
            )
        }

        // Search Field
        OutlinedTextField(
            value = searchQuery,
            onValueChange = { viewModel.setSearchQuery(it) },
            placeholder = { Text("Search by handle, app package, platform, or keyword...") },
            leadingIcon = {
                Icon(Icons.Default.Search, contentDescription = "Search", tint = MaterialTheme.colorScheme.onSurfaceVariant)
            },
            trailingIcon = {
                if (searchQuery.isNotEmpty()) {
                    IconButton(onClick = { viewModel.setSearchQuery("") }) {
                        Icon(Icons.Default.Close, contentDescription = "Clear")
                    }
                }
            },
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 4.dp)
                .testTag("search_input_field"),
            singleLine = true,
            shape = RoundedCornerShape(12.dp),
            colors = OutlinedTextFieldDefaults.colors(
                focusedBorderColor = CyberCyan,
                unfocusedBorderColor = MaterialTheme.colorScheme.outline
            )
        )

        // Filter Chips Row
        LazyRow(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 4.dp),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            val filters = listOf(
                "ALL" to "All Entities",
                "THREATS_ONLY" to "Active Threats",
                "OFFICIAL_EXCLUDED" to "Official Excluded",
                "SOCIAL" to "Social Media",
                "APPS" to "Mobile Apps",
                "CRITICAL_ONLY" to "Critical Only"
            )

            items(filters) { (key, label) ->
                val isSelected = selectedFilter == key
                FilterChip(
                    selected = isSelected,
                    onClick = { viewModel.setSelectedFilter(key) },
                    label = { Text(label, fontSize = 12.sp) },
                    leadingIcon = if (isSelected) {
                        { Icon(Icons.Default.Check, contentDescription = null, modifier = Modifier.size(14.dp)) }
                    } else null,
                    colors = FilterChipDefaults.filterChipColors(
                        selectedContainerColor = CyberCyan.copy(alpha = 0.2f),
                        selectedLabelColor = CyberCyan
                    ),
                    modifier = Modifier.testTag("filter_$key")
                )
            }
        }

        // Monitored Items List
        if (filteredAssets.isEmpty()) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(32.dp),
                contentAlignment = Alignment.Center
            ) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Icon(
                        imageVector = Icons.Default.FilterListOff,
                        contentDescription = "No results",
                        tint = TextMuted,
                        modifier = Modifier.size(48.dp)
                    )
                    Spacer(modifier = Modifier.height(12.dp))
                    Text(
                        text = "No matching monitored assets found",
                        style = MaterialTheme.typography.titleMedium,
                        color = TextMuted
                    )
                    Text(
                        text = "Try adjusting your search query or active filter",
                        style = MaterialTheme.typography.bodySmall,
                        color = TextSubtle
                    )
                }
            }
        } else {
            LazyColumn(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(horizontal = 16.dp),
                verticalArrangement = Arrangement.spacedBy(10.dp),
                contentPadding = PaddingValues(vertical = 8.dp)
            ) {
                items(filteredAssets, key = { it.id }) { asset ->
                    AssetCard(
                        asset = asset,
                        onClick = { viewModel.selectThreat(asset) },
                        onTriageClick = { status -> viewModel.updateTriage(asset.id, status) }
                    )
                }
            }
        }
    }

    // Scan Custom Asset / Probe Impersonation Dialog
    if (showScanDialog) {
        var assetType by remember { mutableStateOf(AssetType.SOCIAL_PROFILE) }
        var platformName by remember { mutableStateOf("X / Twitter") }
        var titleOrHandle by remember { mutableStateOf("@${currentProfile.name.replace(" ", "")}_FakeSupport") }
        var displayName by remember { mutableStateOf("${currentProfile.name} Official Support") }
        var publisherOrBio by remember { mutableStateOf("Independent Helpdesk for ${currentProfile.name} users. DM for immediate assistance.") }
        var logoLikeness by remember { mutableStateOf(85f) }
        var errorMessage by remember { mutableStateOf<String?>(null) }
        var errorDetails by remember { mutableStateOf<String?>(null) }

        AlertDialog(
            onDismissRequest = { showScanDialog = false },
            title = {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        imageVector = Icons.Default.AddModerator,
                        contentDescription = null,
                        tint = CyberCyan,
                        modifier = Modifier.size(22.dp)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text("Probe Asset for Username & Logo Spoofing", fontSize = 16.sp, fontWeight = FontWeight.Bold)
                }
            },
            text = {
                LazyColumn(
                    modifier = Modifier.fillMaxWidth(),
                    verticalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    item {
                        Text(
                            text = "Add custom data to check if an external account or mobile application is impersonating '${currentProfile.name}' using the same username, logo likeness, or deceptive developer name.",
                            fontSize = 12.sp,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }

                    // Preset Quick-Fill Chips
                    item {
                        Text("Quick Test Presets:", fontSize = 11.sp, fontWeight = FontWeight.Bold, color = CyberCyan)
                        Spacer(modifier = Modifier.height(4.dp))
                        LazyRow(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                            item {
                                FilterChip(
                                    selected = false,
                                    onClick = {
                                        assetType = AssetType.SOCIAL_PROFILE
                                        platformName = "X / Twitter"
                                        titleOrHandle = "@${currentProfile.name.replace(" ", "")}"
                                        displayName = "${currentProfile.name} Verified"
                                        publisherOrBio = "Official accounts clone test with identical handle & logo"
                                        logoLikeness = 95f
                                        errorMessage = null
                                    },
                                    label = { Text("Same Username & Logo", fontSize = 11.sp) }
                                )
                            }
                            item {
                                FilterChip(
                                    selected = false,
                                    onClick = {
                                        assetType = AssetType.MOBILE_APP
                                        platformName = "Google Play Store"
                                        titleOrHandle = "com.unauthorized.${currentProfile.name.lowercase().replace(" ", "")}.wallet"
                                        displayName = "${currentProfile.name} Mobile Banking"
                                        publisherOrBio = "Rogue Apps Studio Ltd"
                                        logoLikeness = 90f
                                        errorMessage = null
                                    },
                                    label = { Text("Fake App (Cloned Logo)", fontSize = 11.sp) }
                                )
                            }
                            item {
                                FilterChip(
                                    selected = false,
                                    onClick = {
                                        val firstOfficial = currentProfile.officialSocialAccounts.firstOrNull()
                                        assetType = AssetType.SOCIAL_PROFILE
                                        platformName = firstOfficial?.platform?.displayName ?: "X / Twitter"
                                        titleOrHandle = firstOfficial?.handle ?: "@${currentProfile.name.replace(" ", "")}"
                                        displayName = currentProfile.name
                                        publisherOrBio = "Verified company account"
                                        logoLikeness = 100f
                                        errorMessage = null
                                    },
                                    label = { Text("Official Whitelisted Handle", fontSize = 11.sp) }
                                )
                            }
                        }
                    }

                    // Asset Type Selection
                    item {
                        Text("Asset Classification:", fontSize = 12.sp, fontWeight = FontWeight.Medium)
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            FilledTonalButton(
                                onClick = {
                                    assetType = AssetType.SOCIAL_PROFILE
                                    platformName = "X / Twitter"
                                },
                                modifier = Modifier.weight(1f),
                                colors = ButtonDefaults.filledTonalButtonColors(
                                    containerColor = if (assetType == AssetType.SOCIAL_PROFILE) CyberCyan.copy(alpha = 0.25f) else MaterialTheme.colorScheme.surfaceVariant,
                                    contentColor = if (assetType == AssetType.SOCIAL_PROFILE) CyberCyan else MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            ) {
                                Icon(Icons.Default.Share, contentDescription = null, modifier = Modifier.size(14.dp))
                                Spacer(modifier = Modifier.width(4.dp))
                                Text("Social Profile", fontSize = 11.sp)
                            }

                            FilledTonalButton(
                                onClick = {
                                    assetType = AssetType.MOBILE_APP
                                    platformName = "Google Play Store"
                                },
                                modifier = Modifier.weight(1f),
                                colors = ButtonDefaults.filledTonalButtonColors(
                                    containerColor = if (assetType == AssetType.MOBILE_APP) CyberCyan.copy(alpha = 0.25f) else MaterialTheme.colorScheme.surfaceVariant,
                                    contentColor = if (assetType == AssetType.MOBILE_APP) CyberCyan else MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            ) {
                                Icon(Icons.Default.Android, contentDescription = null, modifier = Modifier.size(14.dp))
                                Spacer(modifier = Modifier.width(4.dp))
                                Text("Mobile App", fontSize = 11.sp)
                            }
                        }
                    }

                    // Platform Name
                    item {
                        OutlinedTextField(
                            value = platformName,
                            onValueChange = { platformName = it },
                            label = { Text("Platform (e.g. Twitter, Instagram, Google Play)") },
                            modifier = Modifier.fillMaxWidth(),
                            singleLine = true
                        )
                    }

                    // Handle / Package Name
                    item {
                        OutlinedTextField(
                            value = titleOrHandle,
                            onValueChange = {
                                titleOrHandle = it
                                errorMessage = null
                            },
                            label = { Text(if (assetType == AssetType.SOCIAL_PROFILE) "Username / Handle (e.g. @MyBrand)" else "Package ID (e.g. com.brand.app)") },
                            modifier = Modifier.fillMaxWidth(),
                            singleLine = true,
                            colors = OutlinedTextFieldDefaults.colors(focusedBorderColor = CyberCyan)
                        )
                    }

                    // Display Name / App Title
                    item {
                        OutlinedTextField(
                            value = displayName,
                            onValueChange = {
                                displayName = it
                                errorMessage = null
                            },
                            label = { Text(if (assetType == AssetType.SOCIAL_PROFILE) "Display Name" else "App Title") },
                            modifier = Modifier.fillMaxWidth(),
                            singleLine = true
                        )
                    }

                    // Publisher / Bio
                    item {
                        OutlinedTextField(
                            value = publisherOrBio,
                            onValueChange = {
                                publisherOrBio = it
                                errorMessage = null
                            },
                            label = { Text(if (assetType == AssetType.SOCIAL_PROFILE) "Profile Bio" else "Publisher / Developer Name") },
                            modifier = Modifier.fillMaxWidth(),
                            maxLines = 2
                        )
                    }

                    // Logo Likeness Slider
                    item {
                        Column {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Text("Visual Logo Likeness:", fontSize = 12.sp, fontWeight = FontWeight.Medium)
                                Text(
                                    text = "${logoLikeness.toInt()}% Match",
                                    fontSize = 12.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = if (logoLikeness >= 75f) CyberRed else CyberCyan
                                )
                            }
                            Slider(
                                value = logoLikeness,
                                onValueChange = { logoLikeness = it },
                                valueRange = 0f..100f,
                                modifier = Modifier.fillMaxWidth()
                            )
                            Text(
                                text = if (logoLikeness >= 75f) "⚠️ Threshold met: Flags logo visual spoofing threat (+15 risk pts)" else "Below 75%: Not flagged as direct logo clone",
                                fontSize = 10.sp,
                                color = if (logoLikeness >= 75f) CyberAmber else MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }

                    // Error Banner if blocked
                    if (errorMessage != null) {
                        item {
                            Card(
                                colors = CardDefaults.cardColors(containerColor = CyberRed.copy(alpha = 0.2f)),
                                border = CardDefaults.outlinedCardBorder().copy(brush = androidx.compose.ui.graphics.SolidColor(CyberRed))
                            ) {
                                Column(modifier = Modifier.padding(10.dp)) {
                                    Text(
                                        text = errorMessage!!,
                                        color = CyberRed,
                                        fontWeight = FontWeight.Bold,
                                        fontSize = 12.sp
                                    )
                                    if (errorDetails != null) {
                                        Spacer(modifier = Modifier.height(2.dp))
                                        Text(
                                            text = errorDetails!!,
                                            color = MaterialTheme.colorScheme.onSurface,
                                            fontSize = 11.sp
                                        )
                                    }
                                }
                            }
                        }
                    }
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        val result = viewModel.submitCustomAssetScan(
                            type = assetType,
                            platformName = platformName.trim(),
                            handle = titleOrHandle.trim(),
                            name = displayName.trim(),
                            bio = publisherOrBio.trim(),
                            logoLikenessScore = logoLikeness.toInt()
                        )

                        if (result is SecurityValidationResult.Invalid) {
                            errorMessage = result.errorMessage
                            errorDetails = "${result.violationType} — ${result.technicalDetails}"
                        } else {
                            showScanDialog = false
                        }
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = CyberCyan, contentColor = CyberNavy)
                ) {
                    Text("Run Risk Scan", fontWeight = FontWeight.Bold)
                }
            },
            dismissButton = {
                TextButton(onClick = { showScanDialog = false }) {
                    Text("Cancel")
                }
            }
        )
    }
}

@Composable
fun MetricCard(
    title: String,
    count: String,
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    accentColor: Color,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier,
        shape = RoundedCornerShape(10.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.7f)
        ),
        border = CardDefaults.outlinedCardBorder().copy(brush = androidx.compose.ui.graphics.SolidColor(accentColor.copy(alpha = 0.3f)))
    ) {
        Column(
            modifier = Modifier.padding(10.dp),
            verticalArrangement = Arrangement.Center
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween,
                modifier = Modifier.fillMaxWidth()
            ) {
                Text(
                    text = title,
                    fontSize = 11.sp,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
                Icon(
                    imageVector = icon,
                    contentDescription = null,
                    tint = accentColor,
                    modifier = Modifier.size(14.dp)
                )
            }
            Spacer(modifier = Modifier.height(4.dp))
            Text(
                text = count,
                fontSize = 18.sp,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onSurface
            )
        }
    }
}

@Composable
fun AssetCard(
    asset: MonitoredAsset,
    onClick: () -> Unit,
    onTriageClick: (TriageStatus) -> Unit
) {
    val isOfficial = asset.isOfficialAsset
    val borderColor = when {
        isOfficial -> SafeGreenText.copy(alpha = 0.5f)
        asset.severity == ThreatSeverity.CRITICAL -> CyberRed
        asset.severity == ThreatSeverity.HIGH -> CyberAmber
        else -> MaterialTheme.colorScheme.outline.copy(alpha = 0.5f)
    }

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick)
            .testTag("asset_card_${asset.id}"),
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(
            containerColor = if (isOfficial) SafeGreenBg.copy(alpha = 0.15f) else MaterialTheme.colorScheme.surface
        ),
        border = CardDefaults.outlinedCardBorder().copy(brush = androidx.compose.ui.graphics.SolidColor(borderColor))
    ) {
        Column(modifier = Modifier.padding(14.dp)) {
            // Header Row: Platform & Severity Badge
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        imageVector = if (asset.type == AssetType.MOBILE_APP) Icons.Default.PhoneAndroid else Icons.Default.Share,
                        contentDescription = null,
                        tint = if (isOfficial) SafeGreenText else CyberBlue,
                        modifier = Modifier.size(16.dp)
                    )
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(
                        text = asset.platformName,
                        style = MaterialTheme.typography.labelMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = "• ${asset.followersOrDownloads}",
                        style = MaterialTheme.typography.labelSmall,
                        color = TextSubtle
                    )
                }

                // Status Chip
                if (isOfficial) {
                    Surface(
                        shape = RoundedCornerShape(4.dp),
                        color = SafeGreenBg
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                        ) {
                            Icon(Icons.Default.CheckCircle, contentDescription = null, tint = SafeGreenText, modifier = Modifier.size(12.dp))
                            Spacer(modifier = Modifier.width(4.dp))
                            Text(
                                text = "EXCLUDED (OFFICIAL)",
                                fontSize = 10.sp,
                                fontWeight = FontWeight.Bold,
                                color = SafeGreenText
                            )
                        }
                    }
                } else {
                    val badgeColor = when (asset.severity) {
                        ThreatSeverity.CRITICAL -> CriticalRedBg to CriticalRedText
                        ThreatSeverity.HIGH -> HighOrangeBg to HighOrangeText
                        ThreatSeverity.MEDIUM -> MedAmberBg to MedAmberText
                        else -> MaterialTheme.colorScheme.surfaceVariant to MaterialTheme.colorScheme.onSurfaceVariant
                    }
                    Surface(
                        shape = RoundedCornerShape(4.dp),
                        color = badgeColor.first
                    ) {
                        Text(
                            text = "${asset.severity.label} • ${asset.riskScore}% RISK",
                            fontSize = 10.sp,
                            fontWeight = FontWeight.Bold,
                            color = badgeColor.second,
                            modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(8.dp))

            // Title & Handle
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = asset.displayName,
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onSurface
                )
                if (isOfficial) {
                    Spacer(modifier = Modifier.width(6.dp))
                    Icon(
                        imageVector = Icons.Default.Verified,
                        contentDescription = "Verified Official",
                        tint = SafeGreenText,
                        modifier = Modifier.size(18.dp)
                    )
                }
            }

            Text(
                text = asset.titleOrHandle,
                style = MaterialTheme.typography.bodyMedium,
                fontFamily = FontFamily.Monospace,
                color = if (isOfficial) SafeGreenText else CyberCyan
            )

            Spacer(modifier = Modifier.height(6.dp))

            // Bio / Publisher
            Text(
                text = asset.publisherOrBio,
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                maxLines = 2,
                overflow = TextOverflow.Ellipsis
            )

            // Threat Reason Tags
            if (!isOfficial && asset.reasons.isNotEmpty()) {
                Spacer(modifier = Modifier.height(10.dp))
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    asset.reasons.take(2).forEach { reason ->
                        Surface(
                            shape = RoundedCornerShape(4.dp),
                            color = CyberNavySurface,
                            border = CardDefaults.outlinedCardBorder().copy(brush = androidx.compose.ui.graphics.SolidColor(CyberCardBorder))
                        ) {
                            Text(
                                text = reason.description,
                                fontSize = 11.sp,
                                color = MaterialTheme.colorScheme.onSurface,
                                modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp),
                                maxLines = 1,
                                overflow = TextOverflow.Ellipsis
                            )
                        }
                    }
                }
            }

            // Triage Status / Action Bar
            if (!isOfficial) {
                Spacer(modifier = Modifier.height(10.dp))
                HorizontalDivider(color = MaterialTheme.colorScheme.outline.copy(alpha = 0.2f))
                Spacer(modifier = Modifier.height(6.dp))
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    val triageLabel = when (asset.triageStatus) {
                        TriageStatus.TAKEDOWN_REQUESTED -> "Takedown Notice Sent" to CyberRed
                        TriageStatus.ESCALATED -> "Escalated to Legal" to CyberAmber
                        TriageStatus.MARKED_FALSE_POSITIVE -> "Cleared (False Positive)" to TextMuted
                        TriageStatus.UNREVIEWED -> "Pending Triage" to CyberCyan
                    }

                    Text(
                        text = "Status: ${triageLabel.first}",
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Medium,
                        color = triageLabel.second
                    )

                    Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                        TextButton(
                            onClick = { onTriageClick(TriageStatus.TAKEDOWN_REQUESTED) },
                            contentPadding = PaddingValues(horizontal = 8.dp, vertical = 2.dp),
                            modifier = Modifier.testTag("takedown_action_${asset.id}")
                        ) {
                            Text("Takedown", fontSize = 11.sp, color = CyberRed)
                        }
                        FilledTonalButton(
                            onClick = onClick,
                            contentPadding = PaddingValues(horizontal = 8.dp, vertical = 2.dp)
                        ) {
                            Text("Inspect", fontSize = 11.sp)
                        }
                    }
                }
            }
        }
    }
}
