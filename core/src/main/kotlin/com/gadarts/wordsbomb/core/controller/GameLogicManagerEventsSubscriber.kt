package com.gadarts.wordsbomb.core.controller

import com.gadarts.wordsbomb.core.model.Player
import com.gadarts.wordsbomb.core.view.Brick
import com.gadarts.wordsbomb.core.view.hud.EventsSubscriber
import kotlin.collections.ArrayList
import kotlin.collections.HashSet

interface GameLogicManagerEventsSubscriber : EventsSubscriber {
    fun onNewLettersFetched(newLetters: ArrayList<Char>)
    fun onGameBegin(player: Player, playersNames: List<String>)
    fun onPlayerFinishedTurn(draftBricks: HashSet<Brick>, success: Boolean)
    fun onRivalChangedBrickOnBoard(letter: Int, row: Int, col: Int, draftBricks: HashSet<Brick>)
    fun onMyTurn()
    fun onScoreUpdated(updatedScore: Int)
    fun onCashierUpdated(cashierSize: Int)
    fun onGameFinished()

}