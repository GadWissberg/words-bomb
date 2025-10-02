package com.gadarts.shubutz.core.screens.game

import com.gadarts.shubutz.core.model.Product

interface GamePlayScreen {
    fun onQuitSession()
    fun onBrickClicked(letter: Char)
    fun onScreenEmpty()
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

    fun onClickedToRevealWordOnGameOver()
    fun onRevealedWordOnGameOver(cost: Int)

}
