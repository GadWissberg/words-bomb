package com.gadarts.shubutz.core.screens

import com.badlogic.gdx.Screen

abstract class GameScreen : Screen {
    abstract fun onSuccessfulPurchase(products: MutableList<String>)
    abstract fun onFailedPurchase(message: String)
    abstract fun onRewardForVideoAd(rewardAmount: Int)

}
