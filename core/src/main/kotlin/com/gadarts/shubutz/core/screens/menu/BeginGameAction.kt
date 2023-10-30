package com.gadarts.shubutz.core.screens.menu

import com.gadarts.shubutz.core.model.BombGameModes

interface BeginGameAction {
    fun beginBombGame(selectedDifficulty: BombGameModes)
    fun beginWordleGame()
}
