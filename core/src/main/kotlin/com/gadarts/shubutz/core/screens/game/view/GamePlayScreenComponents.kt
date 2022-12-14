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

class GamePlayScreenComponents(
    private val assetsManager: GameAssetManager,
    private val soundPlayer: SoundPlayer
) : Disposable {

    lateinit var targetPhrasesView: TargetPhrasesView
    val bombHandler = BombHandler(soundPlayer, assetsManager)
    lateinit var optionsComponent: OptionsComponent
    val topBarView = TopBarView(soundPlayer)

    fun onShow(
        letterSize: Vector2,
        font80: BitmapFont,
        assetsManager: GameAssetManager,
        stage: GameStage,
        gameModel: GameModel,
        gamePlayScreen: GamePlayScreen,
    ) {
        topBarView.addTopBar(assetsManager, gameModel, gamePlayScreen, stage)
        targetPhrasesView = TargetPhrasesView(letterSize, font80, soundPlayer, assetsManager)
        targetPhrasesView.calculateMaxBricksPerLine(assetsManager)
        optionsComponent = OptionsComponent(stage, soundPlayer, assetsManager)
    }

    fun init(
        uiTable: Table,
        gameModel: GameModel,
        letterSize: Vector2,
        gamePlayScreen: GamePlayScreen,
        stage: GameStage
    ) {
        bombHandler.addBomb(assetsManager, stage, uiTable, gameModel)
        targetPhrasesView.onGameBegin(gameModel, assetsManager, uiTable)
        optionsComponent.addLettersOptionsTable(
            uiTable,
            assetsManager,
            targetPhrasesView.maxBricksPerLine,
            letterSize,
            gamePlayScreen,
            gameModel
        )
        topBarView.setCategoryLabelText(gameModel.currentCategory)
    }

    fun onGameWinAnimation(stage: GameStage, actionOnGameWinAnimationFinish: Runnable) {
        bombHandler.onGameWinAnimation()
        targetPhrasesView.onGameWinAnimation(assetsManager, stage, actionOnGameWinAnimationFinish)
    }

    fun onScreenClear() {
        bombHandler.onScreenClear {
            optionsComponent.onScreenClear()
            targetPhrasesView.onScreenClear()
        }
    }

    fun onIncorrectGuess() {
        bombHandler.onIncorrectGuess()
        optionsComponent.onIncorrectGuess()
    }

    fun onGameOverAnimation(stage: GameStage) {
        bombHandler.onGameOverAnimation(assetsManager, stage)
        optionsComponent.clearAllOptions()
    }

    override fun dispose() {
        topBarView.dispose()
    }

    fun clear() {
        topBarView.clear()
        bombHandler.clear()
    }

    fun onCorrectGuess(coinsAmount: Int) {
        topBarView.applyWinCoinEffect(coinsAmount, assetsManager)
    }

}
