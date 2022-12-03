package com.gadarts.wordsbomb.core.controller.network

import com.badlogic.gdx.Gdx
import com.gadarts.wordsbomb.core.AndroidInterface
import com.gadarts.wordsbomb.core.CommandHandlingException
import com.gadarts.wordsbomb.core.model.Player
import com.gadarts.wordsbomb.core.controller.network.NetworkConstants.Commands
import com.gadarts.wordsbomb.core.controller.network.NetworkConstants.Commands.COMMAND_BEGIN_GAME
import com.gadarts.wordsbomb.core.view.Brick
import com.gadarts.wordsbomb.core.view.hud.SubmissionResult
import ktx.assets.disposeSafely
import java.io.InputStreamReader
import java.io.OutputStreamWriter
import java.nio.ByteBuffer

open class NetworkHandlerImpl : NetworkHandler {
    private lateinit var reader: InputStreamReader
    private lateinit var writer: OutputStreamWriter
    private var socketManager = SocketManager()

    override fun sendForValidationAndFetchResults(
        results: ArrayList<SubmissionResult>,
        androidInterface: AndroidInterface
    ): Boolean {
        return when (socketManager.socket!!.inputStream.read()) {
            NetworkConstants.Responses.RESPONSE_OK -> {
                fetchResults(results)
                true
            }
            NetworkConstants.Responses.RESPONSE_DENIED_INVALID_FIRST_WORD -> {
                androidInterface.toast("First turn can be a single word only!")
                false
            }
            NetworkConstants.Responses.RESPONSE_DENIED_INVALID_LETTER_PLACING -> {
                androidInterface.toast("The letters are not placed in a legal manner")
                false
            }
            else -> false
        }
    }

    private fun fetchResults(
        results: ArrayList<SubmissionResult>
    ) {
        while (true) {
            val wordFirstByte = socketManager.socket!!.inputStream.read()
            if (wordFirstByte != STREAM_END.toInt()) {
                fetchResult(wordFirstByte, results)
            } else {
                break
            }
        }
    }

    private fun fetchResult(
        firstByte: Int,
        results: ArrayList<SubmissionResult>
    ) {
        val startCol = socketManager.socket!!.inputStream.read()
        val endRow = socketManager.socket!!.inputStream.read()
        val endCol = socketManager.socket!!.inputStream.read()
        val resultValue = socketManager.socket!!.inputStream.read() == 1
        results.add(SubmissionResult(firstByte, startCol, endRow, endCol, resultValue))
    }

    override fun fetchNewLetters(
        newLetters: ArrayList<Char>
    ) {
        while (true) {
            val letter = socketManager.socket!!.inputStream.read()
            if (letter != STREAM_END.toInt()) {
                newLetters.add(Player.allowedLetters[letter])
            } else {
                break
            }
        }
    }

    override fun fetchUpdatedScore(): Int {
        socketManager.socket!!.inputStream.read(auxByteArray)
        auxByteArray.reverse()
        return ByteBuffer.wrap(auxByteArray).short.toInt()
    }

    override fun reset() {
        socketManager.reset()
        initialize()
    }

    override fun dispose() {
        socketManager.dispose()
    }

    override fun getSocketManager(): SocketManager {
        return socketManager
    }

    override fun initialize() {
        reader = socketManager.socket!!.inputStream.reader()
        writer = socketManager.socket!!.outputStream.writer()
    }

    override fun close() {
        socketManager.disposeSafely()
        writer.close()
        reader.close()
    }

    override fun informLetterChangedInBoard(
        letter: Int,
        brick: Brick,
        removed: Boolean
    ) {
        writer.append(NetworkConstants.Requests.REQUEST_LETTER_CHANGED_ON_BOARD.toChar())
        writer.append(if (!removed) letter.toChar() else Player.allowedLetters.length.toChar())
        writer.append(brick.onCell!!.row.toChar())
        writer.append(brick.onCell!!.col.toChar())
        writer.flush()
    }

    override fun act(androidInterface: AndroidInterface, commandsListener: CommandsListener) {
        try {
            fetchCommands(androidInterface, commandsListener)
        } catch (e: CommandHandlingException) {
            Gdx.app.error(e.commandIndex.toString(), e.message)
        }
    }

    private fun fetchCommands(
        androidInterface: AndroidInterface,
        commandsListener: CommandsListener
    ) {
        if (socketManager.socket!!.inputStream.available() > 0) {
            when (val commandIndex = socketManager.socket!!.inputStream.read()) {
                COMMAND_BEGIN_GAME -> {
                    val cashierSize = readCommandDataNextByteSafely(
                        COMMAND_BEGIN_GAME,
                        EXCEPTION_EXPECTED_CASHIER_SIZE_NOT_RETRIEVED
                    )
                    val players = readPlayersNames()
                    val letters = readInitialLetters()
                    commandsListener.onCommandBeginGame(
                        cashierSize,
                        letters,
                        players
                    )
                }
                Commands.COMMAND_YOUR_TURN -> {
                    commandsListener.onCommandYourTurn(androidInterface)
                }
                Commands.COMMAND_LETTER_PLACED -> {
                    val letter = socketManager.socket!!.inputStream.read()
                    val row = socketManager.socket!!.inputStream.read()
                    val col = socketManager.socket!!.inputStream.read()
                    commandsListener.onCommandLetterPlaced(letter, row, col)
                }
                Commands.COMMAND_UPDATE_CASHIER -> {
                    val cashierSize = readCommandDataNextByteSafely(
                        Commands.COMMAND_UPDATE_CASHIER,
                        EXCEPTION_EXPECTED_CASHIER_SIZE_NOT_RETRIEVED
                    )
                    commandsListener.onCommandUpdateCashier(cashierSize)
                }
                Commands.COMMAND_GAME_OVER -> {
                    val winners = ArrayList<Int>()
                    while (socketManager.socket!!.inputStream.available() > 0) {
                        val winnerIndex = readCommandDataNextByteSafely(
                            Commands.COMMAND_GAME_OVER,
                            EXCEPTION_EXPECTED_WINNER_INDEX
                        )
                        if (winnerIndex.toChar() != STREAM_END) {
                            winners.add(winnerIndex)
                        }
                    }
                    commandsListener.onCommandGameOver(winners)
                }
                else -> {
                    throw CommandHandlingException(
                        commandIndex,
                        EXCEPTION_UNKNOWN_COMMAND.format()
                    )
                }
            }
        }
    }

    private fun readPlayersNames(): ArrayList<String> {
        val players = ArrayList<String>()
        val numberOfPlayer = readCommandDataNextByteSafely(
            COMMAND_BEGIN_GAME,
            EXCEPTION_EXPECTED_NUMBER_OF_PLAYERS
        )
        for (i in 0 until numberOfPlayer) {
            players.add(readNextPlayerName())
        }
        return players
    }

    private fun readNextPlayerName(): String {
        val size = readCommandDataNextByteSafely(COMMAND_BEGIN_GAME, EXCEPTION_EXPECTED_NAME_SIZE)
        val name = ByteArray(size)
        for (i in 0 until size) {
            name[i] = readCommandDataNextByteSafely(
                COMMAND_BEGIN_GAME,
                EXCEPTION_EXPECTED_NAME_CHAR
            ).toByte()
        }
        return String(name, Charsets.UTF_8)
    }

    private fun readInitialLetters(): ArrayList<Char> {
        val letters = ArrayList<Char>()
        while (socketManager.socket!!.inputStream.available() > 0) {
            if (readNextLetter(letters)) break
        }
        return letters
    }

    private fun readNextLetter(letters: ArrayList<Char>): Boolean {
        val index = readCommandDataNextByteSafely(
            COMMAND_BEGIN_GAME,
            EXCEPTION_EXPECTED_LETTER
        )
        if (index != STREAM_END.toInt()) {
            letters.add(Player.allowedLetters[index])
        } else {
            return true
        }
        return false
    }

    private fun readCommandDataNextByteSafely(commandIndex: Int, failureMessage: String): Int {
        val result: Int
        if (socketManager.socket!!.inputStream.available() > 0) {
            result = socketManager.socket!!.inputStream.read()
        } else {
            throw CommandHandlingException(commandIndex, failureMessage)
        }
        return result
    }

    override fun applySelectedLetters(draftBricks: HashSet<Brick>) {
        writer.append(NetworkConstants.Requests.REQUEST_SUBMIT_LETTERS.toChar())
        draftBricks.forEach {
            val indexOf = Player.allowedLetters.indexOf(it.letter[0], 0, false)
            writer.append(indexOf.toChar())
                .append(it.onCell!!.row.toChar())
                .append(it.onCell!!.col.toChar())
        }
        writer.append(STREAM_END).flush()
    }

    companion object {
        val auxByteArray = ByteArray(2)
        private const val EXCEPTION_EXPECTED_CASHIER_SIZE_NOT_RETRIEVED = "Expected cashier size " +
                "was not retrieved"
        private const val EXCEPTION_EXPECTED_WINNER_INDEX = "Expected winner index"
        private const val EXCEPTION_EXPECTED_LETTER = "Expected a letter index"
        private const val EXCEPTION_UNKNOWN_COMMAND = "Unknown command received"
        private const val EXCEPTION_EXPECTED_NUMBER_OF_PLAYERS = "Expected number of players " +
                "was not retrieved"
        private const val EXCEPTION_EXPECTED_NAME_SIZE = "Expected name size " +
                "was not retrieved"
        private const val EXCEPTION_EXPECTED_NAME_CHAR = "Expected name char " +
                "was not retrieved"

        /**
         * Signals for the end of sent stream.
         */
        const val STREAM_END = '!'
    }
}