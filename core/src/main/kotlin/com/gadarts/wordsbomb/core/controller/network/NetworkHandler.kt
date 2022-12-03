package com.gadarts.wordsbomb.core.controller.network

import com.badlogic.gdx.utils.Disposable
import com.gadarts.wordsbomb.core.AndroidInterface
import com.gadarts.wordsbomb.core.view.Brick
import com.gadarts.wordsbomb.core.view.hud.SubmissionResult
import java.util.ArrayList
import java.util.HashSet

interface NetworkHandler : Disposable {
    fun getSocketManager(): SocketManager
    fun initialize()
    fun close()
    fun act(androidInterface: AndroidInterface, commandsListener: CommandsListener)
    fun applySelectedLetters(draftBricks: HashSet<Brick>)
    fun sendForValidationAndFetchResults(
        results: ArrayList<SubmissionResult>,
        androidInterface: AndroidInterface
    ): Boolean

    fun informLetterChangedInBoard(
        letter: Int,
        brick: Brick,
        removed: Boolean = false
    )

    fun fetchNewLetters(newLetters: ArrayList<Char>)
    fun fetchUpdatedScore(): Int
    fun reset()

}
