package com.example.engine

import kotlin.math.max
import kotlin.math.min

data class LookalikeAnalysisResult(
    val candidate: String,
    val canonicalCandidate: String,
    val brandStem: String,
    val isLookalike: Boolean,
    val riskScore: Int, // 0 to 100
    val editDistance: Int,
    val detectedHomoglyphs: List<Pair<Char, Char>>,
    val detectedAffixes: List<String>,
    val detectionReasons: List<String>,
    val overflaggingProtectionTriggered: Boolean
)

object LookalikeDetector {

    // Common homoglyphs and visual confusables mapping to standard Latin lowercase
    private val HOMOGLYPH_MAP: Map<Char, Char> = mapOf(
        '0' to 'o',
        '1' to 'l',
        '!' to 'i',
        '|' to 'l',
        '3' to 'e',
        '4' to 'a',
        '@' to 'a',
        '5' to 's',
        '$' to 's',
        '7' to 't',
        '8' to 'b',
        '9' to 'g',
        // Cyrillic & Greek confusables commonly abused in brand impersonation
        'а' to 'a', // Cyrillic small letter a (U+0430)
        'с' to 'c', // Cyrillic small letter es (U+0441)
        'е' to 'e', // Cyrillic small letter ie (U+0435)
        'о' to 'o', // Cyrillic small letter o (U+043E)
        'р' to 'p', // Cyrillic small letter er (U+0440)
        'х' to 'x', // Cyrillic small letter ha (U+0445)
        'у' to 'y', // Cyrillic small letter u (U+0443)
        'і' to 'i', // Cyrillic small letter i (U+0456)
        'ѕ' to 's', // Cyrillic small letter dze (U+0455)
        'ј' to 'j'  // Cyrillic small letter je (U+0458)
    )

    private val SUSPICIOUS_AFFIXES = listOf(
        "support", "helpdesk", "help", "care", "security", "claims", "claim",
        "official", "verify", "verified", "real", "the_real", "app", "mobile",
        "portal", "login", "auth", "service", "airdrop", "giveaway", "free",
        "rewards", "bonus", "tech", "global", "group", "bank", "pay"
    )

    /**
     * Canonicalizes a string by replacing homoglyphs and stripping non-alphanumeric separators.
     */
    fun canonicalize(input: String): String {
        val lower = input.lowercase()
        val builder = StringBuilder()
        var i = 0
        while (i < lower.length) {
            // Check double-v for w
            if (i < lower.length - 1 && lower[i] == 'v' && lower[i + 1] == 'v') {
                builder.append('w')
                i += 2
                continue
            }
            val c = lower[i]
            val mapped = HOMOGLYPH_MAP[c] ?: c
            if (mapped.isLetterOrDigit()) {
                builder.append(mapped)
            }
            i++
        }
        return builder.toString()
    }

    /**
     * Identifies individual homoglyphs used in the candidate string.
     */
    fun findHomoglyphs(candidate: String): List<Pair<Char, Char>> {
        val list = mutableListOf<Pair<Char, Char>>()
        for (c in candidate.lowercase()) {
            val mapped = HOMOGLYPH_MAP[c]
            if (mapped != null && c != mapped) {
                list.add(c to mapped)
            }
        }
        return list
    }

    /**
     * Damerau-Levenshtein distance calculation (handles insertions, deletions, substitutions, and adjacent transpositions).
     */
    fun damerauLevenshteinDistance(s1: String, s2: String): Int {
        val len1 = s1.length
        val len2 = s2.length
        val d = Array(len1 + 1) { IntArray(len2 + 1) }

        for (i in 0..len1) d[i][0] = i
        for (j in 0..len2) d[0][j] = j

        for (i in 1..len1) {
            for (j in 1..len2) {
                val cost = if (s1[i - 1] == s2[j - 1]) 0 else 1
                d[i][j] = min(
                    d[i - 1][j] + 1, // deletion
                    min(
                        d[i][j - 1] + 1, // insertion
                        d[i - 1][j - 1] + cost // substitution
                    )
                )
                // Transposition
                if (i > 1 && j > 1 && s1[i - 1] == s2[j - 2] && s1[i - 2] == s2[j - 1]) {
                    d[i][j] = min(d[i][j], d[i - 2][j - 2] + 1)
                }
            }
        }
        return d[len1][len2]
    }

    /**
     * Evaluates a candidate name/handle against a legitimate brand name.
     */
    fun analyze(candidate: String, brandName: String): LookalikeAnalysisResult {
        val cleanCandidate = candidate.trim().removePrefix("@")
        val cleanBrand = brandName.trim().lowercase().filter { it.isLetterOrDigit() }
        val canonicalCand = canonicalize(cleanCandidate)
        val homoglyphs = findHomoglyphs(cleanCandidate)

        val reasons = mutableListOf<String>()
        val detectedAffixes = mutableListOf<String>()

        // 1. Exact match check (if candidate canonicalizes to brand name directly but had homoglyphs)
        val isExactCanonicalMatch = (canonicalCand == cleanBrand)

        // 2. Homoglyph swap detection
        if (homoglyphs.isNotEmpty()) {
            val hgDetails = homoglyphs.joinToString(", ") { "'${it.first}' -> '${it.second}'" }
            reasons.add("Homoglyph characters detected: $hgDetails")
        }

        // 3. Substring & Affix squatting detection
        var containsBrandStem = false
        if (canonicalCand.contains(cleanBrand)) {
            containsBrandStem = true
            reasons.add("Contains brand stem '$cleanBrand'")
            for (affix in SUSPICIOUS_AFFIXES) {
                if (canonicalCand.contains(affix)) {
                    detectedAffixes.add(affix)
                }
            }
            if (detectedAffixes.isNotEmpty()) {
                reasons.add("Abuses high-risk brand keywords: ${detectedAffixes.joinToString(", ")}")
            }
        }

        // 4. Edit distance calculation against brand
        val editDist = damerauLevenshteinDistance(canonicalCand, cleanBrand)
        val maxLen = max(canonicalCand.length, cleanBrand.length)
        val similarityRatio = if (maxLen > 0) 1.0 - (editDist.toDouble() / maxLen.toDouble()) else 0.0

        // 5. Over-flagging protection:
        // Do not flag words that are completely unrelated or too short, or where similarity is low and no brand stem is present.
        val overflaggingProtectionTriggered = if (cleanBrand.length >= 4) {
            !containsBrandStem && editDist > 2 && similarityRatio < 0.65
        } else {
            !containsBrandStem && editDist > 1
        }

        // 6. Risk Scoring
        var score = 0
        if (!overflaggingProtectionTriggered) {
            if (isExactCanonicalMatch && homoglyphs.isNotEmpty()) {
                // High confidence homoglyph spoofing (e.g. Apex -> Ap3x, Acme -> Acm3)
                score += 85
                reasons.add("Direct homoglyph brand impersonation")
            } else if (containsBrandStem) {
                score += 50
                if (detectedAffixes.isNotEmpty()) {
                    score += 35
                }
                if (homoglyphs.isNotEmpty()) {
                    score += 20
                }
            } else if (editDist in 1..2 && cleanBrand.length >= 4) {
                // Typo-squatting (e.g. amce for acme, aplpex for apex)
                score += 65
                reasons.add("Typosquatting detected (Edit distance: $editDist)")
                if (homoglyphs.isNotEmpty()) {
                    score += 25
                }
            } else if (similarityRatio >= 0.75) {
                score += 45
                reasons.add("High phonetic/character similarity (${(similarityRatio * 100).toInt()}%)")
            }
        }

        score = min(score, 100)
        val isLookalike = (score >= 40) && !overflaggingProtectionTriggered

        return LookalikeAnalysisResult(
            candidate = candidate,
            canonicalCandidate = canonicalCand,
            brandStem = cleanBrand,
            isLookalike = isLookalike,
            riskScore = score,
            editDistance = editDist,
            detectedHomoglyphs = homoglyphs,
            detectedAffixes = detectedAffixes,
            detectionReasons = reasons,
            overflaggingProtectionTriggered = overflaggingProtectionTriggered
        )
    }
}
