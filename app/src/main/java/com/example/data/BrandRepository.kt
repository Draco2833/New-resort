package com.example.data

import com.example.engine.MonitoringEngine
import com.example.engine.SecurityValidator
import com.example.model.*
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import java.util.UUID

data class BlockedSecurityEvent(
    val id: String = UUID.randomUUID().toString(),
    val timestamp: Long = System.currentTimeMillis(),
    val payloadPreview: String,
    val violationType: String,
    val technicalDetails: String
)

class BrandRepository {

    private val _currentProfile = MutableStateFlow(MockDataProvider.APEX_FINANCIAL)
    val currentProfile: StateFlow<BrandProfile> = _currentProfile.asStateFlow()

    private val _monitoredAssets = MutableStateFlow<List<MonitoredAsset>>(emptyList())
    val monitoredAssets: StateFlow<List<MonitoredAsset>> = _monitoredAssets.asStateFlow()

    private val _blockedEvents = MutableStateFlow<List<BlockedSecurityEvent>>(emptyList())
    val blockedEvents: StateFlow<List<BlockedSecurityEvent>> = _blockedEvents.asStateFlow()

    private val _isScanning = MutableStateFlow(false)
    val isScanning: StateFlow<Boolean> = _isScanning.asStateFlow()

    init {
        // Load initial mock assets evaluated against default brand profile
        reScanWithProfile(_currentProfile.value)
    }

    fun setBrandProfile(profile: BrandProfile) {
        _currentProfile.value = profile
        reScanWithProfile(profile)
    }

    fun reScanWithProfile(profile: BrandProfile) {
        val rawAssets = if (_monitoredAssets.value.isEmpty()) {
            MockDataProvider.getInitialMonitoredAssets()
        } else {
            _monitoredAssets.value
        }

        val evaluated = rawAssets.map { asset ->
            MonitoringEngine.evaluate(asset, profile)
        }
        _monitoredAssets.value = evaluated
    }

    /**
     * Ingests a new candidate feed item through the strict Security Gate.
     * Returns a SecurityValidationResult. If invalid, the payload is blocked and logged.
     */
    fun ingestCandidateAsset(
        type: AssetType,
        platformName: String,
        titleOrHandle: String,
        displayName: String,
        publisherOrBio: String
    ): SecurityValidationResult {
        // Validate each input field through the Security Gate
        val handleCheck = SecurityValidator.validate(titleOrHandle)
        if (handleCheck is SecurityValidationResult.Invalid) {
            logBlockedEvent(handleCheck)
            return handleCheck
        }

        val nameCheck = SecurityValidator.validate(displayName)
        if (nameCheck is SecurityValidationResult.Invalid) {
            logBlockedEvent(nameCheck)
            return nameCheck
        }

        val bioCheck = SecurityValidator.validate(publisherOrBio)
        if (bioCheck is SecurityValidationResult.Invalid) {
            logBlockedEvent(bioCheck)
            return bioCheck
        }

        // Passed security gate -> evaluate against brand profile
        val raw = MonitoredAsset(
            id = "user_ingested_" + UUID.randomUUID().toString().take(8),
            type = type,
            platformName = platformName,
            titleOrHandle = titleOrHandle.trim(),
            displayName = displayName.trim(),
            publisherOrBio = publisherOrBio.trim(),
            followersOrDownloads = "Newly Detected",
            logoLikenessScore = 70
        )

        val evaluated = MonitoringEngine.evaluate(raw, _currentProfile.value)
        _monitoredAssets.value = listOf(evaluated) + _monitoredAssets.value

        return SecurityValidationResult.Valid(titleOrHandle)
    }

    private fun logBlockedEvent(invalid: SecurityValidationResult.Invalid) {
        val event = BlockedSecurityEvent(
            payloadPreview = invalid.blockedPayloadPreview,
            violationType = invalid.violationType,
            technicalDetails = invalid.technicalDetails
        )
        _blockedEvents.value = listOf(event) + _blockedEvents.value
    }

    fun addOfficialSocialAccount(account: OfficialSocialAccount) {
        val updated = _currentProfile.value.copy(
            officialSocialAccounts = _currentProfile.value.officialSocialAccounts + account
        )
        setBrandProfile(updated)
    }

    fun addOfficialMobileApp(app: OfficialMobileApp) {
        val updated = _currentProfile.value.copy(
            officialMobileApps = _currentProfile.value.officialMobileApps + app
        )
        setBrandProfile(updated)
    }

    fun updateTriageStatus(assetId: String, status: TriageStatus) {
        _monitoredAssets.value = _monitoredAssets.value.map { asset ->
            if (asset.id == assetId) {
                asset.copy(triageStatus = status)
            } else {
                asset
            }
        }
    }

    fun resetToDefault() {
        _currentProfile.value = MockDataProvider.APEX_FINANCIAL
        _monitoredAssets.value = MockDataProvider.getInitialMonitoredAssets().map {
            MonitoringEngine.evaluate(it, MockDataProvider.APEX_FINANCIAL)
        }
        _blockedEvents.value = emptyList()
    }
}
