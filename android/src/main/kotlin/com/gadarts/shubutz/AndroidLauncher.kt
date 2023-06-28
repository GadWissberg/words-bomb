package com.gadarts.shubutz

import android.content.Intent
import android.content.pm.PackageInfo
import android.content.pm.PackageManager
import android.os.Build
import android.os.Bundle
import android.view.Window
import android.view.WindowInsets
import android.view.WindowManager
import android.widget.RelativeLayout
import android.widget.Toast
import com.android.billingclient.api.*
import com.badlogic.gdx.backends.android.AndroidApplication
import com.badlogic.gdx.backends.android.AndroidApplicationConfiguration
import com.badlogic.gdx.utils.TimeUtils
import com.gadarts.shubutz.core.AndroidInterface
import com.gadarts.shubutz.core.DebugSettings
import com.gadarts.shubutz.core.ShubutzGame
import com.gadarts.shubutz.core.model.Difficulties
import com.gadarts.shubutz.core.model.Product
import com.gadarts.shubutz.core.model.assets.SharedPreferencesKeys
import com.gadarts.shubutz.core.model.assets.SharedPreferencesKeys.SHARED_PREFERENCES_DATA_NAME
import com.gadarts.shubutz.core.screens.menu.view.OnChampionFetched
import com.google.android.gms.ads.MobileAds
import com.google.android.gms.ads.RequestConfiguration
import de.golfgl.gdxgamesvcs.GpgsClient.RC_LEADERBOARD


class AndroidLauncher : AndroidApplication(), AndroidInterface {


    private val googleServicesHandler = GoogleServicesHandler()
    private lateinit var game: ShubutzGame
    private var versionName = "0.0.0"
    private lateinit var layout: RelativeLayout

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        window.addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON)
        try {
            val pInfo = context.packageManager.getPackageInfoCompat(context.packageName, 0)
            versionName = pInfo.versionName
        } catch (e: PackageManager.NameNotFoundException) {
            e.printStackTrace()
        }
        game = ShubutzGame(this)
        requestWindowFeature(Window.FEATURE_NO_TITLE)
        createLayout()
        googleServicesHandler.onCreate(game, this)
    }

    override fun toast(msg: String) {
        runOnUiThread {
            Toast.makeText(context, msg, Toast.LENGTH_LONG).show()
        }
    }

    override fun versionName(): String {
        return versionName
    }

    override fun getSharedPreferencesIntValue(key: String, default: Int): Int {
        val sharedPreferences = getSharedPreferences(SHARED_PREFERENCES_DATA_NAME, MODE_PRIVATE)
        return sharedPreferences.getInt(key, default)
    }

    override fun getSharedPreferencesBooleanValue(key: String, default: Boolean): Boolean {
        val sharedPreferences = getSharedPreferences(SHARED_PREFERENCES_DATA_NAME, MODE_PRIVATE)
        return sharedPreferences.getBoolean(key, default)
    }

    override fun getSharedPreferencesLongValue(key: String, default: Long): Long {
        val sharedPreferences = getSharedPreferences(SHARED_PREFERENCES_DATA_NAME, MODE_PRIVATE)
        return sharedPreferences.getLong(key, default)
    }

    override fun saveSharedPreferencesIntValue(key: String, value: Int) {
        val sharedPreferences = getSharedPreferences(SHARED_PREFERENCES_DATA_NAME, MODE_PRIVATE)
        val editor = sharedPreferences.edit()
        editor.putInt(key, value)
        editor.apply()
    }

    override fun saveSharedPreferencesLongValue(key: String, value: Long) {
        val sharedPreferences = getSharedPreferences(SHARED_PREFERENCES_DATA_NAME, MODE_PRIVATE)
        val editor = sharedPreferences.edit()
        editor.putLong(key, value)
        editor.apply()
    }

    override fun saveSharedPreferencesBooleanValue(key: String, value: Boolean) {
        val sharedPreferences = getSharedPreferences(SHARED_PREFERENCES_DATA_NAME, MODE_PRIVATE)
        val editor = sharedPreferences.edit()
        editor.putBoolean(key, value)
        editor.apply()
    }

    override fun initializeInAppPurchases(
        onSuccess: (products: Map<String, Product>) -> Unit,
        onFailure: (message: String) -> Unit
    ) {
        googleServicesHandler.initializeInAppPurchases(onSuccess, onFailure)
    }

    override fun launchBillingFlow(selectedProduct: Product) {
        googleServicesHandler.launchBillingFlow(selectedProduct)
    }

    override fun initializeAds(onFinish: () -> Unit) {
        val requestConfiguration = MobileAds.getRequestConfiguration()
            .toBuilder()
            .setTagForChildDirectedTreatment(
                RequestConfiguration.TAG_FOR_CHILD_DIRECTED_TREATMENT_TRUE
            )
            .setMaxAdContentRating(RequestConfiguration.MAX_AD_CONTENT_RATING_G)
            .build()
        MobileAds.setRequestConfiguration(requestConfiguration)
        MobileAds.initialize(this) { onFinish.invoke() }
    }

    override fun loadVideoAd(onLoaded: () -> Unit) {
        runOnUiThread {
            googleServicesHandler.loadVideoAd(onLoaded, this)
        }
    }

    override fun displayRewardedAd(onAdCompleted: () -> Unit, onAdDismissed: () -> Unit) {
        runOnUiThread {
            googleServicesHandler.displayRewardedAd(onAdCompleted, onAdDismissed, this, game)
        }
    }

    override fun loadBannerAd() {
        if (shouldLoadBannerAd()) {
            runOnUiThread {
                googleServicesHandler.loadBannerAd()
            }
        }
    }

    override fun hideBannerAd() {
        runOnUiThread {
            googleServicesHandler.hideBannerAd()
        }
    }

    override fun submitScore(score: Long, leaderboardsId: String): Boolean {
        return googleServicesHandler.submitScore(score, leaderboardsId)
    }

    override fun displayLeaderboard(leaderboardsId: String) {
        googleServicesHandler.displayLeaderboard(leaderboardsId)
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (requestCode == RC_LEADERBOARD) {
            game.onLeaderboardClosed()
        }
    }

    override fun fetchChampion(difficulty: Difficulties, callback: OnChampionFetched) {
        googleServicesHandler.fetchChampion(difficulty, callback)
    }

    override fun isConnected(): Boolean {
        return googleServicesHandler.isConnected()
    }

    override fun onDestroy() {
        super.onDestroy()
        runOnUiThread {
            googleServicesHandler.onDestroy()
        }
    }

    private fun createAndroidApplicationConfig(): AndroidApplicationConfiguration {
        val config = AndroidApplicationConfiguration()
        config.numSamples = 2
        return config
    }

    private fun createLayout() {
        layout = RelativeLayout(context)
        val view = initializeForView(game, createAndroidApplicationConfig())
        layout.addView(view)
        setContentView(layout)
        @Suppress("DEPRECATION")
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
            window.insetsController?.hide(WindowInsets.Type.statusBars())
        } else {
            window.setFlags(
                WindowManager.LayoutParams.FLAG_FULLSCREEN,
                WindowManager.LayoutParams.FLAG_FULLSCREEN
            )
            window.clearFlags(WindowManager.LayoutParams.FLAG_FORCE_NOT_FULLSCREEN)
        }
        googleServicesHandler.addBannerAdLayout(this, layout)
    }

    private fun PackageManager.getPackageInfoCompat(
        packageName: String,
        flags: Int = 0
    ): PackageInfo =
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            getPackageInfo(packageName, PackageManager.PackageInfoFlags.of(flags.toLong()))
        } else {
            @Suppress("DEPRECATION") getPackageInfo(packageName, flags)
        }

    private fun shouldLoadBannerAd() =
        DebugSettings.ALWAYS_DISPLAY_BANNER_ADS || getSharedPreferencesLongValue(
            SharedPreferencesKeys.DISABLE_ADS_DUE_DATE,
            0
        ) < TimeUtils.millis()
}
