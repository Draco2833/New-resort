package com.example.ui

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.data.BrandRepository
import com.example.data.MockDataProvider
import com.example.engine.LookalikeAnalysisResult
import com.example.engine.LookalikeDetector
import com.example.engine.SecurityValidator
import com.example.model.*
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch

enum class AppNavDestination(val label: String) {
    DASHBOARD("Threats"),
    BRAND_PROFILE("Brand"),
    LOOKALIKE_LAB("Look-alike"),
    NOTIFICATION_SHIELD("Notifs"),
    SECURITY_GATE("Security Gate"),
    ARCHITECTURE("Architecture")
}

data class PresetSecurityVector(
    val title: String,
    val category: String,
    val payload: String,
    val expectedOutcome: String
)

class MainViewModel(
    val repository: BrandRepository = BrandRepository()
) : ViewModel() {

    val currentProfile: StateFlow<BrandProfile> = repository.currentProfile
    val monitoredAssets: StateFlow<List<MonitoredAsset>> = repository.monitoredAssets
    val blockedEvents = repository.blockedEvents
    val isScanning = repository.isScanning

    // Navigation
    private val _currentNav = MutableStateFlow(AppNavDestination.DASHBOARD)
    val currentNav: StateFlow<AppNavDestination> = _currentNav.asStateFlow()

    // Dashboard Search & Filters
    private val _searchQuery = MutableStateFlow("")
    val searchQuery: StateFlow<String> = _searchQuery.asStateFlow()

    private val _selectedFilter = MutableStateFlow("ALL")
    val selectedFilter: StateFlow<String> = _selectedFilter.asStateFlow()

    val filteredAssets: StateFlow<List<MonitoredAsset>> = combine(
        monitoredAssets,
        searchQuery,
        selectedFilter
    ) { assets, query, filter ->
        assets.filter { asset ->
            val matchesQuery = query.isBlank() ||
                    asset.titleOrHandle.contains(query, ignoreCase = true) ||
                    asset.displayName.contains(query, ignoreCase = true) ||
                    asset.publisherOrBio.contains(query, ignoreCase = true) ||
                    asset.platformName.contains(query, ignoreCase = true)

            val matchesFilter = when (filter) {
                "SOCIAL" -> asset.type == AssetType.SOCIAL_PROFILE && !asset.isOfficialAsset
                "APPS" -> asset.type == AssetType.MOBILE_APP && !asset.isOfficialAsset
                "OFFICIAL_EXCLUDED" -> asset.isOfficialAsset
                "CRITICAL_ONLY" -> asset.severity == ThreatSeverity.CRITICAL
                "THREATS_ONLY" -> !asset.isOfficialAsset && asset.status == AssetStatus.FLAGGED_THREAT
                else -> true
            }

            matchesQuery && matchesFilter
        }
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    // Metrics
    val totalScanned = monitoredAssets.map { it.size }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), 0)

    val criticalThreatsCount = monitoredAssets.map { it.count { a -> a.severity == ThreatSeverity.CRITICAL } }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), 0)

    val officialExcludedCount = monitoredAssets.map { it.count { a -> a.isOfficialAsset } }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), 0)

    val blockedCount = blockedEvents.map { it.size }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), 0)

    // Threat detail & takedown notice sheet
    private val _selectedThreat = MutableStateFlow<MonitoredAsset?>(null)
    val selectedThreat: StateFlow<MonitoredAsset?> = _selectedThreat.asStateFlow()

    // Look-alike Interactive Lab State (Bonus Feature)
    private val _lookalikeCandidateInput = MutableStateFlow("@Apex_Financia1_Support")
    val lookalikeCandidateInput: StateFlow<String> = _lookalikeCandidateInput.asStateFlow()

    private val _lookalikeResult = MutableStateFlow<LookalikeAnalysisResult?>(null)
    val lookalikeResult: StateFlow<LookalikeAnalysisResult?> = _lookalikeResult.asStateFlow()

    // Security Gate Interactive State (Security Requirement)
    private val _securityInput = MutableStateFlow("")
    val securityInput: StateFlow<String> = _securityInput.asStateFlow()

    private val _securityValidationResult = MutableStateFlow<SecurityValidationResult?>(null)
    val securityValidationResult: StateFlow<SecurityValidationResult?> = _securityValidationResult.asStateFlow()

    val securityTestVectors = listOf(
        PresetSecurityVector(
            title = "Corrupted Base64 Payload",
            category = "Binary / Ciphertext Abuse",
            payload = "U2FsdGVkX181dK3k8lFq8Y8zL9kLmN0oPqRsTuVwXyZ123456789+==",
            expectedOutcome = "Blocked by Base64 payload & entropy rule"
        ),
        PresetSecurityVector(
            title = "High-Entropy Gibberish",
            category = "Encrypted String",
            payload = "7f8b9c0d1e2f3a4b5c6d7e8f9a0b1c2d3e4f5a6b",
            expectedOutcome = "Blocked by Shannon entropy threshold (>4.65)"
        ),
        PresetSecurityVector(
            title = "Crypto Doubling Scam",
            category = "Spam / Financial Fraud",
            payload = "Send 1 ETH to our verified company treasury to receive 2 ETH back instantly! Official bonus.",
            expectedOutcome = "Blocked by cryptocurrency doubling heuristic"
        ),
        PresetSecurityVector(
            title = "Recovery DM Spam",
            category = "Social Impersonation",
            payload = "Contact @cyber_recovery_expert on Instagram immediately to unlock your suspended brand account.",
            expectedOutcome = "Blocked by illicit account unlock heuristic"
        ),
        PresetSecurityVector(
            title = "SQL Injection Payload",
            category = "Malicious Code",
            payload = "' OR '1'='1' UNION SELECT username, password FROM users --",
            expectedOutcome = "Blocked by WAF injection signature"
        ),
        PresetSecurityVector(
            title = "Clean Query / Safe Input",
            category = "Legitimate Probe",
            payload = "Apex Financial Official Customer Portal",
            expectedOutcome = "Passed Security Gate successfully"
        )
    )

    init {
        // Run initial lookalike test for default candidate
        testLookalikeCandidate(_lookalikeCandidateInput.value)
    }

    fun setNav(nav: AppNavDestination) {
        _currentNav.value = nav
    }

    fun setSearchQuery(query: String) {
        _searchQuery.value = query
    }

    fun setSelectedFilter(filter: String) {
        _selectedFilter.value = filter
    }

    fun selectThreat(asset: MonitoredAsset?) {
        _selectedThreat.value = asset
    }

    fun updateTriage(assetId: String, status: TriageStatus) {
        repository.updateTriageStatus(assetId, status)
        if (_selectedThreat.value?.id == assetId) {
            _selectedThreat.value = _selectedThreat.value?.copy(triageStatus = status)
        }
    }

    fun switchBrandProfile(presetId: String) {
        when (presetId) {
            "APEX" -> repository.setBrandProfile(MockDataProvider.APEX_FINANCIAL)
            "CLOUD_VAULT" -> repository.setBrandProfile(MockDataProvider.CLOUD_VAULT)
            "AEROPAY" -> repository.setBrandProfile(MockDataProvider.AEROPAY)
        }
        testLookalikeCandidate(_lookalikeCandidateInput.value)
    }

    fun updateBrandName(newName: String) {
        val updated = currentProfile.value.copy(name = newName)
        repository.setBrandProfile(updated)
        testLookalikeCandidate(_lookalikeCandidateInput.value)
    }

    fun addOfficialSocial(platform: PlatformType, handle: String, url: String) {
        repository.addOfficialSocialAccount(
            OfficialSocialAccount(
                platform = platform,
                handle = handle.trim(),
                profileUrl = url.trim(),
                verified = true
            )
        )
    }

    fun addOfficialApp(platform: AppStoreType, packageName: String, title: String, devName: String) {
        repository.addOfficialMobileApp(
            OfficialMobileApp(
                platform = platform,
                packageName = packageName.trim(),
                appTitle = title.trim(),
                developerName = devName.trim(),
                storeUrl = "https://play.google.com/store/apps/details?id=${packageName.trim()}"
            )
        )
    }

    // Look-alike tester
    fun setLookalikeCandidateInput(input: String) {
        _lookalikeCandidateInput.value = input
        testLookalikeCandidate(input)
    }

    fun testLookalikeCandidate(candidate: String) {
        val brandName = currentProfile.value.name
        _lookalikeResult.value = LookalikeDetector.analyze(candidate, brandName)
    }

    // Security Validator
    fun setSecurityInput(input: String) {
        _securityInput.value = input
    }

    fun evaluateSecurityInput() {
        val result = SecurityValidator.validate(_securityInput.value)
        _securityValidationResult.value = result
    }

    fun loadSecurityVector(vector: PresetSecurityVector) {
        _securityInput.value = vector.payload
        evaluateSecurityInput()
    }

    fun submitSecurityFeedIngest(
        type: AssetType,
        platformName: String,
        handle: String,
        name: String,
        bio: String
    ): SecurityValidationResult {
        val result = repository.ingestCandidateAsset(
            type = type,
            platformName = platformName,
            titleOrHandle = handle,
            displayName = name,
            publisherOrBio = bio
        )
        _securityValidationResult.value = result
        return result
    }

    fun resetToDemoDefaults() {
        repository.resetToDefault()
        _searchQuery.value = ""
        _selectedFilter.value = "ALL"
        _lookalikeCandidateInput.value = "@Apex_Financia1_Support"
        testLookalikeCandidate("@Apex_Financia1_Support")
        _securityInput.value = ""
        _securityValidationResult.value = null
    }
}
