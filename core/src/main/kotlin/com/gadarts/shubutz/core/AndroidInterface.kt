package com.gadarts.shubutz.core

import com.gadarts.shubutz.core.model.Difficulties
import com.gadarts.shubutz.core.model.GameModel
import com.gadarts.shubutz.core.model.Product

interface AndroidInterface {
    fun toast(msg: String)
    fun versionName(): String
    fun getSharedPreferencesIntValue(key: String, default: Int = 0): Int
    fun getSharedPreferencesBooleanValue(key: String, default: Boolean): Boolean
    fun getSharedPreferencesLongValue(key: String, default: Long): Long
    fun saveSharedPreferencesIntValue(key: String, value: Int)
    fun saveSharedPreferencesLongValue(key: String, value: Long)
    fun saveSharedPreferencesBooleanValue(key: String, value: Boolean)

}
