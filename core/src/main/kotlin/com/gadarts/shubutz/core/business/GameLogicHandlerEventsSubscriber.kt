package com.gadarts.shubutz.core.business

interface GameLogicHandlerEventsSubscriber {
    fun onCorrectGuess(indices: List<Int>, gameWin: Boolean)
    fun onIncorrectGuess(gameOver: Boolean)

}