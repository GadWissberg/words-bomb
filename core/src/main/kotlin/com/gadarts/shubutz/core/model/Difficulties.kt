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
    val perfectBonusEnabled: Boolean = true,
    val leaderboardsId: String
) {


    BEGINNER(
        displayName = "מתחיל",
        lettersToHideFactor = 0.5F,
        tries = 6,
        winWorth = 1,
        perfectBonusEnabled = false,
        leaderboardsId = "CgkItLjwycoZEAIQBA"
    ),
    INTERMEDIATE(
        displayName = "בינוני",
        lettersToHideFactor = 0.8F,
        tries = 5,
        winWorth = 2,
        minimumLength = 4,
        leaderboardsId = "CgkItLjwycoZEAIQAw"
    ),
    ADVANCED(
        displayName = "מתקדם",
        lettersToHideFactor = 0.9F,
        tries = 4,
        winWorth = 3,
        minimumLength = 5,
        leaderboardsId = "CgkItLjwycoZEAIQAg"
    ),
    EXPERT(
        displayName = "מומחה",
        lettersToHideFactor = 1F,
        tries = 3,
        winWorth = 4,
        minimumLength = 6,
        leaderboardsId = "CgkItLjwycoZEAIQAA"
    ),
    KIDS(
        lettersToHideFactor = 0.6F,
        tries = 6,
        winWorth = 1,
        phrasesFileName = "phrases_kids",
        sharedPreferencesCoinsKey = "kids_coins",
        revealLetterCost = 4,
        leaderboardsId = "CgkItLjwycoZEAIQBQ"
    );

}
