package com.gadarts.shubutz.core.business

interface BusinessLogicHandlerEventsSubscriber {
    fun onCorrectGuess(indices: List<Int>, gameWin: Boolean)
    fun onIncorrectGuess(gameOver: Boolean)

}