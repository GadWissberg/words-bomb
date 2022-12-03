package com.gadarts.wordsbomb.core.view.board

import com.gadarts.wordsbomb.core.view.Brick
import com.gadarts.wordsbomb.core.view.hud.EventsSubscriber

/**
 * Gets notified for the Board Stage events.
 */
interface BoardStageEventsSubscriber : EventsSubscriber {

    /**
     * Called when a brick is taken out of a board's cell.
     */
    fun onPlacedBrickTakenOutOfBoard(brickToReturn: Brick)

    /**
     * Called when a touch occurs on a brick actor inside a cell.
     */
    fun onPlacedBrickTouched(brick: Brick)

    /**
     * Called when a brick actor is placed in a cell.
     */
    fun onBrickPlacedInBoard(brick: Brick)

    /**
     * Called when a brick is taken out of a cell and placed inside another cell.
     */
    fun onPlacedBrickMovedToAnotherCell(oldCell: BoardCell?, newCell: BoardCell, brick: Brick)

}
