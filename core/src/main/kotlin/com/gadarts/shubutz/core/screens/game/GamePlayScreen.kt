package com.gadarts.shubutz.core.screens.game

import com.gadarts.shubutz.core.model.Product

interface GamePlayScreen {
    fun onGameOverAnimationDone()
    fun onClickedBackButton()
    fun onBrickClicked(letter: Char)
    fun onScreenEmpty()
    fun onOpenProductsMenu(
        onSuccess: (products: Map<String, Product>) -> Unit,
        onFailure: (message: String) -> Unit
    )

    fun onPackPurchaseButtonClicked(selectedProduct: Product)
    fun onRevealLetterButtonClicked()
    fun onLetterRevealed(letter: Char, cost: Int)
    fun onIncorrectGuess(gameOver: Boolean)
    fun onCorrectGuess(indices: List<Int>, gameWin: Boolean, coinsAmount: Int)
    fun onLetterRevealFailed()

}
