package com.gadarts.shubutz.core

import com.gadarts.shubutz.core.model.GameModes

interface GameLifeCycleManager {
    var loadingDone: Boolean

    fun goToMenu()
    fun goToPlayScreen(selectedDifficulty: GameModes)
    fun onSuccessfulPurchase(products: MutableList<String>)
    fun onFailedPurchase(message: String)
    fun onLeaderboardClosed()
    fun restart()

}
