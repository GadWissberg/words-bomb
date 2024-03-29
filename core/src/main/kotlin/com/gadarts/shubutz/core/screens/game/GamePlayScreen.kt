package com.gadarts.shubutz.core.screens.game

import com.gadarts.shubutz.core.model.Product

interface GamePlayScreen {
    fun onGameOverAnimationDone()
    fun onQuitSession()
    fun onBrickClicked(letter: Char)
    fun onScreenEmpty()
    fun onOpenProductsMenu(
        onSuccess: (products: Map<String, Product>) -> Unit,
        onFailure: (message: String) -> Unit
    )

    fun onPackPurchaseButtonClicked(selectedProduct: Product)
    fun onRevealLetterButtonClicked() : Boolean
    fun onLetterRevealed(letter: Char, cost: Int)
    fun onIncorrectGuess(gameOver: Boolean)
    fun onCorrectGuess(
        indices: List<Int>,
        roundWin: Boolean,
        coinsAmount: Int,
        perfectBonusAchieved: Boolean,
        prevScore: Long,
    )

    fun onLetterRevealFailedNotEnoughCoins()
    fun onBuyCoinsDialogOpened(onLoaded: () -> Unit)
    fun onShowVideoAdClicked(onAdCompleted: () -> Unit, onAdDismissed: () -> Unit)
    fun onClickedToRevealWordOnGameOver()
    fun onRevealedWordOnGameOver(cost: Int)
    fun onFailedToRevealWordOnGameOver()
    fun onBuyCoinsButtonClicked()

}
