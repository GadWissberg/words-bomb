package com.gadarts.shubutz.core

import com.gadarts.shubutz.core.model.Difficulties
import com.gadarts.shubutz.core.model.Product
import com.gadarts.shubutz.core.screens.menu.view.Champion
import com.gadarts.shubutz.core.screens.menu.view.OnChampionFetched

interface AndroidInterface {
    fun toast(msg: String)
    fun versionName(): String
    fun getSharedPreferencesIntValue(key: String): Int
    fun getSharedPreferencesBooleanValue(key: String, default: Boolean): Boolean
    fun saveSharedPreferencesIntValue(key: String, value: Int)
    fun saveSharedPreferencesBooleanValue(key: String, value: Boolean)
    fun initializeInAppPurchases(
        onSuccess: (products: Map<String, Product>) -> Unit,
        onFailure: (message: String) -> Unit
    )

    fun launchBillingFlow(selectedProduct: Product)
    fun initializeAds(onFinish: () -> Unit)
    fun loadVideoAd(onLoaded: () -> Unit)
    fun displayRewardedAd(onAdCompleted: () -> Unit, onAdDismissed: () -> Unit)
    fun loadBannerAd()
    fun hideBannerAd()
    fun submitScore(score: Long, leaderboardsId: String): Boolean
    fun displayLeaderboard(leaderboardsId: String)
    fun fetchChampion(difficulty: Difficulties, callback: OnChampionFetched)
    fun isConnected(): Boolean
}
