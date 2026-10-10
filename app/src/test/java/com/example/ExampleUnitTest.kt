package com.example

import com.example.data.MockDataProvider
import com.example.engine.LookalikeDetector
import com.example.engine.MonitoringEngine
import com.example.engine.SecurityValidator
import com.example.model.*
import org.junit.Assert.*
import org.junit.Test

class ExampleUnitTest {

  @Test
  fun testOfficialAssetsAreExcluded() {
    val profile = MockDataProvider.APEX_FINANCIAL
    val officialAsset = MonitoredAsset(
      id = "test_official",
      type = AssetType.SOCIAL_PROFILE,
      platformName = "X / Twitter",
      titleOrHandle = "@ApexFinancial",
      displayName = "Apex Financial",
      publisherOrBio = "Official account",
      logoLikenessScore = 100
    )

    val evaluated = MonitoringEngine.evaluate(officialAsset, profile)
    assertTrue("Official asset must be marked as official", evaluated.isOfficialAsset)
    assertEquals(AssetStatus.OFFICIAL_EXCLUDED, evaluated.status)
    assertEquals(0, evaluated.riskScore)
    assertEquals(ThreatSeverity.SAFE, evaluated.severity)
  }

  @Test
  fun testSameUsernameNotOnWhitelistIsFlagged() {
    val profile = MockDataProvider.APEX_FINANCIAL
    // Rogue account on Instagram (not registered in official accounts) using same brand name
    val rogueClone = MonitoredAsset(
      id = "test_rogue",
      type = AssetType.SOCIAL_PROFILE,
      platformName = "Instagram",
      titleOrHandle = "@ApexFinancial_OfficialScam",
      displayName = "Apex Financial",
      publisherOrBio = "DM for customer support claim",
      logoLikenessScore = 90
    )

    val evaluated = MonitoringEngine.evaluate(rogueClone, profile)
    assertFalse("Unverified clone must not be marked official", evaluated.isOfficialAsset)
    assertTrue("Risk score should be high", evaluated.riskScore >= 60)
    assertTrue(evaluated.reasons.any { it.category == "LOGO_VISUAL_SPOOFING" })
  }

  @Test
  fun testLogoLikenessDetectionThreshold() {
    val profile = MockDataProvider.APEX_FINANCIAL
    val fakeAppHighLogo = MonitoredAsset(
      id = "test_fake_app",
      type = AssetType.MOBILE_APP,
      platformName = "Google Play Store",
      titleOrHandle = "com.apexfinance.mobileapp",
      displayName = "Apex Mobile Banking",
      publisherOrBio = "Unknown Rogue Developer",
      logoLikenessScore = 88
    )

    val evaluated = MonitoringEngine.evaluate(fakeAppHighLogo, profile)
    assertTrue(evaluated.reasons.any { it.category == "LOGO_VISUAL_SPOOFING" })
    assertTrue(evaluated.reasons.any { it.category == "PUBLISHER_MISMATCH" })
  }

  @Test
  fun testLookalikeHomoglyphDetection() {
    val result = LookalikeDetector.analyze("@Apex_Financia1", "Apex Financial")
    assertTrue("Should detect homoglyph variation", result.isLookalike)
    assertTrue(result.riskScore > 50)
  }

  @Test
  fun testSecurityValidatorBlocksSpamAndCorruptedInput() {
    val corrupted = "U2FsdGVkX181dK3k8lFq8Y8zL9kLmN0oPqRsTuVwXyZ123456789+=="
    val check = SecurityValidator.validate(corrupted)
    assertTrue("Corrupted payload must be rejected", check is SecurityValidationResult.Invalid)
    val invalid = check as SecurityValidationResult.Invalid
    assertEquals("Error: Invalid, corrupted, or spam input detected.", invalid.errorMessage)
  }
}
