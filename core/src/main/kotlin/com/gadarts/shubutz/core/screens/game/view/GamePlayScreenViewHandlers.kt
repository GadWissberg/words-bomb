package com.gadarts.shubutz.core.screens.game.view

import com.badlogic.gdx.graphics.g2d.BitmapFont
import com.badlogic.gdx.math.Vector2
import com.badlogic.gdx.scenes.scene2d.Action
import com.badlogic.gdx.scenes.scene2d.ui.Table
import com.gadarts.shubutz.core.model.GameModel
import com.gadarts.shubutz.core.model.assets.GameAssetManager
import com.gadarts.shubutz.core.screens.game.GamePlayScreen
import com.gadarts.shubutz.core.screens.menu.view.stage.GameStage

class GamePlayScreenViewHandlers(private val assetsManager: GameAssetManager) {

    lateinit var targetWordsHandler: TargetWordsHandler
    val bombHandler = BombHandler()
    lateinit var optionsHandler: OptionsHandler

    fun onShow(
        letterSize: Vector2,
        font80: BitmapFont,
        assetsManager: GameAssetManager,
        stage: GameStage,
        uiTable: Table,
        gameModel: GameModel,
    ) {
        targetWordsHandler = TargetWordsHandler(letterSize, font80)
        targetWordsHandler.calculateMaxBricksPerLine(assetsManager)
        optionsHandler = OptionsHandler(stage)
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
    }

    fun onGameWinAnimation(stage: GameStage, actionOnGameWinAnimationFinish: Action) {
        bombHandler.onGameWinAnimation()
        targetWordsHandler.onGameWinAnimation(assetsManager, stage, actionOnGameWinAnimationFinish)
    }

    fun onScreenClear(postAction: Runnable) {
        bombHandler.onScreenClear {
            optionsHandler.onScreenClear(postAction)
            targetWordsHandler.onScreenClear()
        }
    }

    fun onLetterFail() {
        bombHandler.onLetterFail()
        optionsHandler.onLetterFail()
    }

    fun onGameOverAnimation(stage: GameStage, gamePlayScreen: GamePlayScreen) {
        bombHandler.onGameOverAnimation(assetsManager, stage, gamePlayScreen)
        optionsHandler.clearAllOptions()
    }

}
