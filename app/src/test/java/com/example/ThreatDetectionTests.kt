package com.example

import com.example.data.MockDataProvider
import com.example.engine.LookalikeDetector
import com.example.engine.MonitoringEngine
import com.example.engine.SecurityValidator
import com.example.model.*
import org.junit.Assert.*
import org.junit.Test

class ThreatDetectionTests {

    private val brandProfile = MockDataProvider.APEX_FINANCIAL

    // =========================================================================
    // 1. CRITICAL RULE TEST: OFFICIAL ASSETS EXCLUSION
    // =========================================================================
    @Test
    fun testOfficialSocialAccountIsStrictlyExcluded() {
        val officialSocialAsset = MonitoredAsset(
            id = "test_official_tw",
            type = AssetType.SOCIAL_PROFILE,
            platformName = "X / Twitter",
            titleOrHandle = "@ApexFinancial", // Exact official handle
            displayName = "Apex Financial Official",
            publisherOrBio = "Official Twitter Account"
        )

        val evaluated = MonitoringEngine.evaluate(officialSocialAsset, brandProfile)

        assertTrue("Official social asset MUST be flagged as official", evaluated.isOfficialAsset)
        assertEquals("Status MUST be OFFICIAL_EXCLUDED", AssetStatus.OFFICIAL_EXCLUDED, evaluated.status)
        assertEquals("Risk score MUST be 0", 0, evaluated.riskScore)
        assertEquals("Severity MUST be SAFE", ThreatSeverity.SAFE, evaluated.severity)
        assertTrue(evaluated.reasons.any { it.category == "OFFICIAL_ASSET_EXCLUSION" })
    }

    @Test
    fun testOfficialMobileAppIsStrictlyExcluded() {
        val officialAppAsset = MonitoredAsset(
            id = "test_official_app",
            type = AssetType.MOBILE_APP,
            platformName = "Google Play Store",
            titleOrHandle = "com.apex.mobilebanking", // Exact official package
            displayName = "Apex Mobile Banking",
            publisherOrBio = "Apex Financial Group"
        )

        val evaluated = MonitoringEngine.evaluate(officialAppAsset, brandProfile)

        assertTrue("Official mobile app MUST be flagged as official", evaluated.isOfficialAsset)
        assertEquals("Status MUST be OFFICIAL_EXCLUDED", AssetStatus.OFFICIAL_EXCLUDED, evaluated.status)
        assertEquals("Risk score MUST be 0", 0, evaluated.riskScore)
        assertEquals("Severity MUST be SAFE", ThreatSeverity.SAFE, evaluated.severity)
    }

    // =========================================================================
    // 2. LOOK-ALIKE & HOMOGLYPH DETECTION TESTS
    // =========================================================================
    @Test
    fun testHomoglyphSwapDetection() {
        // '1' swapped for 'l'
        val result1 = LookalikeDetector.analyze("Apex_Financia1", "Apex Financial")
        assertTrue("Candidate with homoglyph '1' must be flagged", result1.isLookalike)
        assertTrue(result1.detectedHomoglyphs.any { it.first == '1' && it.second == 'l' })

        // '0' swapped for 'o'
        val result2 = LookalikeDetector.analyze("0fficial_ApexFinancial", "Apex Financial")
        assertTrue("Candidate with homoglyph '0' must be flagged", result2.isLookalike)
        assertTrue(result2.detectedHomoglyphs.any { it.first == '0' && it.second == 'o' })

        // '3' swapped for 'e'
        val result3 = LookalikeDetector.analyze("Ap3x_Financial", "Apex Financial")
        assertTrue("Candidate with homoglyph '3' must be flagged", result3.isLookalike)
        assertTrue(result3.detectedHomoglyphs.any { it.first == '3' && it.second == 'e' })
    }

    @Test
    fun testAffixAndKeywordSquattingDetection() {
        val result = LookalikeDetector.analyze("Apex_Financial_Support", "Apex Financial")
        assertTrue(result.isLookalike)
        assertTrue("Should detect 'support' keyword", result.detectedAffixes.contains("support"))
        assertTrue(result.riskScore >= 70)
    }

    @Test
    fun testTyposquattingEditDistance() {
        // Adjacent transposition or minor character edit
        val dist = LookalikeDetector.damerauLevenshteinDistance("apex", "apxe")
        assertEquals("Damerau-Levenshtein transposition distance should be 1", 1, dist)
    }

    @Test
    fun testOverflaggingProtectionForBenignUnrelatedWords() {
        // Community gaming or mountaineering accounts should not be flagged as brand impersonators
        val resultGaming = LookalikeDetector.analyze("ApexLegendsDaily", "Apex Financial")
        assertFalse("Unrelated gaming fan handle should not be flagged as threat", resultGaming.isLookalike)
        assertTrue("Over-flagging protection must trigger", resultGaming.overflaggingProtectionTriggered)
    }

    // =========================================================================
    // 3. SECURITY GATE & SPAM / CORRUPTED INPUT DETECTION TESTS
    // =========================================================================
    @Test
    fun testSecurityValidatorBlocksCorruptedBase64() {
        val corruptedPayload = "U2FsdGVkX181dK3k8lFq8Y8zL9kLmN0oPqRsTuVwXyZ123456789+=="
        val result = SecurityValidator.validate(corruptedPayload)

        assertTrue("Should reject corrupted Base64", result is SecurityValidationResult.Invalid)
        val invalid = result as SecurityValidationResult.Invalid
        assertEquals(
            "Must return required UI error message",
            "Error: Invalid, corrupted, or spam input detected.",
            invalid.errorMessage
        )
        assertEquals("BASE64_PAYLOAD_ABUSE", invalid.violationType)
    }

    @Test
    fun testSecurityValidatorBlocksHighEntropyGibberish() {
        val highEntropyCipher = "7f8b9c0d1e2f3a4b5c6d7e8f9a0b1c2d3e4f5a6b"
        val result = SecurityValidator.validate(highEntropyCipher)

        assertTrue("Should reject high-entropy ciphertext", result is SecurityValidationResult.Invalid)
        val invalid = result as SecurityValidationResult.Invalid
        assertEquals(
            "Must return required UI error message",
            "Error: Invalid, corrupted, or spam input detected.",
            invalid.errorMessage
        )
        assertEquals("HIGH_ENTROPY_CIPHERTEXT", invalid.violationType)
    }

    @Test
    fun testSecurityValidatorBlocksCryptoScamSpam() {
        val cryptoScam = "Send 1 ETH to our wallet to receive 2 ETH back immediately! Limited airdrop."
        val result = SecurityValidator.validate(cryptoScam)

        assertTrue("Should reject crypto doubling scam", result is SecurityValidationResult.Invalid)
        val invalid = result as SecurityValidationResult.Invalid
        assertEquals(
            "Must return required UI error message",
            "Error: Invalid, corrupted, or spam input detected.",
            invalid.errorMessage
        )
        assertEquals("SPAM_SCAM_HEURISTIC", invalid.violationType)
    }

    @Test
    fun testSecurityValidatorBlocksRecoveryDMSpam() {
        val recoverySpam = "DM @cyber_recovery_agent on Instagram to unlock your suspended bank account now."
        val result = SecurityValidator.validate(recoverySpam)

        assertTrue("Should reject account recovery spam", result is SecurityValidationResult.Invalid)
        val invalid = result as SecurityValidationResult.Invalid
        assertEquals(
            "Must return required UI error message",
            "Error: Invalid, corrupted, or spam input detected.",
            invalid.errorMessage
        )
    }

    @Test
    fun testSecurityValidatorBlocksInjectionAttacks() {
        val sqli = "' OR '1'='1' UNION SELECT username, password FROM users --"
        val result = SecurityValidator.validate(sqli)

        assertTrue("Should reject SQL injection", result is SecurityValidationResult.Invalid)
        val invalid = result as SecurityValidationResult.Invalid
        assertEquals(
            "Must return required UI error message",
            "Error: Invalid, corrupted, or spam input detected.",
            invalid.errorMessage
        )
        assertEquals("SECURITY_INJECTION_SIGNATURE", invalid.violationType)
    }

    @Test
    fun testSecurityValidatorAcceptsCleanLegitimateInput() {
        val cleanInput = "Apex Financial Customer Support Portal"
        val result = SecurityValidator.validate(cleanInput)

        assertTrue("Legitimate input should be valid", result is SecurityValidationResult.Valid)
        val valid = result as SecurityValidationResult.Valid
        assertEquals("Apex Financial Customer Support Portal", valid.sanitizedText)
    }
}
