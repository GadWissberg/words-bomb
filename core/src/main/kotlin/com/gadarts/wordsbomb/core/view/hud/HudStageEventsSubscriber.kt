package com.gadarts.wordsbomb.core.view.hud

import com.badlogic.gdx.scenes.scene2d.ui.Image
import com.gadarts.wordsbomb.core.view.Brick

interface HudStageEventsSubscriber : EventsSubscriber {
    fun onBrickDropped(screenX: Int, screenY: Int)
    fun onGoRequest()
    fun onPlacedBrickReturnedBackToDeck(brickToReturn: Brick)
    fun onHudStageInitializedForGame(bottomHudImage: Image)

}