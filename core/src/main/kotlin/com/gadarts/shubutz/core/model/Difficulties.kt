package com.gadarts.shubutz.core.model

enum class Difficulties(
    val displayName: String? = null,
    val lettersToHideFactor: Float,
    val tries: Int,
    val winWorth: Int,
    val minimumLength: Int = 2,
    val phrasesFileName: String = "phrases",
    val sharedPreferencesCoinsKey: String = "regular_coins",
    val revealLetterCost: Int = 8,
    val perfectBonusEnabled: Boolean = true
) {


    BEGINNER(
        displayName = "מתחיל",
        lettersToHideFactor = 0.5F,
        tries = 6,
        winWorth = 1,
        perfectBonusEnabled = false
    ),
    INTERMEDIATE("בינוני", 0.8F, 5, 2, 4),
    ADVANCED("מתקדם", 0.9F, 4, 3, 5),
    EXPERT("מומחה", 1F, 3, 4, 6),
    KIDS(
        lettersToHideFactor = 0.6F,
        tries = 6,
        winWorth = 1,
        phrasesFileName = "phrases_kids",
        sharedPreferencesCoinsKey = "kids_coins",
        revealLetterCost = 4
    );

}
