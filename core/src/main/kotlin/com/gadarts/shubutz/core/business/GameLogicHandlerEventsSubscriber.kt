package com.gadarts.shubutz.core.business

interface GameLogicHandlerEventsSubscriber {
    fun onCorrectGuess(indices: List<Int>, gameWin: Boolean, coinsAmount: Int)
    fun onIncorrectGuess(gameOver: Boolean)

}