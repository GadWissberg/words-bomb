package com.gadarts.shubutz.core.screens.game.view

interface GamePlayScreenViewEventsSubscriber {
    fun onBrickClicked(letter: Char)
    fun onScreenEmpty()
    fun onClickedBackButton()

}
