package com.gadarts.shubutz.core

interface GameLifeCycleManager {
    var loadingDone: Boolean

    fun goToMenu()
    fun goToPlayScreen()

}
