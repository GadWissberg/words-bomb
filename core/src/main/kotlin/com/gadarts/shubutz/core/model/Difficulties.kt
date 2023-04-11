package com.gadarts.shubutz.core.model

enum class Difficulties(
    val displayName: String? = null,
    val lettersToHideFactor: Float,
    val tries: Int,
    val winWorth: Int,
    val minimumLength: Int = 2,
    val phrasesFileName: String = "phrases"
) {


    BEGINNER("מתחיל", 0.5F, 6, 1),
    INTERMEDIATE("בינוני", 0.8F, 5, 2, 4),
    ADVANCED("מתקדם", 0.9F, 4, 3, 5),
    EXPERT("מומחה", 1F, 3, 4, 6),
    KIDS(lettersToHideFactor = 0.6F, tries = 6, winWorth = 1, phrasesFileName = "phrases_kids");

}
