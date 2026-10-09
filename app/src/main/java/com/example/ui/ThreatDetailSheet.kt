package com.example.ui

import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import android.widget.Toast
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
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
import com.example.model.*
import com.example.ui.theme.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ThreatDetailSheet(
    asset: MonitoredAsset,
    brandProfile: BrandProfile,
    onDismiss: () -> Unit,
    onTriage: (TriageStatus) -> Unit
) {
    val context = LocalContext.current
    var activeTab by remember { mutableStateOf(0) } // 0: Forensics, 1: Takedown Notice

    val takedownNoticeText = remember(asset, brandProfile) {
        """
        ========================================================================
        DIGITAL RISK PROTECTION - OFFICIAL TAKEDOWN REQUEST NOTICE
        ========================================================================
        Date: ${java.text.SimpleDateFormat("yyyy-MM-dd HH:mm:ss z", java.util.Locale.US).format(java.util.Date())}
        Notice Reference: DRP-${asset.id.uppercase()}

        TO: Abuse & Trust/Safety Department (${asset.platformName})
        RE: Unauthorized Brand Impersonation & Malicious Fraud Entity

        1. ENTITY IN VIOLATION:
           - Platform: ${asset.platformName}
           - Handle / Package ID: ${asset.titleOrHandle}
           - Display Name: ${asset.displayName}
           - Bio / Publisher: ${asset.publisherOrBio}
           - Target URL: ${if (asset.externalUrl.isNotBlank()) asset.externalUrl else "N/A"}

        2. AFFECTED TRADEMARK / BRAND OWNER:
           - Legitimate Entity: ${brandProfile.name}
           - Protected Aliases: ${brandProfile.aliases.joinToString(", ")}
           - Verified Official Domain/Handle: ${brandProfile.officialSocialAccounts.firstOrNull()?.handle ?: "Verified Tenant"}

        3. FORENSIC EVIDENCE OF INFRINGEMENT:
           - Threat Severity: ${asset.severity.label} (Calculated Risk Score: ${asset.riskScore}/100)
        ${asset.reasons.joinToString("\n") { "   * [${it.category}]: ${it.description} | Evidence: ${it.technicalEvidence}" }}

        4. DEMAND & LEGAL NOTICE:
           We request the immediate suspension, removal, or quarantine of the 
           infringing asset identified above. This account/app constitutes trademark 
           infringement, deceptive consumer misrepresentation, and active cyber risk.

           Submitted via Sentinel Digital Risk Protection Platform.
        ========================================================================
        """.trimIndent()
    }

    ModalBottomSheet(
        onDismissRequest = onDismiss,
        sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true),
        containerColor = MaterialTheme.colorScheme.surface,
        modifier = Modifier.testTag("threat_detail_bottom_sheet")
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 20.dp)
                .padding(bottom = 32.dp)
                .verticalScroll(rememberScrollState())
        ) {
            // Header
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        imageVector = if (asset.isOfficialAsset) Icons.Default.Verified else Icons.Default.Warning,
                        contentDescription = null,
                        tint = if (asset.isOfficialAsset) SafeGreenText else CyberRed,
                        modifier = Modifier.size(28.dp)
                    )
                    Spacer(modifier = Modifier.width(10.dp))
                    Column {
                        Text(
                            text = asset.displayName,
                            style = MaterialTheme.typography.titleLarge,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.onSurface
                        )
                        Text(
                            text = "${asset.platformName} • ${asset.titleOrHandle}",
                            style = MaterialTheme.typography.bodySmall,
                            fontFamily = FontFamily.Monospace,
                            color = CyberCyan
                        )
                    }
                }

                Surface(
                    shape = RoundedCornerShape(6.dp),
                    color = if (asset.isOfficialAsset) SafeGreenBg else CriticalRedBg
                ) {
                    Text(
                        text = if (asset.isOfficialAsset) "OFFICIAL SAFE" else "${asset.riskScore}% RISK",
                        fontWeight = FontWeight.Bold,
                        fontSize = 12.sp,
                        color = if (asset.isOfficialAsset) SafeGreenText else CriticalRedText,
                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                    )
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            // Tab Selector
            TabRow(
                selectedTabIndex = activeTab,
                containerColor = MaterialTheme.colorScheme.surfaceVariant,
                contentColor = CyberCyan
            ) {
                Tab(
                    selected = activeTab == 0,
                    onClick = { activeTab = 0 },
                    text = { Text("Forensic Analysis", fontSize = 12.sp) }
                )
                Tab(
                    selected = activeTab == 1,
                    onClick = { activeTab = 1 },
                    text = { Text("Takedown Notice", fontSize = 12.sp) }
                )
            }

            Spacer(modifier = Modifier.height(14.dp))

            if (activeTab == 0) {
                // Forensic Analysis Tab
                Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                    // Profile/Publisher info
                    Surface(
                        shape = RoundedCornerShape(8.dp),
                        color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Column(modifier = Modifier.padding(12.dp)) {
                            Text(
                                text = "Reported Profile / Metadata Details",
                                style = MaterialTheme.typography.labelMedium,
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.onSurface
                            )
                            Spacer(modifier = Modifier.height(4.dp))
                            Text(
                                text = "Bio/Publisher: ${asset.publisherOrBio}",
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                            Text(
                                text = "Engagement/Audience: ${asset.followersOrDownloads}",
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                            if (asset.logoLikenessScore > 0) {
                                Text(
                                    text = "Logo Likeness Similarity: ${asset.logoLikenessScore}%",
                                    style = MaterialTheme.typography.bodySmall,
                                    color = if (asset.logoLikenessScore >= 75) CyberRed else MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }
                        }
                    }

                    // Detection Rule Triggers
                    Text(
                        text = "Detection Engine Rule Triggers (${asset.reasons.size}):",
                        style = MaterialTheme.typography.titleSmall,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onSurface
                    )

                    asset.reasons.forEach { reason ->
                        Card(
                            shape = RoundedCornerShape(8.dp),
                            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant),
                            border = CardDefaults.outlinedCardBorder().copy(brush = androidx.compose.ui.graphics.SolidColor(CyberCardBorder))
                        ) {
                            Column(modifier = Modifier.padding(10.dp)) {
                                Text(
                                    text = reason.category,
                                    fontWeight = FontWeight.Bold,
                                    fontFamily = FontFamily.Monospace,
                                    fontSize = 11.sp,
                                    color = CyberCyan
                                )
                                Spacer(modifier = Modifier.height(2.dp))
                                Text(
                                    text = reason.description,
                                    style = MaterialTheme.typography.bodyMedium,
                                    color = MaterialTheme.colorScheme.onSurface
                                )
                                Spacer(modifier = Modifier.height(4.dp))
                                Text(
                                    text = "Evidence: ${reason.technicalEvidence}",
                                    style = MaterialTheme.typography.bodySmall,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }
                        }
                    }

                    // Triage Actions
                    if (!asset.isOfficialAsset) {
                        Spacer(modifier = Modifier.height(8.dp))
                        Text(
                            text = "Incident Triage Actions:",
                            style = MaterialTheme.typography.titleSmall,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.onSurface
                        )
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            Button(
                                onClick = {
                                    onTriage(TriageStatus.TAKEDOWN_REQUESTED)
                                    onDismiss()
                                },
                                modifier = Modifier.weight(1f),
                                colors = ButtonDefaults.buttonColors(containerColor = CyberRed)
                            ) {
                                Text("Send Takedown", fontSize = 11.sp)
                            }
                            FilledTonalButton(
                                onClick = {
                                    onTriage(TriageStatus.ESCALATED)
                                    onDismiss()
                                },
                                modifier = Modifier.weight(1f)
                            ) {
                                Text("Escalate Legal", fontSize = 11.sp)
                            }
                            OutlinedButton(
                                onClick = {
                                    onTriage(TriageStatus.MARKED_FALSE_POSITIVE)
                                    onDismiss()
                                },
                                modifier = Modifier.weight(1f)
                            ) {
                                Text("False Positive", fontSize = 11.sp)
                            }
                        }
                    }
                }
            } else {
                // Takedown Notice Tab
                Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                    Text(
                        text = "Automated DMCA & Trademark Abuse Takedown Notice",
                        style = MaterialTheme.typography.titleSmall,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onSurface
                    )

                    Surface(
                        shape = RoundedCornerShape(8.dp),
                        color = Color.Black.copy(alpha = 0.5f),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Text(
                            text = takedownNoticeText,
                            fontSize = 11.sp,
                            fontFamily = FontFamily.Monospace,
                            color = CyberCyan,
                            modifier = Modifier.padding(12.dp)
                        )
                    }

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.End
                    ) {
                        Button(
                            onClick = {
                                val clipboard = context.getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
                                val clip = ClipData.newPlainText("Takedown Notice", takedownNoticeText)
                                clipboard.setPrimaryClip(clip)
                                Toast.makeText(context, "Takedown Notice copied to clipboard!", Toast.LENGTH_SHORT).show()
                            }
                        ) {
                            Icon(Icons.Default.ContentCopy, contentDescription = null, modifier = Modifier.size(16.dp))
                            Spacer(modifier = Modifier.width(6.dp))
                            Text("Copy Notice")
                        }
                    }
                }
            }
        }
    }
}
