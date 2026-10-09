package com.example.engine

import com.example.model.*

object MonitoringEngine {

    /**
     * Checks if a candidate asset matches any legitimate asset configured in the Brand Profile.
     * CRITICAL RULE: Official assets MUST be explicitly excluded and never flagged as threats.
     */
    fun isOfficialAsset(asset: MonitoredAsset, profile: BrandProfile): Boolean {
        val cleanCandidateHandle = normalizeHandle(asset.titleOrHandle)

        // 1. Social Media check against official handles & URLs
        for (officialSocial in profile.officialSocialAccounts) {
            val cleanOfficialHandle = normalizeHandle(officialSocial.handle)
            if (cleanCandidateHandle.equals(cleanOfficialHandle, ignoreCase = true)) {
                return true
            }
            if (asset.externalUrl.isNotBlank() && officialSocial.profileUrl.isNotBlank()) {
                if (asset.externalUrl.trim().equals(officialSocial.profileUrl.trim(), ignoreCase = true)) {
                    return true
                }
            }
        }

        // 2. Mobile App check against official package names & developer publishers
        if (asset.type == AssetType.MOBILE_APP) {
            for (officialApp in profile.officialMobileApps) {
                // Direct package name match
                if (cleanCandidateHandle.equals(officialApp.packageName.trim(), ignoreCase = true)) {
                    return true
                }
                // Developer name + exact title match
                if (asset.publisherOrBio.equals(officialApp.developerName, ignoreCase = true) &&
                    asset.displayName.equals(officialApp.appTitle, ignoreCase = true)
                ) {
                    return true
                }
            }
        }

        return false
    }

    private fun normalizeHandle(handle: String): String {
        return handle.trim().removePrefix("@").lowercase()
    }

    /**
     * Evaluates a monitored asset against the active Brand Profile.
     */
    fun evaluate(asset: MonitoredAsset, profile: BrandProfile): MonitoredAsset {
        // =========================================================================
        // CRITICAL RULE: EXCLUDE OFFICIAL ASSETS FROM THREAT DETECTION
        // =========================================================================
        if (isOfficialAsset(asset, profile)) {
            return asset.copy(
                isOfficialAsset = true,
                status = AssetStatus.OFFICIAL_EXCLUDED,
                riskScore = 0,
                severity = ThreatSeverity.SAFE,
                reasons = listOf(
                    ThreatReason(
                        category = "OFFICIAL_ASSET_EXCLUSION",
                        description = "Whitelisted legitimate brand asset.",
                        technicalEvidence = "Identity verified against Brand Profile '${profile.name}'. Strict exclusion rule applied."
                    )
                )
            )
        }

        // =========================================================================
        // THREAT DETECTION ENGINE (Non-Official Assets)
        // =========================================================================
        val reasons = mutableListOf<ThreatReason>()
        var totalScore = 0

        // 1. Look-alike Analysis (Handles & Display Names)
        val brandNamesToTest = listOf(profile.name) + profile.aliases
        var maxLookalikeScore = 0
        var bestLookalikeResult: LookalikeAnalysisResult? = null

        for (targetBrand in brandNamesToTest) {
            val handleResult = LookalikeDetector.analyze(asset.titleOrHandle, targetBrand)
            val displayResult = LookalikeDetector.analyze(asset.displayName, targetBrand)

            val currentMax = maxOf(handleResult.riskScore, displayResult.riskScore)
            if (currentMax > maxLookalikeScore) {
                maxLookalikeScore = currentMax
                bestLookalikeResult = if (handleResult.riskScore >= displayResult.riskScore) handleResult else displayResult
            }
        }

        if (bestLookalikeResult != null && maxLookalikeScore > 0) {
            totalScore += maxLookalikeScore
            for (reason in bestLookalikeResult.detectionReasons) {
                reasons.add(
                    ThreatReason(
                        category = "LOOKALIKE_IMPERSONATION",
                        description = reason,
                        technicalEvidence = "Canonical form: '${bestLookalikeResult.canonicalCandidate}', Edit distance: ${bestLookalikeResult.editDistance}"
                    )
                )
            }
        }

        // 2. Publisher Mismatch Check for Mobile Apps
        if (asset.type == AssetType.MOBILE_APP && profile.officialMobileApps.isNotEmpty()) {
            val officialDevs = profile.officialMobileApps.map { it.developerName.lowercase() }
            val candidatePublisher = asset.publisherOrBio.lowercase()
            val matchesAnyOfficialDev = officialDevs.any { candidatePublisher.contains(it) }

            if (!matchesAnyOfficialDev && maxLookalikeScore >= 35) {
                totalScore += 35
                reasons.add(
                    ThreatReason(
                        category = "PUBLISHER_MISMATCH",
                        description = "App publisher '${asset.publisherOrBio}' is not recognized.",
                        technicalEvidence = "Expected official developer: '${profile.officialMobileApps.first().developerName}'."
                    )
                )
            }
        }

        // 3. Verification Badge Spoofing & Phishing Signals
        val hasSpoofedVerificationEmoji = asset.displayName.contains("✔️") ||
                asset.displayName.contains("✓") ||
                asset.displayName.contains("☑️") ||
                asset.displayName.contains("[Verified]", ignoreCase = true)

        if (hasSpoofedVerificationEmoji) {
            totalScore += 25
            reasons.add(
                ThreatReason(
                    category = "BADGE_SPOOFING",
                    description = "Detected fake verification symbol in display name.",
                    technicalEvidence = "Spoofed unicode glyph used to simulate official platform badge."
                )
            )
        }

        // 4. Low follower / suspicious engagement signal
        val bioLower = asset.publisherOrBio.lowercase()
        val suspiciousBioKeywords = listOf("claim", "airdrop", "giveaway", "recovery", "dm for support", "whatsapp", "t.me")
        if (suspiciousBioKeywords.any { bioLower.contains(it) }) {
            totalScore += 20
            reasons.add(
                ThreatReason(
                    category = "SUSPICIOUS_BIO_CONTENT",
                    description = "Profile bio contains scam or external contact call-to-action.",
                    technicalEvidence = "Bio snippet matches known financial phishing or recovery scam vectors."
                )
            )
        }

        // 5. Logo Likeness
        if (asset.logoLikenessScore >= 75) {
            totalScore += 15
            reasons.add(
                ThreatReason(
                    category = "LOGO_VISUAL_SPOOFING",
                    description = "High visual similarity to official brand logo (${asset.logoLikenessScore}%).",
                    technicalEvidence = "Perceptual hash distance below threshold indicates direct visual copy."
                )
            )
        }

        // Final score capping and classification
        val finalScore = minOf(totalScore, 100)

        val severity = when {
            finalScore >= 80 -> ThreatSeverity.CRITICAL
            finalScore >= 60 -> ThreatSeverity.HIGH
            finalScore >= 35 -> ThreatSeverity.MEDIUM
            else -> ThreatSeverity.LOW
        }

        val status = when {
            finalScore >= 50 -> AssetStatus.FLAGGED_THREAT
            finalScore >= 30 -> AssetStatus.SUSPICIOUS
            else -> AssetStatus.BENIGN_CLEARED
        }

        return asset.copy(
            isOfficialAsset = false,
            status = status,
            riskScore = finalScore,
            severity = severity,
            reasons = reasons
        )
    }
}
