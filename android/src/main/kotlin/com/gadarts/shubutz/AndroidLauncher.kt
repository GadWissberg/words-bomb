package com.gadarts.shubutz

import android.content.pm.PackageInfo
import android.content.pm.PackageManager
import android.os.Build
import android.os.Bundle
import android.view.Window
import android.view.WindowInsets
import android.view.WindowManager
import android.widget.RelativeLayout
import android.widget.Toast
import com.badlogic.gdx.backends.android.AndroidApplication
import com.badlogic.gdx.backends.android.AndroidApplicationConfiguration
import com.gadarts.shubutz.core.AndroidInterface
import com.gadarts.shubutz.core.ShubutzGame
import com.gadarts.shubutz.core.model.assets.SharedPreferencesKeys.SHARED_PREFERENCES_DATA_NAME
import com.google.firebase.FirebaseApp


class AndroidLauncher : AndroidApplication(), AndroidInterface {


    private lateinit var game: ShubutzGame
    private var versionName = "0.0.0"
    private lateinit var layout: RelativeLayout
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        FirebaseApp.initializeApp(context)
        window.addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON)
        try {
            val pInfo = context.packageManager.getPackageInfoCompat(context.packageName, 0)
            versionName = pInfo.versionName ?: "0.0.0"
        } catch (e: PackageManager.NameNotFoundException) {
            e.printStackTrace()
        }
        game = ShubutzGame(this)
        requestWindowFeature(Window.FEATURE_NO_TITLE)
        createLayout()
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

    private fun createAndroidApplicationConfig(): AndroidApplicationConfiguration {
        val config = AndroidApplicationConfiguration()
        config.numSamples = 2
        return config
    }

    private fun createLayout() {
        layout = RelativeLayout(context)
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
}
