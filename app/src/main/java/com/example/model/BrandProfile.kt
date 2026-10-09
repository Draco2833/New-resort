package com.example.model

enum class PlatformType(val displayName: String, val iconName: String) {
    TWITTER("X / Twitter", "twitter"),
    LINKEDIN("LinkedIn", "linkedin"),
    INSTAGRAM("Instagram", "instagram"),
    FACEBOOK("Facebook", "facebook"),
    YOUTUBE("YouTube", "youtube"),
    TIKTOK("TikTok", "tiktok")
}

enum class AppStoreType(val displayName: String) {
    GOOGLE_PLAY("Google Play Store"),
    APPLE_APP_STORE("Apple App Store")
}

data class OfficialSocialAccount(
    val platform: PlatformType,
    val handle: String,
    val profileUrl: String,
    val verified: Boolean = true
)

data class OfficialMobileApp(
    val platform: AppStoreType,
    val packageName: String,
    val appTitle: String,
    val developerName: String,
    val storeUrl: String
)

data class BrandProfile(
    val id: String,
    val name: String,
    val aliases: List<String> = emptyList(),
    val logoDescription: String = "Official Hex Badge",
    val officialSocialAccounts: List<OfficialSocialAccount> = emptyList(),
    val officialMobileApps: List<OfficialMobileApp> = emptyList()
)
