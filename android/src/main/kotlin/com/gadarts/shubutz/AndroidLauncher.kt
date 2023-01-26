package com.gadarts.shubutz

import android.content.pm.PackageManager
import android.os.Bundle
import android.widget.Toast
import com.android.billingclient.api.*
import com.badlogic.gdx.Gdx
import com.badlogic.gdx.backends.android.AndroidApplication
import com.badlogic.gdx.backends.android.AndroidApplicationConfiguration
import com.gadarts.shubutz.core.AndroidInterface
import com.gadarts.shubutz.core.ShubutzGame
import com.gadarts.shubutz.core.model.InAppPacks


class AndroidLauncher : AndroidApplication(), AndroidInterface {
    private var versionName = "0.0.0"
    private val purchasesUpdatedListener =
        PurchasesUpdatedListener { billingResult, purchases ->
            // To be implemented in a later section.
        }
    private lateinit var billingClient: BillingClient

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        try {
            val pInfo = context.packageManager.getPackageInfo(context.packageName, 0)
            versionName = pInfo.versionName
        } catch (e: PackageManager.NameNotFoundException) {
            e.printStackTrace()
        }
        val config = AndroidApplicationConfiguration()
        config.numSamples = 2
        initialize(ShubutzGame(this), config)
        createBillingClient()
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

    override fun initializeInAppPurchases(postAction: (products: List<String>) -> Unit) {
        billingClient.startConnection(object : BillingClientStateListener {
            override fun onBillingServiceDisconnected() {
                Gdx.app.log("BillingClientState", "Disconnected!")
            }

            override fun onBillingSetupFinished(p0: BillingResult) {
                Gdx.app.log("BillingClientState", "${p0.responseCode} - ${p0.debugMessage}")
                if (billingClient.connectionState == BillingClient.ConnectionState.CONNECTED) {
                    fetchProducts(postAction)
                }
            }
        }
        )

    }

    private fun fetchProducts(postAction: (products: List<String>) -> Unit) {
        val newBuilder = QueryProductDetailsParams.Product.newBuilder()
        val queryProductDetailsParams = QueryProductDetailsParams.newBuilder().setProductList(
            InAppPacks.values().map {
                newBuilder
                    .setProductId(it.name.lowercase())
                    .setProductType(BillingClient.ProductType.INAPP)
                    .build()
            }
        ).build()
        billingClient.queryProductDetailsAsync(queryProductDetailsParams) { billingResult,
                                                                            productDetailsList ->
            if (billingResult.responseCode == BillingClient.BillingResponseCode.OK) {
                postAction.invoke(productDetailsList.map { it.name })
            }
        }
    }

    companion object {
        private const val SHARED_PREFERENCES_DATA_NAME = "data"
    }
}