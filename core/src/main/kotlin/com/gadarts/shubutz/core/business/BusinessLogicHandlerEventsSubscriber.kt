package com.gadarts.shubutz.core.business

interface BusinessLogicHandlerEventsSubscriber {
    fun onGuessSuccess(indices: List<Int>, gameWin: Boolean)
    fun onGuessFail(gameOver: Boolean)

}