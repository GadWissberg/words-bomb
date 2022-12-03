package com.gadarts.wordsbomb.core.controller

import com.badlogic.gdx.scenes.scene2d.ui.Image
import com.badlogic.gdx.utils.Disposable
import com.gadarts.wordsbomb.core.AndroidInterface
import com.gadarts.wordsbomb.core.DefaultGameSettings
import com.gadarts.wordsbomb.core.Notifier
import com.gadarts.wordsbomb.core.model.Player
import com.gadarts.wordsbomb.core.model.Room
import com.gadarts.wordsbomb.core.controller.network.CommandsListener
import com.gadarts.wordsbomb.core.controller.network.NetworkHandler
import com.gadarts.wordsbomb.core.view.Brick
import com.gadarts.wordsbomb.core.view.board.BoardCell
import com.gadarts.wordsbomb.core.view.board.BoardStageEventsSubscriber
import com.gadarts.wordsbomb.core.view.hud.HudStageEventsSubscriber
import com.gadarts.wordsbomb.core.view.hud.SubmissionResult

/**
 * Handles game behavior.
 */
class GameController(
    private val networkHandler: NetworkHandler,
    private val draftBricks: HashSet<Brick>,
    private val androidInterface: AndroidInterface,
    private val playerName: String,
) :
    Disposable,
    HudStageEventsSubscriber, BoardStageEventsSubscriber,
    Notifier<GameLogicManagerEventsSubscriber>, CommandsListener {


    /**
     * Handles turns of player and ghost players.
     */
    val turnsManager = TurnsManager()
    private lateinit var board: Array<IntArray>
    private lateinit var player: Player
    private var gameInProgress: Boolean = false
    override val subscribers = HashSet<GameLogicManagerEventsSubscriber>()

    fun act() {
        networkHandler.act(androidInterface, this)
    }

    private fun beginGame(
        cashierSize: Int,
        letters: ArrayList<Char>,
        playersNames: List<String>
    ) {
        gameInProgress = true
        player = Player(letters, playerName)
        board = Array(Room.NUMBER_OF_CELLS_IN_ROW) { IntArray(Room.NUMBER_OF_CELLS_IN_ROW) { -1 } }
        subscribers.forEach { it.onGameBegin(player, playersNames) }
        subscribers.forEach { it.onCashierUpdated(cashierSize) }
    }

    override fun onCommandLetterPlaced(letter: Int, row: Int, col: Int) {
        if (letter >= 0) {
            board[row][col] = letter
        } else {
            board[row][col] = -1
        }
        subscribers.forEach { it.onRivalChangedBrickOnBoard(letter, row, col, draftBricks) }
    }

    override fun onCommandUpdateCashier(cashierSize: Int) {
        subscribers.forEach { it.onCashierUpdated(cashierSize) }
    }

    override fun onCommandGameOver(winners: java.util.ArrayList<Int>) {
        androidInterface.toast("WINNER:${winners}")
        subscribers.forEach { it.onGameFinished() }
    }

    override fun onCommandYourTurn(androidInterface: AndroidInterface) {
        turnsManager.myTurn = true
        draftBricks.forEach { it.draft = false }
        draftBricks.clear()
        subscribers.forEach { it.onMyTurn() }
        androidInterface.toast("Your turn")
    }

    override fun onCommandBeginGame(
        cashierSize: Int,
        letters: ArrayList<Char>,
        playersNames: ArrayList<String>
    ) {
        if (!gameInProgress) {
            beginGame(cashierSize, letters, playersNames)
        }
    }

    /**
     * Called on show() of screen.
     */
    fun onShow(
    ) {
        if (DefaultGameSettings.SKIP_MENU) {
            val randomLetters = ArrayList<Char>()
            for (i in 0 until 8) {
                randomLetters.add(Player.allowedLetters.random())
            }
            beginGame(80, randomLetters, listOf("שחקן"))
        }
    }

    /**
     * Called on resize() of screen.
     */
    fun onResize() {
    }

    override fun dispose() {
    }

    override fun onBrickDropped(screenX: Int, screenY: Int) {
    }

    override fun onGoRequest() {
        if (turnsManager.myTurn) {
            networkHandler.applySelectedLetters(draftBricks)
            val r = ArrayList<SubmissionResult>()
            var success = false
            if (networkHandler.sendForValidationAndFetchResults(r, androidInterface)) {
                handleSuccessfulTurn(r)
                success = handleValidTurn(r)
            }
            handleTurnFinish(success)
        }
    }

    private fun handleTurnFinish(success: Boolean) {
        turnsManager.myTurn = false
        subscribers.forEach { it.onPlayerFinishedTurn(draftBricks, success) }
        draftBricks.forEach {
            it.draft = false
            if (!success) {
                networkHandler.informLetterChangedInBoard(
                    Player.allowedLetters.indexOf(it.letter),
                    it,
                    true
                )
            }
        }
        draftBricks.clear()
    }

    private fun handleSuccessfulTurn(r: ArrayList<SubmissionResult>) {
        if (r.none { !it.resultValue }) {
            draftBricks.forEach {
                board[it.onCell!!.row][it.onCell!!.col] = Player.allowedLetters.indexOf(it.letter)
            }
            fetchNewLettersAndAddToPlayer(networkHandler)
            fetchUpdatedScore()
        }
    }

    private fun fetchUpdatedScore() {
        val updatedScore = networkHandler.fetchUpdatedScore()
        subscribers.forEach { it.onScoreUpdated(updatedScore) }
    }

    override fun onPlacedBrickReturnedBackToDeck(brickToReturn: Brick) {
        draftBricks.remove(brickToReturn)
        brickToReturn.draft = false
        val letter = Player.allowedLetters.indexOf(brickToReturn.letter)
        networkHandler.informLetterChangedInBoard(letter, brickToReturn, true)
    }

    override fun onHudStageInitializedForGame(bottomHudImage: Image) {
    }

    private fun handleValidTurn(results: ArrayList<SubmissionResult>): Boolean {
        var result = ""
        var success = true
        results.forEach {
            result += it.resultValue
            success = success.and(it.resultValue)
        }
        androidInterface.toast("RESULT:$result")
        return success
    }

    private fun fetchNewLettersAndAddToPlayer(
        networkHandler: NetworkHandler,
    ) {
        val newLetters = ArrayList<Char>()
        networkHandler.fetchNewLetters(newLetters)
        if (newLetters.isNotEmpty()) {
            player.letters.addAll(newLetters)
            subscribers.forEach { it.onNewLettersFetched(newLetters) }
        }
    }

    override fun onPlacedBrickTakenOutOfBoard(brickToReturn: Brick) {
        board[brickToReturn.onCell!!.row][brickToReturn.onCell!!.col] = NO_LETTER
    }

    override fun onPlacedBrickTouched(brick: Brick) {
    }

    override fun onBrickPlacedInBoard(brick: Brick) {
        board[brick.onCell!!.row][brick.onCell!!.col] = Player.allowedLetters.indexOf(brick.letter)
        networkHandler.informLetterChangedInBoard(
            Player.allowedLetters.indexOf(brick.letter),
            brick,
        )
    }

    override fun onPlacedBrickMovedToAnotherCell(
        oldCell: BoardCell?,
        newCell: BoardCell,
        brick: Brick
    ) {
        board[oldCell!!.row][oldCell.col] = NO_LETTER
        board[newCell.row][newCell.col] = Player.allowedLetters.indexOf(brick.letter)
        networkHandler.informLetterChangedInBoard(
            Player.allowedLetters.indexOf(brick.letter),
            brick,
            true
        )
    }

    override fun subscribeForEvents(subscriber: GameLogicManagerEventsSubscriber) {
        subscribers.add(subscriber)
    }

    companion object {
        const val NO_LETTER = -1
    }


}