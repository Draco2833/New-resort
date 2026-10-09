package com.example.model

enum class AssetType {
    SOCIAL_PROFILE,
    MOBILE_APP
}

enum class AssetStatus {
    OFFICIAL_EXCLUDED, // Critical Rule: Explicitly excluded legitimate official asset
    FLAGGED_THREAT,     // Active impersonation/scam threat
    SUSPICIOUS,         // Needs review / medium risk
    BENIGN_CLEARED      // Cleared by analyst / benign third-party
}

enum class ThreatSeverity(val label: String) {
    CRITICAL("CRITICAL"),
    HIGH("HIGH"),
    MEDIUM("MEDIUM"),
    LOW("LOW"),
    SAFE("OFFICIAL SAFE")
}

enum class TriageStatus {
    UNREVIEWED,
    TAKEDOWN_REQUESTED,
    ESCALATED,
    MARKED_FALSE_POSITIVE
}

data class ThreatReason(
    val category: String,
    val description: String,
    val technicalEvidence: String
)

data class MonitoredAsset(
    val id: String,
    val type: AssetType,
    val platformName: String,
    val titleOrHandle: String,
    val displayName: String,
    val publisherOrBio: String,
    val followersOrDownloads: String = "N/A",
    val isOfficialAsset: Boolean = false,
    val status: AssetStatus = AssetStatus.FLAGGED_THREAT,
    val riskScore: Int = 0, // 0 to 100
    val severity: ThreatSeverity = ThreatSeverity.LOW,
    val reasons: List<ThreatReason> = emptyList(),
    val logoLikenessScore: Int = 0, // 0 to 100%
    val externalUrl: String = "",
    val detectedTimestamp: Long = System.currentTimeMillis(),
    val triageStatus: TriageStatus = TriageStatus.UNREVIEWED
)
