package com.example.model

data class InterceptedNotification(
    val id: String,
    val packageName: String,
    val appName: String,
    val title: String,
    val bodyText: String,
    val timestamp: Long = System.currentTimeMillis(),
    val isThreat: Boolean = false,
    val riskScore: Int = 0,
    val threatCategory: String = "BENIGN",
    val evidence: String = "",
    val isOfficialAsset: Boolean = false
)
