package com.gadarts.shubutz.core.screens.game.view

import com.badlogic.gdx.math.Interpolation
import com.badlogic.gdx.math.Vector2
import com.badlogic.gdx.scenes.scene2d.actions.Actions
import com.badlogic.gdx.scenes.scene2d.ui.Image
import com.badlogic.gdx.scenes.scene2d.ui.Table
import com.badlogic.gdx.utils.Disposable
import com.gadarts.shubutz.core.SoundPlayer
import com.gadarts.shubutz.core.model.GameModel
import com.gadarts.shubutz.core.model.assets.definitions.FontsDefinitions
import com.gadarts.shubutz.core.model.assets.GameAssetManager
import com.gadarts.shubutz.core.model.assets.definitions.TexturesDefinitions
import com.gadarts.shubutz.core.screens.game.GamePlayScreen
import com.gadarts.shubutz.core.screens.menu.view.stage.GameStage

class GamePlayScreenComponents(
    private val assetsManager: GameAssetManager,
    private val soundPlayer: SoundPlayer,
    gamePlayScreen: GamePlayScreen
) : Disposable {

    lateinit var targetPhraseView: TargetPhraseView
    val bombView = BombView(soundPlayer, assetsManager)
    lateinit var optionsView: OptionsView
    val topBarView = TopBarView(soundPlayer, assetsManager, gamePlayScreen)

    fun createViews(
        letterSize: Vector2,
        assetsManager: GameAssetManager,
        stage: GameStage,
        gameModel: GameModel,
        gamePlayScreen: GamePlayScreen,
    ) {
        topBarView.addTopBar(assetsManager, gameModel, gamePlayScreen, stage)
        val font80 = assetsManager.getFont(FontsDefinitions.VARELA_80)
        targetPhraseView = TargetPhraseView(letterSize, font80, soundPlayer, assetsManager)
        targetPhraseView.calculateMaxBricksPerLine(assetsManager)
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
        targetPhraseView.onGameBegin(gameModel, assetsManager, uiTable)
        optionsView.addLettersOptionsTable(
            uiTable,
            assetsManager,
            targetPhraseView.maxBricksPerLine,
            letterSize,
            gamePlayScreen,
            gameModel
        )
        topBarView.setCategoryLabelText(gameModel.currentCategory)
    }

    fun applyGameWinAnimation(
        stage: GameStage,
        gameModel: GameModel,
        actionOnGameWinAnimationFinish: Runnable
    ) {
        bombView.stopFire()
        targetPhraseView.applyGameWinAnimation(assetsManager, stage, actionOnGameWinAnimationFinish)
        applyCoinsEffect(gameModel, stage)
    }

    private fun applyCoinsEffect(
        gameModel: GameModel,
        stage: GameStage
    ) {
        val coinTexture = assetsManager.getTexture(TexturesDefinitions.COIN)
        val startPosition = bombView.bombComponent.localToScreenCoordinates(Vector2())
        startPosition.x += bombView.bombComponent.width / 2F - coinTexture.width / 2F
        startPosition.y = stage.height - startPosition.y
        val targetPosition = topBarView.coinsIcon.localToScreenCoordinates(Vector2())
        targetPosition.y = stage.height - targetPosition.y
        for (i in 0 until gameModel.selectedDifficulty.winWorth) {
            val coin = Image(coinTexture)
            stage.addActor(coin)
            coin.setPosition(startPosition.x, startPosition.y)

            coin.addAction(
                Actions.sequence(
                    Actions.delay(i.toFloat() * 0.25F),
                    Actions.alpha(0F),
                    Actions.sizeTo(0F, 0F),
                    Actions.parallel(
                        Actions.alpha(1F, 0.5F),
                        Actions.sequence(
                            Actions.sizeTo(
                                coinTexture.width.toFloat(),
                                coinTexture.height.toFloat(),
                                0.25F
                            ),
                            Actions.delay(0.25F),
                            Actions.alpha(0F, 1F),
                        ),
                        Actions.moveTo(
                            targetPosition.x,
                            targetPosition.y,
                            1F,
                            Interpolation.smooth2
                        )
                    ),
                    Actions.removeActor()
                )
            )

        }
    }

    fun clearBombView() {
        bombView.animateBombVanish {
            optionsView.onScreenClear()
            targetPhraseView.onScreenClear()
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
            val cell = uiTable.getCell(bombView.bombComponent)
            val delta = optionsView.lettersOptionsTable.y * 2F
            cell.size(
                cell.prefWidth + delta,
                cell.prefHeight + delta
            )
            uiTable.pack()
        }
    }

}
