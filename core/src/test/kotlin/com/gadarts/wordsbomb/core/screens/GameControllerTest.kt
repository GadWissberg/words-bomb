package com.gadarts.wordsbomb.core.screens

import com.badlogic.gdx.net.Socket
import com.gadarts.wordsbomb.core.AndroidInterface
import com.gadarts.wordsbomb.core.view.Brick
import com.gadarts.wordsbomb.core.controller.GameController
import com.gadarts.wordsbomb.core.controller.network.NetworkConstants
import com.gadarts.wordsbomb.core.controller.network.NetworkHandler
import junit.framework.Assert.assertTrue
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.mockito.Mock
import org.mockito.junit.MockitoJUnitRunner
import org.mockito.kotlin.whenever
import java.io.InputStream
import java.util.*

@RunWith(MockitoJUnitRunner::class)
class GameControllerTest {

    @Mock
    private lateinit var inputStream: InputStream

    @Mock
    private lateinit var socket: Socket

    @Mock
    private lateinit var androidInterface: AndroidInterface

    private val draftBricks = HashSet<Brick>()

    @Mock
    private lateinit var networkHandler: NetworkHandler

    private lateinit var gameController: GameController

    @Before
    fun setUp() {
        gameController = GameController(networkHandler, draftBricks, androidInterface, playerName)
        whenever(socket.inputStream).thenReturn(inputStream)
        whenever(inputStream.available()).thenReturn(1)
    }

    @Test
    fun actCommandYourTurnPositive() {
        whenever(inputStream.read()).thenReturn(NetworkConstants.Commands.COMMAND_YOUR_TURN)

        gameController.act(socket)

        assertTrue(gameController.turnsManager.myTurn)
    }
}