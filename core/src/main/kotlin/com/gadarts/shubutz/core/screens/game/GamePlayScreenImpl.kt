package com.gadarts.shubutz.core.screens.game

import com.badlogic.gdx.Gdx
import com.badlogic.gdx.Input
import com.badlogic.gdx.utils.TimeUtils
import com.gadarts.shubutz.core.AnalyticsEvents
import com.gadarts.shubutz.core.AnalyticsEventsParams
import com.gadarts.shubutz.core.AndroidInterface
import com.gadarts.shubutz.core.DebugSettings
import com.gadarts.shubutz.core.GameLifeCycleManager
import com.gadarts.shubutz.core.business.GameLogicHandler
import com.gadarts.shubutz.core.model.GameModel
import com.gadarts.shubutz.core.model.GameModes
import com.gadarts.shubutz.core.model.InAppProducts
import com.gadarts.shubutz.core.model.Product
import com.gadarts.shubutz.core.model.assets.SharedPreferencesKeys.DISABLE_ADS_DUE_DATE
import com.gadarts.shubutz.core.screens.GameScreen
import com.gadarts.shubutz.core.screens.game.view.GamePlayScreenView
import com.gadarts.shubutz.core.screens.menu.view.Champion
import com.gadarts.shubutz.core.screens.menu.view.OnChampionFetched
import com.gadarts.shubutz.core.screens.menu.view.stage.GameStage

class GamePlayScreenImpl(
    private val globalHandlers: GlobalHandlers,
    private val lifeCycleManager: GameLifeCycleManager,
    private val android: AndroidInterface,
    private val stage: GameStage,
    private val selectedMode: GameModes,
) : GameScreen(), GamePlayScreen {


    private var roundsCounter = 0
    private val gameModel = createGameModel()
    private lateinit var gameLogicHandler: GameLogicHandler
    private lateinit var gamePlayScreenView: GamePlayScreenView

    @Suppress("KotlinConstantConditions")
    private fun createGameModel(): GameModel {
        val coins: Int = if (DebugSettings.FORCE_NUMBER_OF_COINS >= 0) {
            DebugSettings.FORCE_NUMBER_OF_COINS
        } else {
            android.getSharedPreferencesIntValue(
                selectedMode.getSharedPrefCoinsKey(),
                INITIAL_COINS_VALUE
            )
        }
        return GameModel(coins, selectedMode)
    }

    override fun onSuccessfulPurchase(products: MutableList<String>) {
        products.forEach { product ->
            android.logEvent(
                AnalyticsEvents.PURCHASE_SUCCESSFUL,
                gameModel,
                mapOf(AnalyticsEventsParams.PACK_NAME to product)
            )
            val filtered = InAppProducts.values().filter { it.name.lowercase() == product }
            if (filtered.isNotEmpty()) {
                val amount = filtered.first().amount
                gameLogicHandler.onPurchasedCoins(gameModel, amount)
                gamePlayScreenView.onPurchasedCoins(amount)
                android.hideBannerAd()
                android.saveSharedPreferencesLongValue(
                    DISABLE_ADS_DUE_DATE,
                    TimeUtils.millis() + ADS_DISABLE_LENGTH
                )
            }
        }
    }

    override fun onFailedPurchase(message: String) {
        android.logEvent(
            AnalyticsEvents.PURCHASE_FAILED,
            gameModel,
            mapOf(AnalyticsEventsParams.ERROR_MESSAGE to message)
        )
        gamePlayScreenView.displayFailedPurchase(message)
    }

    override fun onRewardForVideoAd(rewardAmount: Int) {
        gameLogicHandler.onRewardForVideoAd(rewardAmount, gameModel)
        gamePlayScreenView.onRewardForVideoAd(rewardAmount)
    }

    override fun onLeaderboardClosed() {
    }

    override fun show() {
        gameLogicHandler = GameLogicHandler(
            globalHandlers.assetsManager.phrases[selectedMode.getContentFileName()]!!,
            android,
            this
        )
        gameLogicHandler.beginGame(gameModel)
        android.logEvent(
            AnalyticsEvents.NEW_GAME,
            gameModel,
            mapOf(AnalyticsEventsParams.DIFFICULTY to gameModel.selectedMode.getModeDisplayName()),
        )
        gamePlayScreenView = createGamePlayScreenView()
        gamePlayScreenView.onShow()
        android.loadBannerAd()
    }

    private fun createGamePlayScreenView() = GamePlayScreenView(
        globalHandlers,
        gameModel,
        this,
        stage,
    )

    override fun render(delta: Float) {
        gamePlayScreenView.render(delta)
        if (Gdx.input.isKeyJustPressed(Input.Keys.BACK)) {
            gamePlayScreenView.onPhysicalBackClicked()
        }
    }

    override fun resize(width: Int, height: Int) {
    }

    override fun pause() {
    }

    override fun resume() {
    }

    override fun hide() {
        gamePlayScreenView.clear()
    }

    override fun dispose() {
        gamePlayScreenView.dispose()
    }

    override fun onBrickClicked(letter: Char) {
        gameLogicHandler.onBrickClicked(letter, gameModel)
    }

    override fun onScreenEmpty() {
        if (gameModel.currentTargetData.hiddenLettersIndices.isNotEmpty()) return
        roundsCounter++
        if (roundsCounter % REQUEST_AD_CYCLE_SIZE == 0) {
            android.loadBannerAd()
        }
        gameLogicHandler.beginRound(gameModel)
        gamePlayScreenView.initializeForGameBegin()
    }

    override fun onOpenProductsMenu(
        onSuccess: (products: Map<String, Product>) -> Unit,
        onFailure: (message: String) -> Unit
    ) {
        android.initializeInAppPurchases(onSuccess, onFailure)
    }

    override fun onPackPurchaseButtonClicked(selectedProduct: Product) {
        android.logEvent(
            AnalyticsEvents.CLICKED_PACK_BUTTON,
            gameModel,
            mapOf(AnalyticsEventsParams.PACK_NAME to selectedProduct.name)
        )
        android.launchBillingFlow(selectedProduct)
    }

    override fun onRevealLetterButtonClicked(): Boolean {
        return gameLogicHandler.onRevealLetterButtonClicked(gameModel)
    }

    override fun onLetterRevealed(letter: Char, cost: Int) {
        gamePlayScreenView.onLetterRevealed(letter, cost)
    }

    override fun onQuitSession() {
        val leaderboardsId = gameModel.selectedMode.getHighscoresId()
        if (gameModel.score > 0 && leaderboardsId != null) {
            android.submitScore(gameModel.score, leaderboardsId)
        }
        lifeCycleManager.goToMenu()
    }

    override fun onLetterRevealFailedNotEnoughCoins() {
        gamePlayScreenView.onLetterRevealFailedNotEnoughCoins()
    }

    override fun onBuyCoinsDialogOpened(onLoaded: () -> Unit) {
        gameLogicHandler.onBuyCoinsDialogOpened(onLoaded)
    }

    override fun onShowVideoAdClicked(onAdCompleted: () -> Unit, onAdDismissed: () -> Unit) {
        Gdx.app.postRunnable {
            android.logEvent(AnalyticsEvents.CLICKED_REWARD_BUTTON, gameModel)
            globalHandlers.androidInterface.displayRewardedAd(onAdCompleted, onAdDismissed)
        }
    }

    override fun onClickedToRevealWordOnGameOver() {
        gameLogicHandler.onClickedToRevealWordOnGameOver(gameModel)
    }

    override fun onRevealedWordOnGameOver(cost: Int) {
        gamePlayScreenView.onRevealedWordOnGameOver(cost)
    }

    override fun onFailedToRevealWordOnGameOver() {
        gamePlayScreenView.onFailedToRevealWordOnGameOver()
    }

    override fun onBuyCoinsButtonClicked() {
        android.logEvent(AnalyticsEvents.CLICKED_COINS_BUTTON, gameModel)
    }

    override fun onIncorrectGuess(gameOver: Boolean) {
        if (gameOver) {
            android.logEvent(
                AnalyticsEvents.GAME_OVER,
                gameModel,
                mapOf(AnalyticsEventsParams.SCORE to gameModel.score.toString())
            )
        }
        gamePlayScreenView.onIncorrectGuess(gameOver)
    }

    override fun onCorrectGuess(
        indices: List<Int>,
        roundWin: Boolean,
        coinsAmount: Int,
        perfectBonusAchieved: Boolean,
        prevScore: Long
    ) {
        if (roundWin) {
            android.logEvent(
                AnalyticsEvents.ROUND_WIN,
                gameModel,
                mapOf(AnalyticsEventsParams.SCORE to gameModel.score.toString()),
            )
        }
        gamePlayScreenView.onCorrectGuess(
            indices,
            roundWin,
            coinsAmount,
            perfectBonusAchieved,
            prevScore
        )
    }

    override fun onGameOverAnimationDone() {
        globalHandlers.androidInterface.fetchChampion(
            gameModel.selectedMode,
            object : OnChampionFetched {
                override fun run(champion: Champion?) {
                    val leaderboardsId = gameModel.selectedMode.getHighscoresId()
                    val hasLeaderboards = leaderboardsId != null
                    if (gameModel.score >= 0 && hasLeaderboards) {
                        globalHandlers.androidInterface.submitScore(
                            gameModel.score,
                            leaderboardsId!!
                        )
                    }
                    if (champion != null && champion.score < gameModel.score && hasLeaderboards) {
                        gamePlayScreenView.onChampion {
                            globalHandlers.androidInterface.displayLeaderboard(
                                leaderboardsId!!
                            )
                            openAnotherGameDialog()
                        }
                    } else {
                        openAnotherGameDialog()
                    }
                }
            })
    }

    private fun openAnotherGameDialog() {
        globalHandlers.dialogsHandler.openDialog(
            header = ANOTHER_GAME_DIALOG_HEADER,
            description = ANOTHER_GAME_DIALOG_DESCRIPTION.format(gameModel.selectedMode.getModeDisplayName()),
            onYes = { lifeCycleManager.goToPlayScreen(gameModel.selectedMode) },
            onNo = { lifeCycleManager.goToMenu() },
            dialogName = ANOTHER_GAME_DIALOG_NAME
        )
    }

    companion object {
        private const val INITIAL_COINS_VALUE = 32
        private const val ADS_DISABLE_LENGTH = 7 * 24 * 60 * 60 * 1000
        private const val REQUEST_AD_CYCLE_SIZE = 3
        private const val ANOTHER_GAME_DIALOG_HEADER = "נשחק שוב?"
        private const val ANOTHER_GAME_DIALOG_DESCRIPTION = "עוד משחק\nברמת %s?"
        private const val ANOTHER_GAME_DIALOG_NAME = "another_name"
    }


}