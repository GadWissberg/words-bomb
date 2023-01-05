package com.gadarts.shubutz.core

import com.gadarts.shubutz.core.model.Difficulties

interface GameLifeCycleManager {
    var loadingDone: Boolean

    fun goToMenu()
    fun goToPlayScreen(selectedDifficulty: Difficulties)

}
