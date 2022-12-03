package com.gadarts.wordsbomb.core.screens.menu.view

import com.gadarts.wordsbomb.core.view.hud.EventsSubscriber

interface MenuScreenViewEventsSubscriber : EventsSubscriber {
    fun onLoadingAnimationReady()

}