package com.example.ui

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.Input
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.engine.SecurityValidator
import com.example.model.AssetType
import com.example.model.SecurityValidationResult
import com.example.ui.theme.*

@Composable
fun SecurityGateScreen(
    viewModel: MainViewModel,
    modifier: Modifier = Modifier
) {
    val securityInput by viewModel.securityInput.collectAsState()
    val validationResult by viewModel.securityValidationResult.collectAsState()
    val blockedEvents by viewModel.blockedEvents.collectAsState()
    val testVectors = viewModel.securityTestVectors

    var showFeedIngestDialog by remember { mutableStateOf(false) }

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
                        text = "Security Gate & Spam Firewall",
                        style = MaterialTheme.typography.headlineSmall,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onBackground
                    )
                    Text(
                        text = "Real-time sanitization, Shannon entropy, Base64 filter & spam detection",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }

                FilledTonalButton(
                    onClick = { showFeedIngestDialog = true },
                    modifier = Modifier.testTag("open_feed_ingest_button"),
                    contentPadding = PaddingValues(horizontal = 12.dp, vertical = 6.dp)
                ) {
                    Icon(Icons.AutoMirrored.Filled.Input, contentDescription = null, modifier = Modifier.size(16.dp))
                    Spacer(modifier = Modifier.width(4.dp))
                    Text("Ingest Feed", fontSize = 12.sp)
                }
            }
        }

        // Live Test Console Card
        item {
            Card(
                shape = RoundedCornerShape(12.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                border = CardDefaults.outlinedCardBorder().copy(brush = androidx.compose.ui.graphics.SolidColor(CyberCardBorder))
            ) {
                Column(modifier = Modifier.padding(14.dp)) {
                    Text(
                        text = "Input Payload Validation Console",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                    Text(
                        text = "Enter any raw query, external social message, or simulated payload to test firewall filters:",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Spacer(modifier = Modifier.height(10.dp))

                    OutlinedTextField(
                        value = securityInput,
                        onValueChange = {
                            viewModel.setSecurityInput(it)
                            viewModel.evaluateSecurityInput()
                        },
                        modifier = Modifier
                            .fillMaxWidth()
                            .testTag("security_payload_input"),
                        placeholder = { Text("Paste payload, corrupted string, or spam message here...") },
                        maxLines = 4,
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = CyberCyan,
                            unfocusedBorderColor = MaterialTheme.colorScheme.outline
                        )
                    )

                    Spacer(modifier = Modifier.height(12.dp))

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = "Shannon Entropy: ${"%.2f".format(SecurityValidator.calculateShannonEntropy(securityInput))} bits",
                            style = MaterialTheme.typography.labelSmall,
                            fontFamily = FontFamily.Monospace,
                            color = CyberCyan
                        )

                        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                            if (securityInput.isNotEmpty()) {
                                OutlinedButton(
                                    onClick = { viewModel.setSecurityInput("") },
                                    contentPadding = PaddingValues(horizontal = 10.dp, vertical = 4.dp)
                                ) {
                                    Text("Clear", fontSize = 12.sp)
                                }
                            }
                            Button(
                                onClick = { viewModel.evaluateSecurityInput() },
                                modifier = Modifier.testTag("run_security_check_button"),
                                contentPadding = PaddingValues(horizontal = 14.dp, vertical = 4.dp)
                            ) {
                                Icon(Icons.Default.Security, contentDescription = null, modifier = Modifier.size(16.dp))
                                Spacer(modifier = Modifier.width(6.dp))
                                Text("Filter Check", fontSize = 12.sp)
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(14.dp))
                    Text(
                        text = "Pre-Configured Attack & Spam Test Vectors:",
                        style = MaterialTheme.typography.labelMedium,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                    Spacer(modifier = Modifier.height(6.dp))

                    LazyRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        items(testVectors) { vector ->
                            FilledTonalButton(
                                onClick = { viewModel.loadSecurityVector(vector) },
                                modifier = Modifier.testTag("vector_${vector.title.replace(' ', '_')}"),
                                contentPadding = PaddingValues(horizontal = 10.dp, vertical = 4.dp)
                            ) {
                                Text(vector.title, fontSize = 11.sp)
                            }
                        }
                    }
                }
            }
        }

        // Prominent Error Banner / Result Display
        item {
            val res = validationResult
            if (res != null) {
                when (res) {
                    is SecurityValidationResult.Invalid -> {
                        Card(
                            shape = RoundedCornerShape(12.dp),
                            colors = CardDefaults.cardColors(containerColor = CriticalRedBg),
                            border = CardDefaults.outlinedCardBorder().copy(brush = androidx.compose.ui.graphics.SolidColor(CyberRed)),
                            modifier = Modifier.testTag("security_error_banner")
                        ) {
                            Column(modifier = Modifier.padding(16.dp)) {
                                Row(
                                    verticalAlignment = Alignment.CenterVertically,
                                    modifier = Modifier.fillMaxWidth()
                                ) {
                                    Icon(
                                        imageVector = Icons.Default.Dangerous,
                                        contentDescription = "Security Error",
                                        tint = CyberRed,
                                        modifier = Modifier.size(28.dp)
                                    )
                                    Spacer(modifier = Modifier.width(10.dp))
                                    // MANDATORY ERROR MESSAGE REQUIRED BY SPECIFICATION
                                    Text(
                                        text = res.errorMessage,
                                        style = MaterialTheme.typography.titleMedium,
                                        fontWeight = FontWeight.ExtraBold,
                                        color = Color.White
                                    )
                                }

                                Spacer(modifier = Modifier.height(10.dp))
                                HorizontalDivider(color = CyberRed.copy(alpha = 0.4f))
                                Spacer(modifier = Modifier.height(10.dp))

                                Text(
                                    text = "VIOLATION TYPE: ${res.violationType}",
                                    style = MaterialTheme.typography.labelMedium,
                                    fontFamily = FontFamily.Monospace,
                                    fontWeight = FontWeight.Bold,
                                    color = CriticalRedText
                                )
                                Spacer(modifier = Modifier.height(4.dp))
                                Text(
                                    text = res.technicalDetails,
                                    style = MaterialTheme.typography.bodySmall,
                                    color = Color.White.copy(alpha = 0.9f)
                                )
                                Spacer(modifier = Modifier.height(6.dp))
                                Surface(
                                    shape = RoundedCornerShape(6.dp),
                                    color = Color.Black.copy(alpha = 0.4f),
                                    modifier = Modifier.fillMaxWidth()
                                ) {
                                    Text(
                                        text = "Blocked Payload: ${res.blockedPayloadPreview}",
                                        fontSize = 11.sp,
                                        fontFamily = FontFamily.Monospace,
                                        color = CriticalRedText,
                                        modifier = Modifier.padding(8.dp)
                                    )
                                }
                            }
                        }
                    }

                    is SecurityValidationResult.Valid -> {
                        Card(
                            shape = RoundedCornerShape(12.dp),
                            colors = CardDefaults.cardColors(containerColor = SafeGreenBg.copy(alpha = 0.3f)),
                            border = CardDefaults.outlinedCardBorder().copy(brush = androidx.compose.ui.graphics.SolidColor(SafeGreenText)),
                            modifier = Modifier.testTag("security_success_banner")
                        ) {
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(16.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Icon(
                                    imageVector = Icons.Default.CheckCircle,
                                    contentDescription = "Clean Payload",
                                    tint = SafeGreenText,
                                    modifier = Modifier.size(28.dp)
                                )
                                Spacer(modifier = Modifier.width(12.dp))
                                Column {
                                    Text(
                                        text = "Input Verification Passed",
                                        style = MaterialTheme.typography.titleSmall,
                                        fontWeight = FontWeight.Bold,
                                        color = SafeGreenText
                                    )
                                    Text(
                                        text = "Payload passed entropy checks, base64 rules, and spam filters. Safe for ingestion.",
                                        style = MaterialTheme.typography.bodySmall,
                                        color = MaterialTheme.colorScheme.onSurface
                                    )
                                }
                            }
                        }
                    }
                }
            }
        }

        // Blocked Quarantine Log
        item {
            Text(
                text = "Quarantined & Blocked Payloads Log (${blockedEvents.size})",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onBackground
            )
        }

        if (blockedEvents.isEmpty()) {
            item {
                Surface(
                    shape = RoundedCornerShape(10.dp),
                    color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text(
                        text = "No quarantined events yet. Click any test vector above to trigger firewall blocks.",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        modifier = Modifier.padding(16.dp)
                    )
                }
            }
        } else {
            items(blockedEvents) { event ->
                Card(
                    shape = RoundedCornerShape(10.dp),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.7f)),
                    border = CardDefaults.outlinedCardBorder().copy(brush = androidx.compose.ui.graphics.SolidColor(CyberCardBorder))
                ) {
                    Column(modifier = Modifier.padding(12.dp)) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = event.violationType,
                                fontWeight = FontWeight.Bold,
                                fontFamily = FontFamily.Monospace,
                                fontSize = 11.sp,
                                color = CyberRed
                            )
                            Text(
                                text = "BLOCKED",
                                fontSize = 10.sp,
                                fontWeight = FontWeight.Bold,
                                color = CyberRed
                            )
                        }
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(
                            text = event.technicalDetails,
                            fontSize = 11.sp,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(
                            text = event.payloadPreview,
                            fontFamily = FontFamily.Monospace,
                            fontSize = 11.sp,
                            color = CyberCyan
                        )
                    }
                }
            }
        }
    }

    // Feed Ingest Dialog
    if (showFeedIngestDialog) {
        var ingestHandle by remember { mutableStateOf("") }
        var ingestName by remember { mutableStateOf("") }
        var ingestBio by remember { mutableStateOf("") }
        var ingestPlatform by remember { mutableStateOf("X / Twitter") }
        var ingestType by remember { mutableStateOf(AssetType.SOCIAL_PROFILE) }
        var ingestError by remember { mutableStateOf<String?>(null) }

        AlertDialog(
            onDismissRequest = { showFeedIngestDialog = false },
            title = { Text("Ingest Simulated Monitored Feed") },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    Text(
                        text = "All fields are inspected by the Security Validator before entry into the monitoring database.",
                        fontSize = 12.sp,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )

                    if (ingestError != null) {
                        Surface(
                            shape = RoundedCornerShape(6.dp),
                            color = CriticalRedBg
                        ) {
                            Text(
                                text = ingestError!!,
                                color = CriticalRedText,
                                fontSize = 12.sp,
                                fontWeight = FontWeight.Bold,
                                modifier = Modifier.padding(8.dp)
                            )
                        }
                    }

                    OutlinedTextField(
                        value = ingestHandle,
                        onValueChange = { ingestHandle = it },
                        label = { Text("Handle or Package ID") },
                        modifier = Modifier.fillMaxWidth(),
                        singleLine = true
                    )
                    OutlinedTextField(
                        value = ingestName,
                        onValueChange = { ingestName = it },
                        label = { Text("Display Name") },
                        modifier = Modifier.fillMaxWidth(),
                        singleLine = true
                    )
                    OutlinedTextField(
                        value = ingestBio,
                        onValueChange = { ingestBio = it },
                        label = { Text("Bio or Publisher Description") },
                        modifier = Modifier.fillMaxWidth()
                    )
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        val res = viewModel.submitSecurityFeedIngest(
                            type = ingestType,
                            platformName = ingestPlatform,
                            handle = ingestHandle,
                            name = ingestName,
                            bio = ingestBio
                        )
                        if (res is SecurityValidationResult.Invalid) {
                            ingestError = res.errorMessage
                        } else {
                            showFeedIngestDialog = false
                        }
                    }
                ) {
                    Text("Ingest & Scan")
                }
            },
            dismissButton = {
                TextButton(onClick = { showFeedIngestDialog = false }) {
                    Text("Cancel")
                }
            }
        )
    }
}
