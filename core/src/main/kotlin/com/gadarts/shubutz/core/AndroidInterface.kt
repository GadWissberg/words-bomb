package com.gadarts.shubutz.core

import com.gadarts.shubutz.core.model.Product

interface AndroidInterface {
    fun toast(msg: String)
    fun versionName(): String
    fun getSharedPreferencesValue(key: String): Int
    fun saveSharedPreferencesValue(key: String, value: Int)
    fun initializeInAppPurchases(
        onSuccess: (products: Map<String, Product>) -> Unit,
        onFailure: (message: String) -> Unit
    )

    fun launchBillingFlow(selectedProduct: Product)
    fun initializeAds(onFinish: () -> Unit)
    fun loadAd()
}
