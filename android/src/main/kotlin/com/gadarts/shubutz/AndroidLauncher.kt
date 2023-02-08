package com.gadarts.shubutz

import android.content.pm.PackageInfo
import android.content.pm.PackageManager
import android.os.Build
import android.os.Bundle
import android.view.WindowManager
import android.widget.Toast
import com.android.billingclient.api.*
import com.android.billingclient.api.BillingClient.BillingResponseCode
import com.badlogic.gdx.Gdx
import com.badlogic.gdx.backends.android.AndroidApplication
import com.badlogic.gdx.backends.android.AndroidApplicationConfiguration
import com.gadarts.shubutz.core.AndroidInterface
import com.gadarts.shubutz.core.ShubutzGame
import com.gadarts.shubutz.core.model.InAppProducts
import com.gadarts.shubutz.core.model.Product


class AndroidLauncher : AndroidApplication(), AndroidInterface {

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
                toast(billingResult.debugMessage)
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
        initialize(game, createAndroidApplicationConfig())
        createBillingClient()
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

    override fun getSharedPreferencesValue(key: String): Int {
        val sharedPreferences = getSharedPreferences(SHARED_PREFERENCES_DATA_NAME, MODE_PRIVATE)
        return sharedPreferences.getInt(key, 0)
    }

    override fun saveSharedPreferencesValue(key: String, value: Int) {
        val sharedPreferences = getSharedPreferences(SHARED_PREFERENCES_DATA_NAME, MODE_PRIVATE)
        val editor = sharedPreferences.edit()
        editor.putInt(key, value)
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
            toast(billingResult.debugMessage)
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
                    it.productId to Product(
                        it.productId,
                        it.name,
                        it.oneTimePurchaseOfferDetails?.formattedPrice ?: "(?)",
                    )
                })
            } else {
                onFailure.invoke(FAILURE_MESSAGE_IN_APP_PURCHASE)
            }
        }
    }

    companion object {
        private const val SHARED_PREFERENCES_DATA_NAME = "data"
        private const val FAILURE_MESSAGE_IN_APP_PURCHASE = "אופס! קרתה תקלה..."
    }
}