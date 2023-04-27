package com.gadarts.shubutz

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
import com.android.billingclient.api.BillingClient.BillingResponseCode
import com.badlogic.gdx.Gdx
import com.badlogic.gdx.backends.android.AndroidApplication
import com.badlogic.gdx.backends.android.AndroidApplicationConfiguration
import com.gadarts.shubutz.core.AndroidInterface
import com.gadarts.shubutz.core.DebugSettings
import com.gadarts.shubutz.core.ShubutzGame
import com.gadarts.shubutz.core.model.InAppProducts
import com.gadarts.shubutz.core.model.Product
import com.gadarts.shubutz.core.model.assets.SharedPreferencesKeys
import com.gadarts.shubutz.core.model.assets.SharedPreferencesKeys.SHARED_PREFERENCES_DATA_NAME
import com.google.android.gms.ads.AdListener
import com.google.android.gms.ads.AdRequest
import com.google.android.gms.ads.AdSize
import com.google.android.gms.ads.AdView
import com.google.android.gms.ads.FullScreenContentCallback
import com.google.android.gms.ads.LoadAdError
import com.google.android.gms.ads.MobileAds
import com.google.android.gms.ads.rewarded.RewardedAd
import com.google.android.gms.ads.rewarded.RewardedAdLoadCallback


class AndroidLauncher : AndroidApplication(), AndroidInterface {

    private lateinit var adView: AdView
    private lateinit var layout: RelativeLayout
    private lateinit var loadedAd: RewardedAd
    private lateinit var game: ShubutzGame
    private var versionName = "0.0.0"
    private val purchasesUpdatedListener =
        PurchasesUpdatedListener { billingResult, purchases ->
            if (billingResult.responseCode == BillingResponseCode.OK && purchases != null) {
                for (purchase in purchases) {
                    if (purchase.purchaseState == Purchase.PurchaseState.PURCHASED) {
                        consumePurchase(purchase)
                    }
                }
            } else if (billingResult.responseCode != BillingResponseCode.USER_CANCELED) {
                Gdx.app.log("Shubutz", "Purchase failed: ${billingResult.debugMessage}")
            }
        }
    private lateinit var billingClient: BillingClient

    private fun consumePurchase(purchase: Purchase) {
        val consumeParams = ConsumeParams.newBuilder()
            .setPurchaseToken(purchase.purchaseToken)
            .build()
        billingClient.consumeAsync(consumeParams) { billingResult, _ ->
            if (billingResult.responseCode == BillingResponseCode.OK) {
                game.onSuccessfulPurchase(purchase.products)
            } else {
                game.onFailedPurchase(FAILURE_MESSAGE_IN_APP_PURCHASE)
            }
        }
    }

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
        createLayout()
        createBillingClient()
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

    private fun createBillingClient() {
        billingClient = BillingClient.newBuilder(context)
            .setListener(purchasesUpdatedListener)
            .enablePendingPurchases()
            .build()
    }

    override fun toast(msg: String) {
        runOnUiThread {
            Toast.makeText(context, msg, Toast.LENGTH_LONG).show()
        }
    }

    override fun versionName(): String {
        return versionName
    }

    override fun getSharedPreferencesIntValue(key: String): Int {
        val sharedPreferences = getSharedPreferences(SHARED_PREFERENCES_DATA_NAME, MODE_PRIVATE)
        return sharedPreferences.getInt(key, 0)
    }

    override fun getSharedPreferencesBooleanValue(key: String, default: Boolean): Boolean {
        val sharedPreferences = getSharedPreferences(SHARED_PREFERENCES_DATA_NAME, MODE_PRIVATE)
        return sharedPreferences.getBoolean(key, default)
    }

    override fun saveSharedPreferencesIntValue(key: String, value: Int) {
        val sharedPreferences = getSharedPreferences(SHARED_PREFERENCES_DATA_NAME, MODE_PRIVATE)
        val editor = sharedPreferences.edit()
        editor.putInt(key, value)
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
        billingClient.startConnection(object : BillingClientStateListener {
            override fun onBillingServiceDisconnected() {
                Gdx.app.log("BillingClientState", "Disconnected!")
            }

            override fun onBillingSetupFinished(p0: BillingResult) {
                Gdx.app.log("BillingClientState", "${p0.responseCode} - ${p0.debugMessage}")
                if (billingClient.connectionState == BillingClient.ConnectionState.CONNECTED) {
                    fetchProducts(onSuccess, onFailure)
                } else {
                    onFailure.invoke(FAILURE_MESSAGE_IN_APP_PURCHASE)
                }
            }
        }
        )

    }

    override fun launchBillingFlow(selectedProduct: Product) {
        val productDetailsParamsList = listOf(
            BillingFlowParams.ProductDetailsParams.newBuilder()
                .setProductDetails(selectedProduct.productDetails as ProductDetails)
                .build()
        )
        val billingFlowParams = BillingFlowParams.newBuilder()
            .setProductDetailsParamsList(productDetailsParamsList)
            .build()
        val billingResult = billingClient.launchBillingFlow(this, billingFlowParams)
        if (billingResult.responseCode != BillingResponseCode.OK) {
            Gdx.app.log("Shubutz", "Failed to launch billing flow: ${billingResult.debugMessage}")
        }
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
        if (!DebugSettings.ALWAYS_DISPLAY_BANNER_ADS && getSharedPreferencesBooleanValue(
                SharedPreferencesKeys.DISABLE_ADS,
                false
            )
        ) return

        runOnUiThread {
            val adRequest = AdRequest.Builder().build()
            adView.visibility = VISIBLE
            adView.loadAd(adRequest)
        }
    }

    override fun hideBannerAd() {
        runOnUiThread {
            adView.destroy()
            adView.visibility = GONE
        }
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

    private fun fetchProducts(
        onSuccess: (products: Map<String, Product>) -> Unit,
        onFailure: (message: String) -> Unit
    ) {
        val newBuilder = QueryProductDetailsParams.Product.newBuilder()
        val queryProductDetailsParams = QueryProductDetailsParams.newBuilder().setProductList(
            InAppProducts.values().map {
                newBuilder
                    .setProductId(it.name.lowercase())
                    .setProductType(BillingClient.ProductType.INAPP)
                    .build()
            }
        ).build()
        billingClient.queryProductDetailsAsync(queryProductDetailsParams) { billingResult,
                                                                            productDetailsList ->
            if (billingResult.responseCode == BillingResponseCode.OK) {
                onSuccess.invoke(productDetailsList.associate {
                    it.productId to Product(it.productId, it)
                })
            } else {
                onFailure.invoke(FAILURE_MESSAGE_IN_APP_PURCHASE)
            }
        }
    }

    companion object {
        private const val FAILURE_MESSAGE_IN_APP_PURCHASE = "אופס! קרתה תקלה..."
        private const val REWARDED_AD_UNIT_TEST = "ca-app-pub-3940256099942544/5224354917"
        private const val REWARDED_AD_UNIT_PROD = "ca-app-pub-2312113291496409/2061684764"
        private const val BANNER_AD_UNIT_PROD = "ca-app-pub-2312113291496409/7487568541"
        private const val BANNER_AD_UNIT_TEST = "ca-app-pub-3940256099942544/6300978111"
    }
}