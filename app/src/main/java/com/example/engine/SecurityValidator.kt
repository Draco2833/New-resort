package com.example.engine

import com.example.model.SecurityValidationResult
import java.util.regex.Pattern
import kotlin.math.ln

object SecurityValidator {

    // Mandatory UI Error String required by specification
    const val ERROR_MESSAGE = "Error: Invalid, corrupted, or spam input detected."

    // Known spam regex patterns
    private val SPAM_PATTERNS = listOf(
        Pattern.compile("(?i)(send|transfer|deposit)\\s+[0-9.]+\\s*(eth|btc|usdt|sol).*?(receive|get|earn)\\s+[0-9.]+", Pattern.DOTALL),
        Pattern.compile("(?i)(crypto|airdrop|token)\\s+(giveaway|claim|drop|bonus).*?(connect\\s+wallet|free\\s+mint)", Pattern.DOTALL),
        Pattern.compile("(?i)(dm|contact|message)\\s+(@[a-z0-9_]+|[a-z0-9_]+)\\s+on\\s+(instagram|telegram|whatsapp).*?(recover|unlock|hack)", Pattern.DOTALL),
        Pattern.compile("(?i)(urgent|alert).*?(account\\s+suspended|verify\\s+identity|unauthorized\\s+login).*?(click\\s+here|http|bit\\.ly)", Pattern.DOTALL),
        Pattern.compile("(?i)hxxp[s]?://", Pattern.DOTALL),
        Pattern.compile("(?i)(seed\\s+phrase|private\\s+key|wallet\\s+phrase)", Pattern.DOTALL)
    )

    // Code injection signatures
    private val INJECTION_PATTERNS = listOf(
        Pattern.compile("(?i)<script.*?>.*?</script>", Pattern.DOTALL),
        Pattern.compile("(?i)('|\")\\s*(or|and)\\s*('|\")?1('|\")?\\s*=\\s*('|\")?1", Pattern.DOTALL),
        Pattern.compile("(?i)union\\s+select", Pattern.DOTALL),
        Pattern.compile("(?i)\\$\\{jndi:(ldap|rmi|dns):", Pattern.DOTALL),
        Pattern.compile("(?i)(exec\\(|system\\(|cmd\\.exe|powershell)", Pattern.DOTALL)
    )

    // Base64 block detector (long unspaced base64 chunks or base64 padding)
    private val BASE64_CHUNK_PATTERN = Pattern.compile("^[A-Za-z0-9+/]{24,}={0,2}$")

    /**
     * Calculates Shannon entropy of a given text.
     * High entropy (> 4.5) indicates encrypted data, cryptographic keys, or random gibberish.
     */
    fun calculateShannonEntropy(text: String): Double {
        if (text.isEmpty()) return 0.0
        val frequencyMap = HashMap<Char, Int>()
        for (c in text) {
            frequencyMap[c] = (frequencyMap[c] ?: 0) + 1
        }
        val len = text.length.toDouble()
        var entropy = 0.0
        val ln2 = ln(2.0)
        for (count in frequencyMap.values) {
            val prob = count / len
            entropy -= prob * (ln(prob) / ln2)
        }
        return entropy
    }

    /**
     * Validates input string against corrupted strings, high-entropy ciphertext,
     * base64 abuse, code injection, and spam message patterns.
     */
    fun validate(input: String?): SecurityValidationResult {
        if (input.isNullOrBlank()) {
            return SecurityValidationResult.Valid("")
        }

        val trimmed = input.trim()

        // 1. Check for control / binary / corrupted non-printable characters
        for (c in trimmed) {
            val code = c.code
            if ((code in 0..8) || (code == 11) || (code in 14..31) || code == 127) {
                return SecurityValidationResult.Invalid(
                    errorMessage = ERROR_MESSAGE,
                    violationType = "CORRUPTED_BINARY_DATA",
                    technicalDetails = "Non-printable control characters or binary bytes detected (char code: $code).",
                    blockedPayloadPreview = sanitizeForPreview(trimmed)
                )
            }
        }

        // 2. Base64 abuse / corrupted block detection
        val words = trimmed.split(Regex("\\s+"))
        for (w in words) {
            if (w.length >= 28 && BASE64_CHUNK_PATTERN.matcher(w).matches()) {
                val entropy = calculateShannonEntropy(w)
                if (entropy > 4.2) {
                    return SecurityValidationResult.Invalid(
                        errorMessage = ERROR_MESSAGE,
                        violationType = "BASE64_PAYLOAD_ABUSE",
                        technicalDetails = "Raw base64 payload / encrypted blob block detected (Length: ${w.length}, Entropy: ${"%.2f".format(entropy)}).",
                        blockedPayloadPreview = sanitizeForPreview(w)
                    )
                }
            }
        }

        // 3. Shannon Entropy Check for encrypted strings / high-entropy gibberish
        if (trimmed.length >= 20) {
            val entropy = calculateShannonEntropy(trimmed)
            val isHexHash = trimmed.length >= 32 && trimmed.matches(Regex("^[0-9a-fA-F]+$"))
            // Normal human words in English sentences have lower entropy.
            // Ciphertext, hashes, and compressed/encrypted strings have high entropy or raw hash patterns.
            if ((entropy > 4.2 || isHexHash) && !trimmed.contains(" ")) {
                return SecurityValidationResult.Invalid(
                    errorMessage = ERROR_MESSAGE,
                    violationType = "HIGH_ENTROPY_CIPHERTEXT",
                    technicalDetails = "Unreadable ciphertext / encrypted string detected (Entropy: ${"%.2f".format(entropy)} bits/byte exceeds safety threshold).",
                    blockedPayloadPreview = sanitizeForPreview(trimmed)
                )
            }
        }

        // 4. Injection signatures (XSS, SQLi, JNDI)
        for (pattern in INJECTION_PATTERNS) {
            if (pattern.matcher(trimmed).find()) {
                return SecurityValidationResult.Invalid(
                    errorMessage = ERROR_MESSAGE,
                    violationType = "SECURITY_INJECTION_SIGNATURE",
                    technicalDetails = "Malicious code or injection syntax blocked by WAF rule.",
                    blockedPayloadPreview = sanitizeForPreview(trimmed)
                )
            }
        }

        // 5. Spam / Scam message heuristics
        for (pattern in SPAM_PATTERNS) {
            if (pattern.matcher(trimmed).find()) {
                return SecurityValidationResult.Invalid(
                    errorMessage = ERROR_MESSAGE,
                    violationType = "SPAM_SCAM_HEURISTIC",
                    technicalDetails = "Matches known brand impersonation, crypto scam, or illicit account recovery spam signature.",
                    blockedPayloadPreview = sanitizeForPreview(trimmed)
                )
            }
        }

        return SecurityValidationResult.Valid(trimmed)
    }

    private fun sanitizeForPreview(input: String): String {
        return if (input.length > 80) input.take(77) + "..." else input
    }
}
