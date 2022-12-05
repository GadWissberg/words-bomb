package com.gadarts.wordsbomb.core.business

interface BusinessLogicHandlerEventsSubscriber {
    fun onGuessSuccess(index: Int, gameWin: Boolean)
    fun onGuessFail(gameOver: Boolean)

}