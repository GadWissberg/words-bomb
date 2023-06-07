package com.gadarts.shubutz

import android.app.Activity
import com.android.billingclient.api.BillingClient
import com.android.billingclient.api.BillingClientStateListener
import com.android.billingclient.api.BillingFlowParams
import com.android.billingclient.api.BillingResult
import com.android.billingclient.api.ConsumeParams
import com.android.billingclient.api.ProductDetails
import com.android.billingclient.api.Purchase
import com.android.billingclient.api.PurchasesUpdatedListener
import com.android.billingclient.api.QueryProductDetailsParams
import com.badlogic.gdx.Gdx
import com.gadarts.shubutz.core.ShubutzGame
import com.gadarts.shubutz.core.model.InAppProducts
import com.gadarts.shubutz.core.model.Product

class PurchaseHandler(private val game: ShubutzGame, private val context: Activity) {

    private val purchasesUpdatedListener =
        PurchasesUpdatedListener { billingResult, purchases ->
            if (billingResult.responseCode == BillingClient.BillingResponseCode.OK && purchases != null) {
                for (purchase in purchases) {
                    if (purchase.purchaseState == Purchase.PurchaseState.PURCHASED) {
                        consumePurchase(purchase)
                    }
                }
            } else if (billingResult.responseCode != BillingClient.BillingResponseCode.USER_CANCELED) {
                Gdx.app.log("Shubutz", "Purchase failed: ${billingResult.debugMessage}")
            }
        }
    private lateinit var billingClient: BillingClient

    init {
        createBillingClient()
    }

    private fun consumePurchase(purchase: Purchase) {
        val consumeParams = ConsumeParams.newBuilder()
            .setPurchaseToken(purchase.purchaseToken)
            .build()
        billingClient.consumeAsync(consumeParams) { billingResult, _ ->
            if (billingResult.responseCode == BillingClient.BillingResponseCode.OK) {
                game.onSuccessfulPurchase(purchase.products)
            } else {
                game.onFailedPurchase(FAILURE_MESSAGE_IN_APP_PURCHASE)
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
            if (billingResult.responseCode == BillingClient.BillingResponseCode.OK) {
                onSuccess.invoke(productDetailsList.associate {
                    val oneTimePurchaseOfferDetails = it.oneTimePurchaseOfferDetails
                    it.productId to Product(
                        it.productId,
                        oneTimePurchaseOfferDetails?.formattedPrice ?: ""
                    )
                })
            } else {
                onFailure.invoke(FAILURE_MESSAGE_IN_APP_PURCHASE)
            }
        }
    }

    private fun createBillingClient() {
        billingClient = BillingClient.newBuilder(context)
            .setListener(purchasesUpdatedListener)
            .enablePendingPurchases()
            .build()
    }

    fun initializeInAppPurchases(
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

    fun launchBillingFlow(selectedProduct: Product) {
        val productDetailsParamsList = listOf(
            BillingFlowParams.ProductDetailsParams.newBuilder()
                .setProductDetails(selectedProduct.formattedPrice as ProductDetails)
                .build()
        )
        val billingFlowParams = BillingFlowParams.newBuilder()
            .setProductDetailsParamsList(productDetailsParamsList)
            .build()
        val billingResult = billingClient.launchBillingFlow(context, billingFlowParams)
        if (billingResult.responseCode != BillingClient.BillingResponseCode.OK) {
            Gdx.app.log("Shubutz", "Failed to launch billing flow: ${billingResult.debugMessage}")
        }
    }

    companion object {
        private const val FAILURE_MESSAGE_IN_APP_PURCHASE = "אופס! קרתה תקלה..."
    }

}
