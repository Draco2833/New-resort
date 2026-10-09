package com.example.ui

import android.content.Context
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.model.InterceptedNotification
import com.example.service.NotificationShieldManager
import com.example.ui.theme.*

@Composable
fun NotificationShieldScreen(
    viewModel: MainViewModel,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    val currentProfile by viewModel.currentProfile.collectAsState()
    val notifications by NotificationShieldManager.interceptedNotifications.collectAsState()
    val isServiceConnected by NotificationShieldManager.isServiceActive.collectAsState()

    var isPermissionGranted by remember {
        mutableStateOf(NotificationShieldManager.isNotificationAccessGranted(context))
    }

    // Refresh permission check when composable recomposes
    LaunchedEffect(Unit) {
        isPermissionGranted = NotificationShieldManager.isNotificationAccessGranted(context)
    }

    var showSimulateDialog by remember { mutableStateOf(false) }

    LazyColumn(
        modifier = modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        // Header
        item {
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Column {
                    Text(
                        text = "Mobile Notification Shield",
                        style = MaterialTheme.typography.headlineSmall,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onBackground
                    )
                    Text(
                        text = "Real-time on-device inspection for brand impersonation & smishing",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }

                FilledTonalButton(
                    onClick = { showSimulateDialog = true },
                    modifier = Modifier.testTag("simulate_notif_button"),
                    contentPadding = PaddingValues(horizontal = 12.dp, vertical = 6.dp)
                ) {
                    Icon(Icons.Default.AddAlert, contentDescription = null, modifier = Modifier.size(16.dp))
                    Spacer(modifier = Modifier.width(4.dp))
                    Text("Inject Test", fontSize = 12.sp)
                }
            }
        }

        // Permission & Listener Status Card
        item {
            val statusColor = if (isPermissionGranted) SafeGreenText else CyberAmber
            val statusBg = if (isPermissionGranted) SafeGreenBg.copy(alpha = 0.25f) else MedAmberBg.copy(alpha = 0.25f)

            Card(
                shape = RoundedCornerShape(12.dp),
                colors = CardDefaults.cardColors(containerColor = statusBg),
                border = CardDefaults.outlinedCardBorder().copy(brush = androidx.compose.ui.graphics.SolidColor(statusColor))
            ) {
                Column(modifier = Modifier.padding(14.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(
                                imageVector = if (isPermissionGranted) Icons.Default.CheckCircle else Icons.Default.NotificationsActive,
                                contentDescription = null,
                                tint = statusColor,
                                modifier = Modifier.size(24.dp)
                            )
                            Spacer(modifier = Modifier.width(10.dp))
                            Column {
                                Text(
                                    text = if (isPermissionGranted) "Notification Access Granted" else "Notification Access Required",
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 14.sp,
                                    color = MaterialTheme.colorScheme.onSurface
                                )
                                Text(
                                    text = if (isPermissionGranted) "Active listener analyzing incoming apps on-device" else "Enable permission in Android Settings to monitor live device alerts",
                                    fontSize = 11.sp,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }
                        }

                        Surface(
                            shape = RoundedCornerShape(4.dp),
                            color = if (isPermissionGranted) SafeGreenBg else MedAmberBg
                        ) {
                            Text(
                                text = if (isPermissionGranted) "PROTECTED" else "ACTION NEEDED",
                                fontSize = 10.sp,
                                fontWeight = FontWeight.Bold,
                                color = statusColor,
                                modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                            )
                        }
                    }

                    if (!isPermissionGranted) {
                        Spacer(modifier = Modifier.height(10.dp))
                        Button(
                            onClick = {
                                try {
                                    context.startActivity(NotificationShieldManager.getSettingsIntent())
                                } catch (e: Exception) {
                                    // Fallback
                                }
                            },
                            modifier = Modifier
                                .fillMaxWidth()
                                .testTag("grant_notification_permission_button"),
                            colors = ButtonDefaults.buttonColors(containerColor = CyberCyan, contentColor = CyberNavy)
                        ) {
                            Icon(Icons.Default.Settings, contentDescription = null, modifier = Modifier.size(16.dp))
                            Spacer(modifier = Modifier.width(6.dp))
                            Text("Open Android Notification Access Settings", fontSize = 12.sp, fontWeight = FontWeight.Bold)
                        }
                    }
                }
            }
        }

        // Quick Simulation Presets Row
        item {
            Card(
                shape = RoundedCornerShape(12.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                border = CardDefaults.outlinedCardBorder().copy(brush = androidx.compose.ui.graphics.SolidColor(CyberCardBorder))
            ) {
                Column(modifier = Modifier.padding(14.dp)) {
                    Text(
                        text = "Simulate Notification Attack Scenarios:",
                        style = MaterialTheme.typography.labelLarge,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                    Text(
                        text = "Trigger on-device heuristic inspection without needing third-party apps installed:",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Spacer(modifier = Modifier.height(10.dp))

                    LazyRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        item {
                            FilledTonalButton(
                                onClick = {
                                    NotificationShieldManager.inspectAndLog(
                                        packageName = "com.google.android.apps.messaging",
                                        appName = "Messages",
                                        title = "Urgent: ${currentProfile.name} Security Alert",
                                        bodyText = "Your account transfer is paused. Verify immediately at bit.ly/apex-claims-portal",
                                        activeBrand = currentProfile
                                    )
                                },
                                contentPadding = PaddingValues(horizontal = 10.dp, vertical = 4.dp),
                                modifier = Modifier.testTag("inject_smishing_button")
                            ) {
                                Icon(Icons.Default.SmsFailed, contentDescription = null, modifier = Modifier.size(14.dp), tint = CyberRed)
                                Spacer(modifier = Modifier.width(4.dp))
                                Text("Simulate Smishing SMS", fontSize = 11.sp)
                            }
                        }

                        item {
                            FilledTonalButton(
                                onClick = {
                                    NotificationShieldManager.inspectAndLog(
                                        packageName = "com.whatsapp",
                                        appName = "WhatsApp",
                                        title = "${currentProfile.name} Rewards Bot",
                                        bodyText = "Congratulations! Connect wallet to claim 500 APEX tokens immediately.",
                                        activeBrand = currentProfile
                                    )
                                },
                                contentPadding = PaddingValues(horizontal = 10.dp, vertical = 4.dp),
                                modifier = Modifier.testTag("inject_crypto_scam_button")
                            ) {
                                Icon(Icons.Default.CurrencyBitcoin, contentDescription = null, modifier = Modifier.size(14.dp), tint = CyberAmber)
                                Spacer(modifier = Modifier.width(4.dp))
                                Text("Simulate Crypto Scam", fontSize = 11.sp)
                            }
                        }

                        item {
                            FilledTonalButton(
                                onClick = {
                                    NotificationShieldManager.inspectAndLog(
                                        packageName = "com.apex.mobilebanking",
                                        appName = "Apex Mobile Banking",
                                        title = "Apex Financial Account Notice",
                                        bodyText = "Your periodic e-statement is ready to view in the official mobile app.",
                                        activeBrand = currentProfile
                                    )
                                },
                                contentPadding = PaddingValues(horizontal = 10.dp, vertical = 4.dp),
                                modifier = Modifier.testTag("inject_official_button")
                            ) {
                                Icon(Icons.Default.VerifiedUser, contentDescription = null, modifier = Modifier.size(14.dp), tint = SafeGreenText)
                                Spacer(modifier = Modifier.width(4.dp))
                                Text("Official App (Whitelisted)", fontSize = 11.sp)
                            }
                        }
                    }
                }
            }
        }

        // Live Log Header
        item {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "Inspected Mobile Notifications (${notifications.size})",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onBackground
                )

                if (notifications.isNotEmpty()) {
                    TextButton(onClick = { NotificationShieldManager.clearLog() }) {
                        Text("Clear Log", fontSize = 11.sp, color = TextMuted)
                    }
                }
            }
        }

        // Notification Log Items
        if (notifications.isEmpty()) {
            item {
                Surface(
                    shape = RoundedCornerShape(10.dp),
                    color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text(
                        text = "No notifications inspected yet. Click an attack scenario above or grant system permission to analyze live incoming notifications.",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        modifier = Modifier.padding(16.dp)
                    )
                }
            }
        } else {
            items(notifications, key = { it.id }) { item ->
                NotificationItemCard(item)
            }
        }
    }

    // Custom Injection Dialog
    if (showSimulateDialog) {
        var pkg by remember { mutableStateOf("com.google.android.apps.messaging") }
        var app by remember { mutableStateOf("SMS App") }
        var title by remember { mutableStateOf("Apex_Financia1 Support") }
        var body by remember { mutableStateOf("DM us on Telegram to unlock your suspended bank account.") }

        AlertDialog(
            onDismissRequest = { showSimulateDialog = false },
            title = { Text("Simulate Mobile Notification Payload") },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    Text("Inject a mock incoming notification to evaluate against DRP heuristics:", fontSize = 12.sp)
                    OutlinedTextField(
                        value = pkg,
                        onValueChange = { pkg = it },
                        label = { Text("Source Package ID") },
                        modifier = Modifier.fillMaxWidth(),
                        singleLine = true
                    )
                    OutlinedTextField(
                        value = title,
                        onValueChange = { title = it },
                        label = { Text("Notification Title") },
                        modifier = Modifier.fillMaxWidth(),
                        singleLine = true
                    )
                    OutlinedTextField(
                        value = body,
                        onValueChange = { body = it },
                        label = { Text("Notification Content / Text") },
                        modifier = Modifier.fillMaxWidth()
                    )
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        NotificationShieldManager.inspectAndLog(
                            packageName = pkg,
                            appName = app,
                            title = title,
                            bodyText = body,
                            activeBrand = currentProfile
                        )
                        showSimulateDialog = false
                    }
                ) {
                    Text("Analyze Payload")
                }
            },
            dismissButton = {
                TextButton(onClick = { showSimulateDialog = false }) {
                    Text("Cancel")
                }
            }
        )
    }
}

@Composable
fun NotificationItemCard(item: InterceptedNotification) {
    val borderColor = when {
        item.isOfficialAsset -> SafeGreenText
        item.isThreat -> CyberRed
        else -> MaterialTheme.colorScheme.outline.copy(alpha = 0.5f)
    }

    Card(
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(
            containerColor = if (item.isOfficialAsset) SafeGreenBg.copy(alpha = 0.15f) else MaterialTheme.colorScheme.surface
        ),
        border = CardDefaults.outlinedCardBorder().copy(brush = androidx.compose.ui.graphics.SolidColor(borderColor)),
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(modifier = Modifier.padding(12.dp)) {
            // Header Row: App name & Badge
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        imageVector = if (item.isOfficialAsset) Icons.Default.Verified else if (item.isThreat) Icons.Default.Warning else Icons.Default.Notifications,
                        contentDescription = null,
                        tint = if (item.isOfficialAsset) SafeGreenText else if (item.isThreat) CyberRed else CyberCyan,
                        modifier = Modifier.size(16.dp)
                    )
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(
                        text = "${item.appName} (${item.packageName})",
                        fontSize = 11.sp,
                        fontFamily = FontFamily.Monospace,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }

                Surface(
                    shape = RoundedCornerShape(4.dp),
                    color = if (item.isOfficialAsset) SafeGreenBg else if (item.isThreat) CriticalRedBg else MaterialTheme.colorScheme.surfaceVariant
                ) {
                    Text(
                        text = if (item.isOfficialAsset) "OFFICIAL EXCLUDED (0% RISK)" else if (item.isThreat) "${item.threatCategory} • ${item.riskScore}% RISK" else "BENIGN",
                        fontSize = 9.sp,
                        fontWeight = FontWeight.Bold,
                        color = if (item.isOfficialAsset) SafeGreenText else if (item.isThreat) CriticalRedText else TextMuted,
                        modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                    )
                }
            }

            Spacer(modifier = Modifier.height(6.dp))

            // Notification Title
            Text(
                text = item.title,
                fontWeight = FontWeight.Bold,
                fontSize = 13.sp,
                color = MaterialTheme.colorScheme.onSurface
            )

            // Notification Body
            Text(
                text = item.bodyText,
                fontSize = 12.sp,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )

            // Threat Evidence Tag
            if (item.evidence.isNotBlank()) {
                Spacer(modifier = Modifier.height(6.dp))
                Surface(
                    shape = RoundedCornerShape(6.dp),
                    color = if (item.isOfficialAsset) SafeGreenBg.copy(alpha = 0.3f) else MaterialTheme.colorScheme.surfaceVariant,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text(
                        text = "Forensic Evidence: ${item.evidence}",
                        fontSize = 10.sp,
                        color = if (item.isOfficialAsset) SafeGreenText else if (item.isThreat) CyberCyan else TextMuted,
                        modifier = Modifier.padding(6.dp)
                    )
                }
            }
        }
    }
}
