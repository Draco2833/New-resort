package com.example.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowRight
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.model.ThreatSeverity
import com.example.ui.theme.*

@Composable
fun LookalikeTesterScreen(
    viewModel: MainViewModel,
    modifier: Modifier = Modifier
) {
    val currentProfile by viewModel.currentProfile.collectAsState()
    val candidateInput by viewModel.lookalikeCandidateInput.collectAsState()
    val result by viewModel.lookalikeResult.collectAsState()

    val sampleCandidates = listOf(
        "@Apex_Financia1_Support" to "Homoglyph '1' + Suffix",
        "@0fficial_ApexFinancial" to "Homoglyph '0' + Prefix",
        "@Ap3x_Financial" to "Homoglyph '3' for 'e'",
        "com.apexbanking.pro" to "Typosquatted App Package",
        "@ApexFinancial" to "Exact Brand (Official)",
        "@ApexLegendsGaming" to "Benign Non-Target"
    )

    LazyColumn(
        modifier = modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        item {
            Text(
                text = "Look-alike Name Detection Lab",
                style = MaterialTheme.typography.headlineSmall,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onBackground
            )
            Text(
                text = "Test homoglyphs, confusable swaps, affix squatting, and over-flagging prevention against '${currentProfile.name}'",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }

        // Candidate Input Field
        item {
            Card(
                shape = RoundedCornerShape(12.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
            ) {
                Column(modifier = Modifier.padding(14.dp)) {
                    Text(
                        text = "Candidate Name / Handle to Probe",
                        style = MaterialTheme.typography.labelLarge,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    OutlinedTextField(
                        value = candidateInput,
                        onValueChange = { viewModel.setLookalikeCandidateInput(it) },
                        modifier = Modifier
                            .fillMaxWidth()
                            .testTag("lookalike_test_input"),
                        singleLine = true,
                        placeholder = { Text("e.g. @Apex_Financia1_Support") },
                        trailingIcon = {
                            if (candidateInput.isNotEmpty()) {
                                IconButton(onClick = { viewModel.setLookalikeCandidateInput("") }) {
                                    Icon(Icons.Default.Clear, contentDescription = "Clear")
                                }
                            }
                        },
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = CyberCyan,
                            unfocusedBorderColor = MaterialTheme.colorScheme.outline
                        )
                    )

                    Spacer(modifier = Modifier.height(10.dp))
                    Text(
                        text = "Quick Test Vectors:",
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Spacer(modifier = Modifier.height(6.dp))
                    LazyRow(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                        items(sampleCandidates) { (candidate, desc) ->
                            FilledTonalButton(
                                onClick = { viewModel.setLookalikeCandidateInput(candidate) },
                                contentPadding = PaddingValues(horizontal = 8.dp, vertical = 2.dp),
                                modifier = Modifier.testTag("sample_candidate_${candidate.replace(Regex("[^a-zA-Z0-9]"), "_")}")
                            ) {
                                Text(candidate, fontSize = 11.sp, fontFamily = FontFamily.Monospace)
                            }
                        }
                    }
                }
            }
        }

        // Analysis Results Card
        item {
            val res = result
            if (res != null) {
                val isThreat = res.isLookalike
                val riskColor = when {
                    res.riskScore >= 70 -> CyberRed
                    res.riskScore >= 40 -> CyberAmber
                    else -> SafeGreenText
                }

                Card(
                    shape = RoundedCornerShape(12.dp),
                    colors = CardDefaults.cardColors(
                        containerColor = if (isThreat) MaterialTheme.colorScheme.surface else SafeGreenBg.copy(alpha = 0.2f)
                    ),
                    border = CardDefaults.outlinedCardBorder().copy(brush = androidx.compose.ui.graphics.SolidColor(riskColor))
                ) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        // Verdict Header
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Icon(
                                    imageVector = if (isThreat) Icons.Default.Warning else Icons.Default.CheckCircle,
                                    contentDescription = null,
                                    tint = riskColor,
                                    modifier = Modifier.size(24.dp)
                                )
                                Spacer(modifier = Modifier.width(8.dp))
                                Text(
                                    text = if (isThreat) "MALICIOUS LOOK-ALIKE FLAGGED" else "BENIGN / NON-TARGET",
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 14.sp,
                                    color = riskColor
                                )
                            }

                            Surface(
                                shape = RoundedCornerShape(6.dp),
                                color = riskColor.copy(alpha = 0.15f)
                            ) {
                                Text(
                                    text = "${res.riskScore}% RISK SCORE",
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 12.sp,
                                    color = riskColor,
                                    modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                                )
                            }
                        }

                        Spacer(modifier = Modifier.height(14.dp))

                        // Technical Breakdown Table
                        Column(
                            verticalArrangement = Arrangement.spacedBy(8.dp),
                            modifier = Modifier
                                .fillMaxWidth()
                                .background(MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f), RoundedCornerShape(8.dp))
                                .padding(12.dp)
                        ) {
                            AnalysisRow(
                                label = "Canonical Normalized Form",
                                value = res.canonicalCandidate,
                                isMonospace = true,
                                highlightColor = CyberCyan
                            )
                            AnalysisRow(
                                label = "Target Brand Stem",
                                value = res.brandStem,
                                isMonospace = true
                            )
                            AnalysisRow(
                                label = "Damerau-Levenshtein Edit Distance",
                                value = "${res.editDistance} operations",
                                isMonospace = false
                            )
                            AnalysisRow(
                                label = "Over-flagging Protection",
                                value = if (res.overflaggingProtectionTriggered) "TRIGGERED (Safe Guard Active)" else "PASSED (Evaluated as Match)",
                                isMonospace = false,
                                highlightColor = if (res.overflaggingProtectionTriggered) SafeGreenText else MaterialTheme.colorScheme.onSurface
                            )
                        }

                        // Homoglyphs Detected
                        if (res.detectedHomoglyphs.isNotEmpty()) {
                            Spacer(modifier = Modifier.height(14.dp))
                            Text(
                                text = "Detected Homoglyph / Confusable Swaps (${res.detectedHomoglyphs.size}):",
                                style = MaterialTheme.typography.labelMedium,
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.onSurface
                            )
                            Spacer(modifier = Modifier.height(6.dp))
                            Row(
                                horizontalArrangement = Arrangement.spacedBy(6.dp),
                                modifier = Modifier.fillMaxWidth()
                            ) {
                                res.detectedHomoglyphs.forEach { (swap, target) ->
                                    Surface(
                                        shape = RoundedCornerShape(6.dp),
                                        color = CriticalRedBg,
                                        border = CardDefaults.outlinedCardBorder().copy(brush = androidx.compose.ui.graphics.SolidColor(CyberRed))
                                    ) {
                                        Text(
                                            text = "'$swap' -> '$target'",
                                            fontSize = 12.sp,
                                            fontFamily = FontFamily.Monospace,
                                            fontWeight = FontWeight.Bold,
                                            color = CriticalRedText,
                                            modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                                        )
                                    }
                                }
                            }
                        }

                        // Affixes Detected
                        if (res.detectedAffixes.isNotEmpty()) {
                            Spacer(modifier = Modifier.height(12.dp))
                            Text(
                                text = "Abused Keywords & Affixes:",
                                style = MaterialTheme.typography.labelMedium,
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.onSurface
                            )
                            Spacer(modifier = Modifier.height(6.dp))
                            Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                                res.detectedAffixes.forEach { affix ->
                                    Surface(
                                        shape = RoundedCornerShape(6.dp),
                                        color = HighOrangeBg
                                    ) {
                                        Text(
                                            text = "+$affix",
                                            fontSize = 11.sp,
                                            fontFamily = FontFamily.Monospace,
                                            color = HighOrangeText,
                                            modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                                        )
                                    }
                                }
                            }
                        }

                        // Engine Reasoning List
                        if (res.detectionReasons.isNotEmpty()) {
                            Spacer(modifier = Modifier.height(14.dp))
                            Text(
                                text = "Detection Engine Rule Logs:",
                                style = MaterialTheme.typography.labelMedium,
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.onSurface
                            )
                            Spacer(modifier = Modifier.height(6.dp))
                            res.detectionReasons.forEach { reason ->
                                Row(
                                    verticalAlignment = Alignment.CenterVertically,
                                    modifier = Modifier.padding(vertical = 2.dp)
                                ) {
                                    Icon(Icons.AutoMirrored.Filled.ArrowRight, contentDescription = null, tint = CyberCyan, modifier = Modifier.size(16.dp))
                                    Text(
                                        text = reason,
                                        style = MaterialTheme.typography.bodySmall,
                                        color = MaterialTheme.colorScheme.onSurfaceVariant
                                    )
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun AnalysisRow(
    label: String,
    value: String,
    isMonospace: Boolean = false,
    highlightColor: androidx.compose.ui.graphics.Color? = null
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(
            text = label,
            fontSize = 12.sp,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
        Text(
            text = value,
            fontSize = 12.sp,
            fontFamily = if (isMonospace) FontFamily.Monospace else FontFamily.Default,
            fontWeight = FontWeight.Bold,
            color = highlightColor ?: MaterialTheme.colorScheme.onSurface
        )
    }
}
