package com.gadarts.shubutz.core.screens.menu

import com.gadarts.shubutz.core.model.GameModes

interface BeginGameAction {
    fun begin(selectedDifficulty: GameModes)
}
