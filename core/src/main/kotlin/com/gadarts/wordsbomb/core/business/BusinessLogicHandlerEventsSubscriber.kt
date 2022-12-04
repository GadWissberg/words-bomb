package com.gadarts.wordsbomb.core.business

interface BusinessLogicHandlerEventsSubscriber {
    fun onHiddenLetterIndexRemoved(index: Int)
    fun onHiddenLetterIndexFailedToRemove()

}