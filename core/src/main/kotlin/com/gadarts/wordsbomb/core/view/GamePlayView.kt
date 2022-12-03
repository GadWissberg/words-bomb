package com.gadarts.wordsbomb.core.view

import com.badlogic.gdx.Gdx
import com.badlogic.gdx.InputMultiplexer
import com.badlogic.gdx.graphics.g2d.SpriteBatch
import com.badlogic.gdx.input.GestureDetector
import com.badlogic.gdx.utils.Disposable
import com.gadarts.wordsbomb.core.model.assets.GameAssetManager
import com.gadarts.wordsbomb.core.controller.GameController
import com.gadarts.wordsbomb.core.controller.TurnsManager
import com.gadarts.wordsbomb.core.controller.network.NetworkHandler
import com.gadarts.wordsbomb.core.view.board.BoardStage
import com.gadarts.wordsbomb.core.view.board.CameraManipulator
import com.gadarts.wordsbomb.core.view.hud.BrickCursor
import com.gadarts.wordsbomb.core.view.hud.HudStage
import java.util.*

/**
 * Responsible for the HUD and board stage.
 */
class GamePlayView(private val playerName: String) : Disposable {

    private var networkHandler: NetworkHandler? = null

    /**
     * Represents the HUD.
     */
    lateinit var hudStage: HudStage

    /**
     * Represents the game's board.
     */
    lateinit var boardStage: BoardStage

    /**
     * Creates and initializes the stages.
     */
    fun initStages(
        assetsManager: GameAssetManager,
        draftBricks: HashSet<Brick>,
        turnsManager: TurnsManager,
        networkHandler: NetworkHandler?,
        gameController: GameController,
    ) {
        val brickCursor = BrickCursor()
        val stagesCommon = StagesCommon(draftBricks, brickCursor)
        this.networkHandler = networkHandler
        boardStage = BoardStage(stagesCommon, assetsManager, SpriteBatch(), CameraManipulator())
        hudStage = HudStage(assetsManager, turnsManager, stagesCommon, SpriteBatch())
        hudStage.subscribeForEvents(boardStage)
        boardStage.subscribeForEvents(hudStage)
        boardStage.init()
        addStagesAsInput()
        gameController.subscribeForEvents(hudStage)
        gameController.subscribeForEvents(boardStage)
    }
    private fun addStagesAsInput() {
        val multiplexer = Gdx.input.inputProcessor as InputMultiplexer
        multiplexer.addProcessor(hudStage)
        multiplexer.addProcessor(GestureDetector(boardStage))
        multiplexer.addProcessor(boardStage)
    }

    /**
     * Updates and renders the stages.
     */
    fun render(delta: Float) {
        boardStage.viewport.apply()
        boardStage.act(delta)
        boardStage.draw()
        hudStage.viewport.apply()
        hudStage.act(delta)
        hudStage.draw()
    }

    /**
     * Updates the stages viewports.
     */
    fun onResize(width: Int, height: Int) {
        hudStage.viewport.update(width, height, true)
    }

    override fun dispose() {
        boardStage.dispose()
        hudStage.dispose()
    }

}
