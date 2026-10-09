package com.example.model

sealed class SecurityValidationResult {
    data class Valid(val sanitizedText: String) : SecurityValidationResult()
    data class Invalid(
        val errorMessage: String,
        val violationType: String,
        val technicalDetails: String,
        val blockedPayloadPreview: String
    ) : SecurityValidationResult()
}
