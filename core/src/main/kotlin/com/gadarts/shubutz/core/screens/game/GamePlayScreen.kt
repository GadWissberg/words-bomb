package com.gadarts.shubutz.core.screens.game

import com.gadarts.shubutz.core.model.Product

interface GamePlayScreen {
    fun onGameOverAnimationDone()
    fun onClickedBackButton()
    fun onBrickClicked(letter: Char)
    fun onScreenEmpty()
    fun onOpenProductsMenu(postAction: (products: Map<String, Product>) -> Unit)
    fun onPackPurchaseButtonClicked(selectedProduct: Product, postAction: () -> String)

}
