package com.gadarts.shubutz.core.screens.game

interface GamePlayScreen {
    fun onGameOverAnimationDone()
    fun onClickedBackButton()
    fun onBrickClicked(letter: Char)
    fun onScreenEmpty()

}
