package com.gadarts.shubutz.core.screens.menu

import com.gadarts.shubutz.core.model.Difficulties

interface BeginGameAction {
    fun begin(selectedDifficulty: Difficulties)
}
