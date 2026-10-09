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
