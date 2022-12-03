package com.gadarts.wordsbomb.core.controller.network

import com.gadarts.wordsbomb.core.AndroidInterface
import com.gadarts.wordsbomb.core.view.Brick
import com.gadarts.wordsbomb.core.view.hud.SubmissionResult
import java.util.ArrayList
import java.util.HashSet

class EmptyNetworkHandler : NetworkHandler {
    private val socketManager = SocketManager()

    override fun getSocketManager(): SocketManager {
        return socketManager
    }

    override fun initialize() {
    }

    override fun close() {
    }

    override fun act(androidInterface: AndroidInterface, commandsListener: CommandsListener) {
    }

    override fun applySelectedLetters(draftBricks: HashSet<Brick>) {
    }

    override fun sendForValidationAndFetchResults(
        results: ArrayList<SubmissionResult>,
        androidInterface: AndroidInterface
    ): Boolean {
        return true
    }

    override fun informLetterChangedInBoard(letter: Int, brick: Brick, removed: Boolean) {
    }

    override fun fetchNewLetters(newLetters: ArrayList<Char>) {
    }

    override fun fetchUpdatedScore(): Int {
        return 0
    }

    override fun reset() {
    }

    override fun dispose() {
    }

}
