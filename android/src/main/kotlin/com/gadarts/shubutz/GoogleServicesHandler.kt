package com.gadarts.shubutz

import android.util.Log
import android.view.View
import android.widget.RelativeLayout
import android.widget.Toast
import com.badlogic.gdx.Gdx
import com.badlogic.gdx.backends.android.AndroidApplication
import com.gadarts.shubutz.core.DebugSettings
import com.gadarts.shubutz.core.ShubutzGame
import com.gadarts.shubutz.core.model.Difficulties
import com.gadarts.shubutz.core.model.Product
import com.gadarts.shubutz.core.screens.menu.view.Champion
import com.gadarts.shubutz.core.screens.menu.view.OnChampionFetched
import com.google.android.gms.ads.AdListener
import com.google.android.gms.ads.AdRequest
import com.google.android.gms.ads.AdSize
import com.google.android.gms.ads.AdView
import com.google.android.gms.ads.FullScreenContentCallback
import com.google.android.gms.ads.LoadAdError
import com.google.android.gms.ads.rewarded.RewardedAd
import com.google.android.gms.ads.rewarded.RewardedAdLoadCallback
import com.google.firebase.crashlytics.FirebaseCrashlytics
import de.golfgl.gdxgamesvcs.GameServiceException
import de.golfgl.gdxgamesvcs.GpgsClient
import de.golfgl.gdxgamesvcs.IGameServiceListener
import de.golfgl.gdxgamesvcs.leaderboard.ILeaderBoardEntry

class GoogleServicesHandler {

    private lateinit var gsClient: GpgsClient
    private lateinit var adView: AdView
    private lateinit var loadedAd: RewardedAd
    private lateinit var purchaseHandler: PurchaseHandler

    fun onCreate(game: ShubutzGame, context: AndroidApplication) {
        purchaseHandler = PurchaseHandler(game, context)
        gsClient = GpgsClient().initialize(context, false)
        gsClient.setListener(object : IGameServiceListener {
            override fun gsOnSessionActive() {

            }

            override fun gsOnSessionInactive() {
            }

            override fun gsShowErrorToUser(
                et: IGameServiceListener.GsErrorType?,
                msg: String?,
                t: Throwable?
            ) {
                logGameServicesError(context, et, msg, t)
            }

        })
        gsClient.connect(true)
    }

    private fun logGameServicesError(
        context: AndroidApplication,
        et: IGameServiceListener.GsErrorType?,
        msg: String?,
        t: Throwable?
    ) {
        Toast.makeText(context, "התחברות נכשלה", Toast.LENGTH_LONG).show()
        val error =
            "Failed to login to Google Games Service. ErrorType: $et, Message: $msg, Exception: $t"
        Gdx.app.error(
            GoogleServicesHandler::javaClass.name,
            error
        )
        FirebaseCrashlytics.getInstance().log(error)
    }

    fun addBannerAdLayout(context: AndroidApplication, layout: RelativeLayout) {
        adView = AdView(context)
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

    fun initializeInAppPurchases(
        onSuccess: (products: Map<String, Product>) -> Unit,
        onFailure: (message: String) -> Unit
    ) {
        purchaseHandler.initializeInAppPurchases(onSuccess, onFailure)
    }

    fun launchBillingFlow(selectedProduct: Product) {
        purchaseHandler.launchBillingFlow(selectedProduct)
    }

    fun loadVideoAd(onLoaded: () -> Unit, context: AndroidApplication) {
        val adRequest = AdRequest.Builder().build()
        RewardedAd.load(
            context,
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

    fun displayRewardedAd(
        onAdCompleted: () -> Unit,
        onAdDismissed: () -> Unit,
        application: AndroidApplication,
        game: ShubutzGame
    ) {
        loadedAd.fullScreenContentCallback = object : FullScreenContentCallback() {
            override fun onAdDismissedFullScreenContent() {
                onAdDismissed.invoke()
            }
        }
        loadedAd.let {
            it.show(application) { rewardItem ->
                val rewardAmount = rewardItem.amount
                game.onRewardForVideoAd(rewardAmount)
                onAdCompleted.invoke()
            }
        }
    }

    fun loadBannerAd() {
        val adRequest = AdRequest.Builder().build()
        adView.visibility = View.VISIBLE
        adView.loadAd(adRequest)
    }

    fun hideBannerAd() {
        adView.destroy()
        adView.visibility = View.GONE
    }

    fun submitScore(score: Long, leaderboardsId: String): Boolean {
        return gsClient.submitToLeaderboard(leaderboardsId, score, null)
    }

    fun displayLeaderboard(leaderboardsId: String) {
        try {
            gsClient.showLeaderboards(leaderboardsId)
        } catch (ex: GameServiceException) {
            var message = "Failed to display the leaderboards!"
            if (ex.message != null) {
                message = ex.message!!
            }
            Gdx.app.error("Play Services", message)
        }
    }

    fun fetchChampion(difficulty: Difficulties, callback: OnChampionFetched) {
        val success = gsClient.fetchLeaderboardEntries(difficulty.leaderboardsId, 1, false) {
            if (it != null && !it.isEmpty) {
                val first = it.first()
                callback.run(
                    tryToConvertEntry(first, difficulty)
                )
            } else {
                callback.run(null)
            }
        }
        if (!success) {
            callback.run(null)
        }
    }

    fun isConnected(): Boolean {
        return gsClient.isSessionActive
    }

    fun onDestroy() {
        adView.destroy()
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

    fun login(): Boolean {
        return gsClient.logIn()
    }

    companion object {
        private const val REWARDED_AD_UNIT_TEST = "ca-app-pub-3940256099942544/5224354917"
        private const val REWARDED_AD_UNIT_PROD = "ca-app-pub-2312113291496409/2061684764"
        private const val BANNER_AD_UNIT_PROD = "ca-app-pub-2312113291496409/7487568541"
        private const val BANNER_AD_UNIT_TEST = "ca-app-pub-3940256099942544/6300978111"
    }
}
