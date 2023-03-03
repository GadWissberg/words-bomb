package com.gadarts.shubutz.core.business

interface GameLogicHandlerEventsSubscriber {
    fun onCorrectGuess(index: Int, gameWin: Boolean, coinsAmount: Int)
    fun onIncorrectGuess(gameOver: Boolean)

}