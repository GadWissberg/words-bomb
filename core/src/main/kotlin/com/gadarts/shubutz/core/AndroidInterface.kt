package com.gadarts.shubutz.core

interface AndroidInterface {
    fun toast(msg: String)
    fun versionName(): String
    fun getSharedPreferencesValue(key: String): Int
    fun saveSharedPreferencesValue(sharedPreferencesDataKeyCoins: String, coins: Int)
}
