package com.gadarts.shubutz.core.model

enum class BombGameModes(
    val displayName: String,
    val lettersToHideFactor: Float,
    val tries: Int,
    val winWorth: Int,
    val minimumLength: Int = 2,
    val phrasesFileName: String? = "phrases",
    val sharedPreferencesCoinsKey: String = "regular_coins",
    val revealLetterCost: Int = 9,
    val perfectBonusEnabled: Boolean = true,
    val leaderboardsId: String? = null
) : GameModes {


    BEGINNER(
        displayName = "מתחיל",
        lettersToHideFactor = 0.5F,
        tries = 6,
        winWorth = 1,
        perfectBonusEnabled = false,
        leaderboardsId = "CgkIwtDryJ4YEAIQAQ"
    ),
    INTERMEDIATE(
        displayName = "בינוני",
        lettersToHideFactor = 0.8F,
        tries = 5,
        winWorth = 2,
        minimumLength = 4,
        leaderboardsId = "CgkIwtDryJ4YEAIQAw"
    ),
    ADVANCED(
        displayName = "מתקדם",
        lettersToHideFactor = 0.9F,
        tries = 4,
        winWorth = 3,
        minimumLength = 5,
        leaderboardsId = "CgkIwtDryJ4YEAIQBA"
    ),
    EXPERT(
        displayName = "מומחה",
        lettersToHideFactor = 1F,
        tries = 3,
        winWorth = 4,
        minimumLength = 6,
        leaderboardsId = "CgkIwtDryJ4YEAIQBQ"
    ),
    KIDS(
        displayName = "ילדים",
        lettersToHideFactor = 0.6F,
        tries = 6,
        winWorth = 1,
        phrasesFileName = "phrases_kids",
        sharedPreferencesCoinsKey = "kids_coins",
        revealLetterCost = 5,
        leaderboardsId = "CgkIwtDryJ4YEAIQBg"
    );

    override fun getSharedPrefCoinsKey(): String {
        return sharedPreferencesCoinsKey
    }

    override fun getContentFileName(): String? {
        return phrasesFileName
    }

    override fun getModeDisplayName(): String {
        return displayName
    }

    override fun getHighscoresId(): String? {
        return leaderboardsId
    }

    override fun getNumberOfTries(): Int {
        return tries
    }

    override fun getPhraseMinimumLength(): Int {
        return minimumLength
    }

    override fun getWinWorthCoins(): Int {
        return winWorth
    }

    override fun getPerfectBonusAllowed(): Boolean {
        return perfectBonusEnabled
    }

    override fun getHideFactor(): Float {
        return lettersToHideFactor
    }

    override fun getRevealSingleLetterCost(): Int {
        return revealLetterCost
    }

    override fun getGame(): Games {
        return Games.BOMB
    }

}
