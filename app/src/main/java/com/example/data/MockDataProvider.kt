package com.example.data

import com.example.model.*

object MockDataProvider {

    val APEX_FINANCIAL = BrandProfile(
        id = "brand_apex",
        name = "Apex Financial",
        aliases = listOf("ApexBank", "ApexFin", "ApexPay"),
        logoDescription = "Electric Blue Shield with Chevron",
        officialSocialAccounts = listOf(
            OfficialSocialAccount(
                platform = PlatformType.TWITTER,
                handle = "@ApexFinancial",
                profileUrl = "https://x.com/ApexFinancial",
                verified = true
            ),
            OfficialSocialAccount(
                platform = PlatformType.LINKEDIN,
                handle = "apex-financial",
                profileUrl = "https://linkedin.com/company/apex-financial",
                verified = true
            ),
            OfficialSocialAccount(
                platform = PlatformType.INSTAGRAM,
                handle = "@apexfinancial_official",
                profileUrl = "https://instagram.com/apexfinancial_official",
                verified = true
            ),
            OfficialSocialAccount(
                platform = PlatformType.FACEBOOK,
                handle = "ApexFinancialGroup",
                profileUrl = "https://facebook.com/ApexFinancialGroup",
                verified = true
            )
        ),
        officialMobileApps = listOf(
            OfficialMobileApp(
                platform = AppStoreType.GOOGLE_PLAY,
                packageName = "com.apex.mobilebanking",
                appTitle = "Apex Mobile Banking",
                developerName = "Apex Financial Group",
                storeUrl = "https://play.google.com/store/apps/details?id=com.apex.mobilebanking"
            ),
            OfficialMobileApp(
                platform = AppStoreType.APPLE_APP_STORE,
                packageName = "com.apex.iosbanking",
                appTitle = "Apex Digital Banking",
                developerName = "Apex Financial Group",
                storeUrl = "https://apps.apple.com/app/apex-digital-banking/id109283746"
            )
        )
    )

    val CLOUD_VAULT = BrandProfile(
        id = "brand_cloudvault",
        name = "CloudVault",
        aliases = listOf("CloudVaultSec", "VaultCloud"),
        logoDescription = "Cyan Hexagonal Lock",
        officialSocialAccounts = listOf(
            OfficialSocialAccount(
                platform = PlatformType.TWITTER,
                handle = "@CloudVault",
                profileUrl = "https://x.com/CloudVault",
                verified = true
            ),
            OfficialSocialAccount(
                platform = PlatformType.LINKEDIN,
                handle = "cloudvault-tech",
                profileUrl = "https://linkedin.com/company/cloudvault-tech",
                verified = true
            )
        ),
        officialMobileApps = listOf(
            OfficialMobileApp(
                platform = AppStoreType.GOOGLE_PLAY,
                packageName = "com.cloudvault.security",
                appTitle = "CloudVault Drive & Key",
                developerName = "CloudVault Technologies Inc.",
                storeUrl = "https://play.google.com/store/apps/details?id=com.cloudvault.security"
            )
        )
    )

    val AEROPAY = BrandProfile(
        id = "brand_aeropay",
        name = "AeroPay",
        aliases = listOf("AeroPayWallet", "AeroPayments"),
        logoDescription = "Teal Supersonic Arrow",
        officialSocialAccounts = listOf(
            OfficialSocialAccount(
                platform = PlatformType.TWITTER,
                handle = "@AeroPay",
                profileUrl = "https://x.com/AeroPay",
                verified = true
            )
        ),
        officialMobileApps = listOf(
            OfficialMobileApp(
                platform = AppStoreType.GOOGLE_PLAY,
                packageName = "com.aeropay.global",
                appTitle = "AeroPay Quick Wallet",
                developerName = "AeroPay Global Ltd.",
                storeUrl = "https://play.google.com/store/apps/details?id=com.aeropay.global"
            )
        )
    )

    fun getInitialMonitoredAssets(): List<MonitoredAsset> {
        return listOf(
            // -------------------------------------------------------------
            // 1. LEGITIMATE OFFICIAL ASSETS (MUST BE EXCLUDED BY CRITICAL RULE)
            // -------------------------------------------------------------
            MonitoredAsset(
                id = "asset_official_twitter",
                type = AssetType.SOCIAL_PROFILE,
                platformName = "X / Twitter",
                titleOrHandle = "@ApexFinancial",
                displayName = "Apex Financial",
                publisherOrBio = "Official corporate account of Apex Financial Group. Member FDIC.",
                followersOrDownloads = "284K followers",
                isOfficialAsset = true,
                externalUrl = "https://x.com/ApexFinancial"
            ),
            MonitoredAsset(
                id = "asset_official_app",
                type = AssetType.MOBILE_APP,
                platformName = "Google Play Store",
                titleOrHandle = "com.apex.mobilebanking",
                displayName = "Apex Mobile Banking",
                publisherOrBio = "Apex Financial Group",
                followersOrDownloads = "1M+ downloads",
                isOfficialAsset = true,
                externalUrl = "https://play.google.com/store/apps/details?id=com.apex.mobilebanking"
            ),
            MonitoredAsset(
                id = "asset_official_instagram",
                type = AssetType.SOCIAL_PROFILE,
                platformName = "Instagram",
                titleOrHandle = "@apexfinancial_official",
                displayName = "Apex Financial Official",
                publisherOrBio = "Real financial intelligence for the modern economy. Official profile.",
                followersOrDownloads = "142K followers",
                isOfficialAsset = true,
                externalUrl = "https://instagram.com/apexfinancial_official"
            ),

            // -------------------------------------------------------------
            // 2. CRITICAL & HIGH THREATS (LOOK-ALIKES, HOMOGLYPHS, SCAMS)
            // -------------------------------------------------------------
            MonitoredAsset(
                id = "threat_x_homoglyph_support",
                type = AssetType.SOCIAL_PROFILE,
                platformName = "X / Twitter",
                titleOrHandle = "@Apex_Financia1_Support",
                displayName = "Apex Financial Support ✔️",
                publisherOrBio = "24/7 Priority Resolution Desk. Having issues with transfers? DM us for urgent account unlock.",
                followersOrDownloads = "64 followers",
                logoLikenessScore = 92,
                externalUrl = "https://x.com/Apex_Financia1_Support"
            ),
            MonitoredAsset(
                id = "threat_app_publisher_mismatch",
                type = AssetType.MOBILE_APP,
                platformName = "Google Play Store",
                titleOrHandle = "com.apexbanking.quickloan",
                displayName = "Apex Online Banking Pro",
                publisherOrBio = "Apex FinTech LTD (Unverified)",
                followersOrDownloads = "5,000+ downloads",
                logoLikenessScore = 88,
                externalUrl = "https://play.google.com/store/apps/details?id=com.apexbanking.quickloan"
            ),
            MonitoredAsset(
                id = "threat_x_zero_prefix",
                type = AssetType.SOCIAL_PROFILE,
                platformName = "X / Twitter",
                titleOrHandle = "@0fficial_ApexFinancial",
                displayName = "Apex Financial Corporate",
                publisherOrBio = "Claim your exclusive Q4 loyalty dividends at: bit.ly/apex-claims-portal",
                followersOrDownloads = "120 followers",
                logoLikenessScore = 80,
                externalUrl = "https://x.com/0fficial_ApexFinancial"
            ),
            MonitoredAsset(
                id = "threat_insta_airdrop_scam",
                type = AssetType.SOCIAL_PROFILE,
                platformName = "Instagram",
                titleOrHandle = "@apexfin_airdrop",
                displayName = "ApexPay Rewards Desk",
                publisherOrBio = "Official Apex community giveaway! Connect wallet to claim 500 APEX tokens immediately.",
                followersOrDownloads = "890 followers",
                logoLikenessScore = 75,
                externalUrl = "https://instagram.com/apexfin_airdrop"
            ),
            MonitoredAsset(
                id = "threat_linkedin_fake_company",
                type = AssetType.SOCIAL_PROFILE,
                platformName = "LinkedIn",
                titleOrHandle = "apex-financial-recruitment-team",
                displayName = "Apex Financial Careers & Verification",
                publisherOrBio = "HR Onboarding Portal. Telegram interview bot contact: @apex_hr_agent",
                followersOrDownloads = "32 connections",
                logoLikenessScore = 65,
                externalUrl = "https://linkedin.com/company/apex-financial-recruitment-team"
            ),
            MonitoredAsset(
                id = "threat_app_fake_crypto_wallet",
                type = AssetType.MOBILE_APP,
                platformName = "Google Play Store",
                titleOrHandle = "com.apexpay.wallet.crypto",
                displayName = "ApexPay Fast Transfer",
                publisherOrBio = "Apex Global Developers",
                followersOrDownloads = "1,200 downloads",
                logoLikenessScore = 82,
                externalUrl = "https://play.google.com/store/apps/details?id=com.apexpay.wallet.crypto"
            ),

            // -------------------------------------------------------------
            // 3. BENIGN / NON-TARGET ENTITIES (TO VERIFY OVER-FLAGGING GUARD)
            // -------------------------------------------------------------
            MonitoredAsset(
                id = "benign_apex_legends",
                type = AssetType.SOCIAL_PROFILE,
                platformName = "X / Twitter",
                titleOrHandle = "@ApexLegendsDaily",
                displayName = "Apex Legends Daily News",
                publisherOrBio = "Community fan account covering gaming tournaments and patch notes.",
                followersOrDownloads = "520K followers",
                logoLikenessScore = 10,
                externalUrl = "https://x.com/ApexLegendsDaily"
            ),
            MonitoredAsset(
                id = "benign_apex_climbing",
                type = AssetType.MOBILE_APP,
                platformName = "Google Play Store",
                titleOrHandle = "com.apexclimb.mountaintracker",
                displayName = "Apex Mountain Altitude Tracker",
                publisherOrBio = "Outdoor Navigation Software",
                followersOrDownloads = "50K downloads",
                logoLikenessScore = 5,
                externalUrl = "https://play.google.com/store/apps/details?id=com.apexclimb.mountaintracker"
            )
        )
    }
}
