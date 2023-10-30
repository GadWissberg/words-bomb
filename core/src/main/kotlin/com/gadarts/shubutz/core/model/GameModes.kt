package com.gadarts.shubutz.core.model

interface GameModes {
    fun getGame(): Games
    fun getSharedPrefCoinsKey(): String
    fun getContentFileName(): String?
    fun getModeDisplayName(): String
    fun getHighscoresId(): String?
    fun getNumberOfTries(): Int
    fun getPhraseMinimumLength(): Int
    fun getWinWorthCoins(): Int
    fun getPerfectBonusAllowed(): Boolean
    fun getHideFactor(): Float
    fun getRevealSingleLetterCost(): Int

}
