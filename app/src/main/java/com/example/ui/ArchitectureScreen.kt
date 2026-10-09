package com.example.ui

import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import android.widget.Toast
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ui.theme.*

data class ArchitectureStep(
    val stepNumber: String,
    val title: String,
    val role: String,
    val components: List<String>,
    val securityGuarantees: String,
    val icon: androidx.compose.ui.graphics.vector.ImageVector,
    val accentColor: Color
)

@Composable
fun ArchitectureScreen(
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    var showMermaidSource by remember { mutableStateOf(false) }

    val mermaidDiagramCode = remember {
        """
        flowchart TD
            subgraph Ingestion["1. Multi-Channel Ingestion Layer"]
                direction TB
                IN_SOC["Social Media Crawlers<br/>(X, LinkedIn, FB, IG, TikTok)"]
                IN_APP["App Store Feeds<br/>(Google Play, Apple App Store)"]
                IN_USER["Manual Query & Webhook Ingest"]
            end

            subgraph SecurityGate["2. Security & Input Sanitization Gate"]
                direction TB
                VAL["Security Validator"]
                F_ENTROPY{"Shannon Entropy Check<br/>(H > 4.65 bits)"}
                F_BASE64{"Base64 Abuse Filter<br/>(Payload Blobs)"}
                F_SPAM{"Spam Heuristics Regex<br/>(Crypto scam, Phishing)"}
                F_INJECT{"WAF Injection Filter<br/>(XSS, SQLi, JNDI)"}
                BLOCK["BLOCK PAYLOAD & RAISE UI ERROR:<br/>'Error: Invalid, corrupted, or spam input detected.'"]
            end

            subgraph BrandProfileWhitelist["3. Brand Profile & Whitelist Engine"]
                direction TB
                BP_CFG[("Brand Profile Config")]
                BP_NAMES["Brand Stems & Aliases"]
                BP_SOC["Official Social Handles/URLs"]
                BP_APPS["Official App Packages & Publishers"]
                EXCL_CHECK{"CRITICAL RULE:<br/>Matches Official Asset?"}
                EXCL_PASS["TAG: OFFICIAL_EXCLUDED<br/>Whitelisted & Never Flagged as Threat"]
            end

            subgraph DetectionEngine["4. Threat Detection & Analytics Engine"]
                direction TB
                DET_LOOKALIKE["Look-alike Name Detection<br/>(Damerau-Levenshtein Edit Dist)"]
                DET_HOMOGLYPH["Homoglyph & Confusable Mapper<br/>(0->o, 1->l, @->a, Cyrillic)"]
                DET_AFFIX["Affix Squatting Analyzer<br/>(+support, +claims, +official)"]
                DET_PUB["Publisher Discrepancy Engine<br/>(Mismatch vs Official Developer)"]
                DET_BADGE["Verification Spoofing Checker<br/>(Fake unicode checkmarks)"]
                SCORER["Risk Scoring Matrix<br/>(0 - 100 Risk Score)"]
            end

            subgraph StorageTriage["5. Storage, Triage & Incident Response"]
                direction TB
                DB[("Monitored Threat Database")]
                TRIAGE["Security Analyst Triage Console"]
                TAKEDOWN["Automated Legal Takedown Notice<br/>(DMCA / Trademark Notice)"]
                QUARANTINE[("Quarantine Blocked Log")]
            end

            %% Data Flow Links
            Ingestion --> VAL
            VAL --> F_ENTROPY & F_BASE64 & F_SPAM & F_INJECT
            F_ENTROPY & F_BASE64 & F_SPAM & F_INJECT -- Malicious / Corrupted / Spam --> BLOCK
            BLOCK --> QUARANTINE

            F_ENTROPY & F_BASE64 & F_SPAM & F_INJECT -- Validated Clean Input --> EXCL_CHECK

            BP_CFG --> BP_NAMES & BP_SOC & BP_APPS --> EXCL_CHECK

            EXCL_CHECK -- Official Asset Match --> EXCL_PASS
            EXCL_PASS --> DB

            EXCL_CHECK -- Non-Official Asset --> DET_LOOKALIKE & DET_HOMOGLYPH & DET_AFFIX & DET_PUB & DET_BADGE
            DET_LOOKALIKE & DET_HOMOGLYPH & DET_AFFIX & DET_PUB & DET_BADGE --> SCORER
            SCORER --> DB

            DB --> TRIAGE
            TRIAGE --> TAKEDOWN
        """.trimIndent()
    }

    val architectureSteps = remember {
        listOf(
            ArchitectureStep(
                stepNumber = "01",
                title = "Multi-Source Ingestion Layer",
                role = "Aggregates social profiles, public posts, company pages, and mobile app store packages.",
                components = listOf("X / Twitter API Stream", "Google Play Store Scraper", "LinkedIn Company Crawler", "Manual Probe Ingest"),
                securityGuarantees = "All inbound payloads are treated as untrusted external inputs.",
                icon = Icons.Default.CloudDownload,
                accentColor = CyberBlue
            ),
            ArchitectureStep(
                stepNumber = "02",
                title = "Security & Input Sanitization Gate",
                role = "Strict firewall enforcing input validation before data touches detection models.",
                components = listOf("Shannon Entropy Filter (>4.65 bits)", "Base64 Ciphertext Block Detector", "WAF Injection Signatures", "Financial Crypto Scam Regex"),
                securityGuarantees = "Immediately blocks corrupt/spam data with: 'Error: Invalid, corrupted, or spam input detected.'",
                icon = Icons.Default.LocalPolice,
                accentColor = CyberRed
            ),
            ArchitectureStep(
                stepNumber = "03",
                title = "Brand Whitelist & Exclusion Engine",
                role = "Critical Rule: Guarantees verified official assets are never falsely flagged as threats.",
                components = listOf("Official Handles Normalizer", "App Package Whitelist", "Verified Developer Matcher", "Exclusion Cache"),
                securityGuarantees = "Exact matches bypass threat scoring and are tagged as OFFICIAL_EXCLUDED (0% Risk).",
                icon = Icons.Default.VerifiedUser,
                accentColor = SafeGreenText
            ),
            ArchitectureStep(
                stepNumber = "04",
                title = "Threat Detection & Analytics Engine",
                role = "Identifies brand impersonation, visual spoofing, and typosquatted scams.",
                components = listOf("Homoglyph & Confusable Mapper", "Damerau-Levenshtein Edit Distance", "Affix & Delimiter Squatting", "Publisher Discrepancy Check"),
                securityGuarantees = "Calculates multi-factor risk score (0-100) with over-flagging protection.",
                icon = Icons.Default.Analytics,
                accentColor = CyberCyan
            ),
            ArchitectureStep(
                stepNumber = "05",
                title = "Incident Store & Takedown Response",
                role = "Alert management, forensic evidence generation, and automated DMCA/takedown notices.",
                components = listOf("Incident Database", "Triage Workflow", "Automated Takedown Notice Generator", "Quarantine Log"),
                securityGuarantees = "Generates actionable legal notices formatted for platform abuse desks.",
                icon = Icons.Default.Gavel,
                accentColor = CyberAmber
            )
        )
    }

    LazyColumn(
        modifier = modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        item {
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Column {
                    Text(
                        text = "System Architecture",
                        style = MaterialTheme.typography.headlineSmall,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onBackground
                    )
                    Text(
                        text = "Data flow, detection pipeline, security gate & official exclusion logic",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }

                Button(
                    onClick = { showMermaidSource = !showMermaidSource },
                    modifier = Modifier.testTag("toggle_mermaid_source_button")
                ) {
                    Icon(
                        imageVector = if (showMermaidSource) Icons.Default.AccountTree else Icons.Default.Code,
                        contentDescription = null,
                        modifier = Modifier.size(16.dp)
                    )
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(if (showMermaidSource) "View Subsystems" else "Mermaid Source", fontSize = 11.sp)
                }
            }
        }

        if (showMermaidSource) {
            item {
                Card(
                    shape = RoundedCornerShape(12.dp),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                    border = CardDefaults.outlinedCardBorder().copy(brush = androidx.compose.ui.graphics.SolidColor(CyberCyan))
                ) {
                    Column(modifier = Modifier.padding(14.dp)) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = "Mermaid.js Architecture Diagram Code",
                                style = MaterialTheme.typography.titleSmall,
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.onSurface
                            )
                            IconButton(
                                onClick = {
                                    val clipboard = context.getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
                                    val clip = ClipData.newPlainText("Mermaid Architecture Diagram", mermaidDiagramCode)
                                    clipboard.setPrimaryClip(clip)
                                    Toast.makeText(context, "Mermaid.js diagram copied to clipboard!", Toast.LENGTH_SHORT).show()
                                }
                            ) {
                                Icon(Icons.Default.ContentCopy, contentDescription = "Copy Mermaid Code", tint = CyberCyan)
                            }
                        }

                        Spacer(modifier = Modifier.height(8.dp))

                        Surface(
                            shape = RoundedCornerShape(8.dp),
                            color = Color.Black.copy(alpha = 0.5f),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Text(
                                text = mermaidDiagramCode,
                                fontSize = 11.sp,
                                fontFamily = FontFamily.Monospace,
                                color = CyberCyan,
                                modifier = Modifier.padding(12.dp)
                            )
                        }
                    }
                }
            }
        } else {
            items(architectureSteps) { step ->
                Card(
                    shape = RoundedCornerShape(12.dp),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                    border = CardDefaults.outlinedCardBorder().copy(brush = androidx.compose.ui.graphics.SolidColor(step.accentColor.copy(alpha = 0.4f)))
                ) {
                    Column(modifier = Modifier.padding(14.dp)) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Surface(
                                    shape = RoundedCornerShape(6.dp),
                                    color = step.accentColor.copy(alpha = 0.15f)
                                ) {
                                    Text(
                                        text = "PHASE ${step.stepNumber}",
                                        fontSize = 11.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = step.accentColor,
                                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                                    )
                                }
                                Spacer(modifier = Modifier.width(10.dp))
                                Text(
                                    text = step.title,
                                    style = MaterialTheme.typography.titleMedium,
                                    fontWeight = FontWeight.Bold,
                                    color = MaterialTheme.colorScheme.onSurface
                                )
                            }
                            Icon(
                                imageVector = step.icon,
                                contentDescription = null,
                                tint = step.accentColor,
                                modifier = Modifier.size(22.dp)
                            )
                        }

                        Spacer(modifier = Modifier.height(8.dp))
                        Text(
                            text = step.role,
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )

                        Spacer(modifier = Modifier.height(10.dp))
                        Text(
                            text = "Key Components:",
                            style = MaterialTheme.typography.labelSmall,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.onSurface
                        )
                        Spacer(modifier = Modifier.height(4.dp))
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(6.dp)
                        ) {
                            step.components.forEach { comp ->
                                Surface(
                                    shape = RoundedCornerShape(4.dp),
                                    color = MaterialTheme.colorScheme.surfaceVariant
                                ) {
                                    Text(
                                        text = comp,
                                        fontSize = 10.sp,
                                        color = MaterialTheme.colorScheme.onSurface,
                                        modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                                    )
                                }
                            }
                        }

                        Spacer(modifier = Modifier.height(10.dp))
                        Surface(
                            shape = RoundedCornerShape(6.dp),
                            color = step.accentColor.copy(alpha = 0.1f),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Row(
                                modifier = Modifier.padding(8.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Icon(
                                    imageVector = Icons.Default.Security,
                                    contentDescription = null,
                                    tint = step.accentColor,
                                    modifier = Modifier.size(14.dp)
                                )
                                Spacer(modifier = Modifier.width(6.dp))
                                Text(
                                    text = step.securityGuarantees,
                                    fontSize = 11.sp,
                                    fontWeight = FontWeight.Medium,
                                    color = step.accentColor
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}
