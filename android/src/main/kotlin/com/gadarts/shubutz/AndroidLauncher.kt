package com.gadarts.shubutz

import android.content.Intent
import android.content.pm.PackageInfo
import android.content.pm.PackageManager
import android.os.Build
import android.os.Bundle
import android.util.Log
import android.view.View.GONE
import android.view.View.VISIBLE
import android.view.Window
import android.view.WindowInsets
import android.view.WindowManager
import android.widget.RelativeLayout
import android.widget.Toast
import com.android.billingclient.api.*
import com.badlogic.gdx.Gdx
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
import com.gadarts.shubutz.core.screens.menu.view.Champion
import com.gadarts.shubutz.core.screens.menu.view.OnChampionFetched
import com.google.android.gms.ads.AdListener
import com.google.android.gms.ads.AdRequest
import com.google.android.gms.ads.AdSize
import com.google.android.gms.ads.AdView
import com.google.android.gms.ads.FullScreenContentCallback
import com.google.android.gms.ads.LoadAdError
import com.google.android.gms.ads.MobileAds
import com.google.android.gms.ads.rewarded.RewardedAd
import com.google.android.gms.ads.rewarded.RewardedAdLoadCallback
import de.golfgl.gdxgamesvcs.GameServiceException
import de.golfgl.gdxgamesvcs.GpgsClient
import de.golfgl.gdxgamesvcs.GpgsClient.RC_LEADERBOARD
import de.golfgl.gdxgamesvcs.leaderboard.ILeaderBoardEntry


class AndroidLauncher : AndroidApplication(), AndroidInterface {

    private lateinit var gsClient: GpgsClient
    private lateinit var adView: AdView
    private lateinit var layout: RelativeLayout
    private lateinit var loadedAd: RewardedAd
    private lateinit var game: ShubutzGame
    private var versionName = "0.0.0"
    private lateinit var purchaseHandler: PurchaseHandler

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
        purchaseHandler = PurchaseHandler(game, this)
        createLayout()
        gsClient = GpgsClient().initialize(this, false)
        signInToPlayServices()
    }

    private fun signInToPlayServices() {
        val loggedIn = gsClient.logIn()
        if (loggedIn) {
            Gdx.app.log("Play Services", "Signed in successfully")
        } else {
            Gdx.app.log("Play Services", "Did not sign in")
        }
    }

    private fun createLayout() {
        layout = RelativeLayout(this)
        requestWindowFeature(Window.FEATURE_NO_TITLE)
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
        addBannerAdLayout()
    }

    private fun addBannerAdLayout() {
        adView = AdView(this)
        val adParams = RelativeLayout.LayoutParams(
            RelativeLayout.LayoutParams.WRAP_CONTENT,
            RelativeLayout.LayoutParams.WRAP_CONTENT
        )
        adView.setAdSize(AdSize.BANNER)
        adView.adUnitId =
            if (DebugSettings.TEST_ADS) BANNER_AD_UNIT_TEST else BANNER_AD_UNIT_PROD
        adParams.addRule(RelativeLayout.ALIGN_PARENT_BOTTOM)
        adParams.addRule(RelativeLayout.CENTER_HORIZONTAL)
        layout.addView(adView, adParams)
        defineBannerAdListener(adView)
    }

    private fun createAndroidApplicationConfig(): AndroidApplicationConfiguration {
        val config = AndroidApplicationConfiguration()
        config.numSamples = 2
        return config
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
        purchaseHandler.initializeInAppPurchases(onSuccess, onFailure)
    }

    override fun launchBillingFlow(selectedProduct: Product) {
        purchaseHandler.launchBillingFlow(selectedProduct)
    }

    override fun initializeAds(onFinish: () -> Unit) {
        MobileAds.initialize(this) { onFinish.invoke() }
    }

    override fun loadVideoAd(onLoaded: () -> Unit) {
        val adRequest = AdRequest.Builder().build()
        runOnUiThread {
            RewardedAd.load(
                this,
                if (DebugSettings.TEST_ADS) REWARDED_AD_UNIT_TEST else REWARDED_AD_UNIT_PROD,
                adRequest,
                object : RewardedAdLoadCallback() {
                    override fun onAdFailedToLoad(adError: LoadAdError) {
                        adError.toString().let { Log.d("Rewarded Ad", it) }
                    }

                    override fun onAdLoaded(ad: RewardedAd) {
                        loadedAd = ad
                        onLoaded.invoke()
                    }
                })
        }

    }

    override fun displayRewardedAd(onAdCompleted: () -> Unit, onAdDismissed: () -> Unit) {
        val activity = this
        runOnUiThread {
            loadedAd.fullScreenContentCallback = object : FullScreenContentCallback() {
                override fun onAdDismissedFullScreenContent() {
                    onAdDismissed.invoke()
                }
            }
            loadedAd.let {
                it.show(activity) { rewardItem ->
                    val rewardAmount = rewardItem.amount
                    game.onRewardForVideoAd(rewardAmount)
                    onAdCompleted.invoke()
                }
            }
        }
    }

    override fun loadBannerAd() {
        if (shouldLoadBannerAd()) {
            runOnUiThread {
                val adRequest = AdRequest.Builder().build()
                adView.visibility = VISIBLE
                adView.loadAd(adRequest)
            }
        }
    }

    private fun shouldLoadBannerAd() =
        DebugSettings.ALWAYS_DISPLAY_BANNER_ADS || getSharedPreferencesLongValue(
            SharedPreferencesKeys.DISABLE_ADS_DUE_DATE,
            0
        ) < TimeUtils.millis()

    override fun hideBannerAd() {
        runOnUiThread {
            adView.destroy()
            adView.visibility = GONE
        }
    }

    override fun submitScore(score: Long, leaderboardsId: String): Boolean {
        return gsClient.submitToLeaderboard(leaderboardsId, score, null)
    }

    override fun displayLeaderboard(leaderboardsId: String) {
        try {
            gsClient.showLeaderboards(leaderboardsId)
        } catch (ex: GameServiceException) {
            Gdx.app.error("Play Services", ex.message)
        }
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (requestCode == RC_LEADERBOARD) {
            game.onLeaderboardClosed()
        }
    }

    override fun fetchChampion(difficulty: Difficulties, callback: OnChampionFetched) {
        val success = gsClient.fetchLeaderboardEntries(difficulty.leaderboardsId, 1, false) {
            if (!it.isEmpty) {
                val first = it.first()
                callback.run(
                    tryToConvertEntry(first, difficulty)
                )
            }
        }
        if (!success) {
            callback.run(null)
        }
    }

    override fun isConnected(): Boolean {
        return gsClient.isSessionActive
    }

    private fun tryToConvertEntry(first: ILeaderBoardEntry?, difficulty: Difficulties): Champion? {
        var result: Champion? = null
        try {
            result = Champion(
                first!!.userDisplayName,
                first.formattedValue.toLong(),
                difficulty
            )
        } catch (_: Exception) {
        }
        return result
    }

    override fun onDestroy() {
        super.onDestroy()
        runOnUiThread {
            adView.destroy()
        }
    }

    private fun defineBannerAdListener(adView: AdView) {
        adView.adListener = object : AdListener() {
            override fun onAdClicked() {
            }

            override fun onAdClosed() {
            }

            override fun onAdFailedToLoad(adError: LoadAdError) {
            }

            override fun onAdImpression() {
            }

            override fun onAdLoaded() {
            }

            override fun onAdOpened() {
            }
        }
    }


    companion object {
        private const val REWARDED_AD_UNIT_TEST = "ca-app-pub-3940256099942544/5224354917"
        private const val REWARDED_AD_UNIT_PROD = "ca-app-pub-2312113291496409/2061684764"
        private const val BANNER_AD_UNIT_PROD = "ca-app-pub-2312113291496409/7487568541"
        private const val BANNER_AD_UNIT_TEST = "ca-app-pub-3940256099942544/6300978111"
    }
}