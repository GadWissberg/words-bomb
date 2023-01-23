package com.gadarts.shubutz.core.screens.game.view

import com.badlogic.gdx.math.Vector2
import com.badlogic.gdx.scenes.scene2d.ui.Table
import com.badlogic.gdx.utils.Disposable
import com.gadarts.shubutz.core.SoundPlayer
import com.gadarts.shubutz.core.model.GameModel
import com.gadarts.shubutz.core.model.assets.FontsDefinitions
import com.gadarts.shubutz.core.model.assets.GameAssetManager
import com.gadarts.shubutz.core.screens.game.GamePlayScreen
import com.gadarts.shubutz.core.screens.menu.view.stage.GameStage

class GamePlayScreenComponents(
    private val assetsManager: GameAssetManager,
    private val soundPlayer: SoundPlayer
) : Disposable {

    lateinit var targetPhrasesView: TargetPhrasesView
    val bombView = BombView(soundPlayer, assetsManager)
    lateinit var optionsView: OptionsView
    val topBarView = TopBarView(soundPlayer, assetsManager)

    fun createViews(
        letterSize: Vector2,
        assetsManager: GameAssetManager,
        stage: GameStage,
        gameModel: GameModel,
        gamePlayScreen: GamePlayScreen,
    ) {
        topBarView.addTopBar(assetsManager, gameModel, gamePlayScreen, stage)
        val font80 = assetsManager.getFont(FontsDefinitions.VARELA_80)
        targetPhrasesView = TargetPhrasesView(letterSize, font80, soundPlayer, assetsManager)
        targetPhrasesView.calculateMaxBricksPerLine(assetsManager)
        optionsView = OptionsView(stage, soundPlayer, assetsManager)
    }

    fun init(
        uiTable: Table,
        gameModel: GameModel,
        letterSize: Vector2,
        gamePlayScreen: GamePlayScreen,
        stage: GameStage,
    ) {
        bombView.addBomb(assetsManager, stage, uiTable, gameModel)
        targetPhrasesView.onGameBegin(gameModel, assetsManager, uiTable)
        optionsView.addLettersOptionsTable(
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
        bombView.onGameWinAnimation()
        targetPhrasesView.onGameWinAnimation(assetsManager, stage, actionOnGameWinAnimationFinish)
    }

    fun clearBombView() {
        bombView.animateBombVanish {
            optionsView.onScreenClear()
            targetPhrasesView.onScreenClear()
        }
    }

    fun onIncorrectGuess() {
        bombView.onIncorrectGuess()
        optionsView.onIncorrectGuess()
    }

    fun onGameOverAnimation(stage: GameStage) {
        bombView.onGameOverAnimation(assetsManager, stage)
        optionsView.clearAllOptions()
    }

    override fun dispose() {
        topBarView.dispose()
    }

    fun clear() {
        topBarView.clear()
        bombView.clear()
    }

    fun onCorrectGuess(coinsAmount: Int) {
        topBarView.applyWinCoinEffect(coinsAmount, assetsManager)
    }

    fun resizeComponentsIfNeeded(uiTable: Table) {
        if (optionsView.lettersOptionsTable.y < 0F) {
            val cell = uiTable.getCell(bombView.bomb)
            val delta = optionsView.lettersOptionsTable.y * 2F
            cell.size(
                cell.prefWidth + delta,
                cell.prefHeight + delta
            )
            uiTable.pack()
        }
    }

}
