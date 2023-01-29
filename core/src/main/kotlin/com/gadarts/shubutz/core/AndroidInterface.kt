package com.gadarts.shubutz.core

import com.gadarts.shubutz.core.model.InAppPacks
import com.gadarts.shubutz.core.model.Product

interface AndroidInterface {
    fun toast(msg: String)
    fun versionName(): String
    fun getSharedPreferencesValue(key: String): Int
    fun saveSharedPreferencesValue(key: String, value: Int)
    fun initializeInAppPurchases(postAction: (products: Map<String, Product>) -> Unit)
    fun launchBillingFlow(selectedProduct: Product, postAction: () -> String)
}
