package com.gadarts.shubutz.core

import com.gadarts.shubutz.core.model.Difficulties
import com.gadarts.shubutz.core.model.Product
import com.gadarts.shubutz.core.screens.menu.view.OnChampionFetched

interface AndroidInterface {
    fun toast(msg: String)
    fun versionName(): String
    fun getSharedPreferencesIntValue(key: String, default: Int = 0): Int
    fun getSharedPreferencesBooleanValue(key: String, default: Boolean): Boolean
    fun getSharedPreferencesLongValue(key: String, default: Long): Long
    fun saveSharedPreferencesIntValue(key: String, value: Int)
    fun saveSharedPreferencesLongValue(key: String, value: Long)
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
    fun logCrashlytics(message: String)
    fun login(): Boolean
}
