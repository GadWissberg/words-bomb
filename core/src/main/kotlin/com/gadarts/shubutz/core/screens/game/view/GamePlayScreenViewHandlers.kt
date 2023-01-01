package com.gadarts.shubutz.core.screens.game.view

import com.badlogic.gdx.graphics.g2d.BitmapFont
import com.badlogic.gdx.math.Vector2
import com.badlogic.gdx.scenes.scene2d.ui.Table
import com.badlogic.gdx.utils.Disposable
import com.gadarts.shubutz.core.SoundPlayer
import com.gadarts.shubutz.core.model.GameModel
import com.gadarts.shubutz.core.model.assets.GameAssetManager
import com.gadarts.shubutz.core.screens.game.GamePlayScreen
import com.gadarts.shubutz.core.screens.menu.view.stage.GameStage

class GamePlayScreenViewHandlers(
    private val assetsManager: GameAssetManager,
    private val soundPlayer: SoundPlayer
) : Disposable {

    lateinit var targetWordsHandler: TargetWordsHandler
    val bombHandler = BombHandler(soundPlayer, assetsManager)
    lateinit var optionsHandler: OptionsHandler
    val topBarHandler = TopBarHandler(soundPlayer)

    fun onShow(
        letterSize: Vector2,
        font80: BitmapFont,
        assetsManager: GameAssetManager,
        stage: GameStage,
        gameModel: GameModel,
        gamePlayScreen: GamePlayScreen,
    ) {
        topBarHandler.addTopBar(assetsManager, gameModel, gamePlayScreen, stage)
        targetWordsHandler = TargetWordsHandler(letterSize, font80, soundPlayer, assetsManager)
        targetWordsHandler.calculateMaxBricksPerLine(assetsManager)
        optionsHandler = OptionsHandler(stage, soundPlayer, assetsManager)
    }

    fun onGameBegin(
        uiTable: Table,
        gameModel: GameModel,
        letterSize: Vector2,
        font80: BitmapFont,
        gamePlayScreen: GamePlayScreen,
        stage: GameStage
    ) {
        bombHandler.addBomb(assetsManager, stage, uiTable, gameModel)
        targetWordsHandler.onGameBegin(gameModel, assetsManager, uiTable)
        optionsHandler.addLettersOptionsTable(
            uiTable,
            assetsManager,
            targetWordsHandler.maxBricksPerLine,
            letterSize,
            font80,
            gamePlayScreen
        )
        topBarHandler.onGameBegin(gameModel.currentCategory)
    }

    fun onGameWinAnimation(stage: GameStage, actionOnGameWinAnimationFinish: Runnable) {
        bombHandler.onGameWinAnimation()
        targetWordsHandler.onGameWinAnimation(assetsManager, stage, actionOnGameWinAnimationFinish)
    }

    fun onScreenClear() {
        bombHandler.onScreenClear {
            optionsHandler.onScreenClear()
            targetWordsHandler.onScreenClear()
        }
    }

    fun onLetterFail() {
        bombHandler.onIncorrectGuess()
        optionsHandler.onIncorrectGuess()
    }

    fun onGameOverAnimation(stage: GameStage) {
        bombHandler.onGameOverAnimation(assetsManager, stage)
        optionsHandler.clearAllOptions()
    }

    override fun dispose() {
        topBarHandler.dispose()
    }

    fun onHide() {
        topBarHandler.onHide()
    }

}
