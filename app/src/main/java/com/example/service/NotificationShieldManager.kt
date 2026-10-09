package com.example.service

import android.content.ComponentName
import android.content.Context
import android.content.Intent
import android.provider.Settings
import com.example.data.MockDataProvider
import com.example.engine.LookalikeDetector
import com.example.engine.MonitoringEngine
import com.example.engine.SecurityValidator
import com.example.model.*
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import java.util.UUID

object NotificationShieldManager {

    private val _interceptedNotifications = MutableStateFlow<List<InterceptedNotification>>(emptyList())
    val interceptedNotifications: StateFlow<List<InterceptedNotification>> = _interceptedNotifications.asStateFlow()

    private val _isServiceActive = MutableStateFlow(false)
    val isServiceActive: StateFlow<Boolean> = _isServiceActive.asStateFlow()

    init {
        // Pre-load representative notification scans for demo showcase
        loadInitialMockNotifications()
    }

    fun setServiceActive(active: Boolean) {
        _isServiceActive.value = active
    }

    /**
     * Checks if the user has enabled Notification Access in Android system settings.
     */
    fun isNotificationAccessGranted(context: Context): Boolean {
        val packageName = context.packageName
        val flat = Settings.Secure.getString(context.contentResolver, "enabled_notification_listeners")
        if (flat.isNullOrEmpty()) return false
        val names = flat.split(":")
        for (name in names) {
            val cn = ComponentName.unflattenFromString(name)
            if (cn != null && cn.packageName == packageName) {
                return true
            }
        }
        return false
    }

    /**
     * Intent to open Android Settings page where user grants notification listening access.
     */
    fun getSettingsIntent(): Intent {
        return Intent(Settings.ACTION_NOTIFICATION_LISTENER_SETTINGS).apply {
            flags = Intent.FLAG_ACTIVITY_NEW_TASK
        }
    }

    /**
     * Inspects an incoming mobile notification against brand impersonation and spam heuristics.
     */
    fun inspectAndLog(
        packageName: String,
        appName: String,
        title: String,
        bodyText: String,
        activeBrand: BrandProfile = MockDataProvider.APEX_FINANCIAL
    ): InterceptedNotification {
        val combined = "$title $bodyText"

        // 1. Critical Rule Check: Check if notification originates from official company app
        val isOfficialApp = activeBrand.officialMobileApps.any { it.packageName.equals(packageName.trim(), ignoreCase = true) }
        val isOfficialTitle = activeBrand.officialSocialAccounts.any {
            val cleanOfficial = it.handle.removePrefix("@").lowercase()
            title.lowercase().contains(cleanOfficial)
        }

        if (isOfficialApp) {
            val officialNotification = InterceptedNotification(
                id = UUID.randomUUID().toString(),
                packageName = packageName,
                appName = appName,
                title = title,
                bodyText = bodyText,
                isThreat = false,
                riskScore = 0,
                threatCategory = "OFFICIAL_ASSET_EXCLUDED",
                evidence = "Notification originates from verified official corporate package '${packageName}'. Whitelisted.",
                isOfficialAsset = true
            )
            addNotification(officialNotification)
            return officialNotification
        }

        // 2. Security Gate / Spam Firewall Validation
        val secResult = SecurityValidator.validate(combined)
        if (secResult is SecurityValidationResult.Invalid) {
            val threatNotification = InterceptedNotification(
                id = UUID.randomUUID().toString(),
                packageName = packageName,
                appName = appName,
                title = title,
                bodyText = bodyText,
                isThreat = true,
                riskScore = 95,
                threatCategory = secResult.violationType,
                evidence = "Blocked by Security Firewall: ${secResult.technicalDetails}",
                isOfficialAsset = false
            )
            addNotification(threatNotification)
            return threatNotification
        }

        // 3. Brand Look-alike & Impersonation Check (Smishing / Phishing Detection)
        val lookalikeResult = LookalikeDetector.analyze(title, activeBrand.name)
        val bodyLookalike = LookalikeDetector.analyze(bodyText, activeBrand.name)

        if (lookalikeResult.isLookalike || bodyLookalike.isLookalike) {
            val best = if (lookalikeResult.riskScore >= bodyLookalike.riskScore) lookalikeResult else bodyLookalike
            val reasons = best.detectionReasons.joinToString("; ")
            val threatNotification = InterceptedNotification(
                id = UUID.randomUUID().toString(),
                packageName = packageName,
                appName = appName,
                title = title,
                bodyText = bodyText,
                isThreat = true,
                riskScore = best.riskScore,
                threatCategory = "BRAND_IMPERSONATION_SMISHING",
                evidence = "Impersonates '${activeBrand.name}'. Canonical: '${best.canonicalCandidate}'. Triggers: $reasons",
                isOfficialAsset = false
            )
            addNotification(threatNotification)
            return threatNotification
        }

        // 4. Benign Notification
        val benignNotification = InterceptedNotification(
            id = UUID.randomUUID().toString(),
            packageName = packageName,
            appName = appName,
            title = title,
            bodyText = bodyText,
            isThreat = false,
            riskScore = 5,
            threatCategory = "BENIGN",
            evidence = "Standard application message. No brand impersonation or spam patterns detected.",
            isOfficialAsset = false
        )
        addNotification(benignNotification)
        return benignNotification
    }

    private fun addNotification(item: InterceptedNotification) {
        _interceptedNotifications.value = listOf(item) + _interceptedNotifications.value
    }

    fun clearLog() {
        _interceptedNotifications.value = emptyList()
    }

    private fun loadInitialMockNotifications() {
        _interceptedNotifications.value = listOf(
            InterceptedNotification(
                id = "mock_notif_1",
                packageName = "com.google.android.apps.messaging",
                appName = "Messages (SMS)",
                title = "Security Alert: Apex_Financia1_Support",
                bodyText = "URGENT: Your bank transfer was paused. Verify your identity immediately at bit.ly/apex-claims-portal",
                isThreat = true,
                riskScore = 95,
                threatCategory = "BRAND_IMPERSONATION_SMISHING",
                evidence = "Homoglyph '1' -> 'l' + phishing shortlink imitating Apex Financial.",
                isOfficialAsset = false
            ),
            InterceptedNotification(
                id = "mock_notif_2",
                packageName = "com.apex.mobilebanking",
                appName = "Apex Mobile Banking",
                title = "Apex Financial Account Update",
                bodyText = "Your monthly electronic statement is now ready to view in the official banking portal.",
                isThreat = false,
                riskScore = 0,
                threatCategory = "OFFICIAL_ASSET_EXCLUDED",
                evidence = "Verified official application 'com.apex.mobilebanking'. Whitelisted with 0% risk.",
                isOfficialAsset = true
            ),
            InterceptedNotification(
                id = "mock_notif_3",
                packageName = "com.whatsapp",
                appName = "WhatsApp",
                title = "Apex Rewards Desk",
                bodyText = "Exclusive Apex community airdrop! Connect wallet to claim 500 APEX tokens immediately.",
                isThreat = true,
                riskScore = 90,
                threatCategory = "SPAM_SCAM_HEURISTIC",
                evidence = "Cryptocurrency doubling / wallet connect scam vector.",
                isOfficialAsset = false
            ),
            InterceptedNotification(
                id = "mock_notif_4",
                packageName = "com.google.android.gm",
                appName = "Gmail",
                title = "Weekly Newsletter",
                bodyText = "Here are your top technology headlines for this weekend.",
                isThreat = false,
                riskScore = 5,
                threatCategory = "BENIGN",
                evidence = "Standard communication. No threat signals.",
                isOfficialAsset = false
            )
        )
    }
}
